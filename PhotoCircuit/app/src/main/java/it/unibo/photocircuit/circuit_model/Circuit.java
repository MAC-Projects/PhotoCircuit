package it.unibo.photocircuit.circuit_model;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import it.unibo.photocircuit.ml_handling.CircuitElement;

public class Circuit {
    final private List<Segment> segments;
    final private List<Node> nodes;

    private Circuit(List<Segment> segments, List<Node> nodes) {
        this.segments = segments;
        this.nodes = nodes;
    }

    public static Circuit from(Mat lines, List<CircuitElement> circuitElements) {
        List<Node> nodes = new ArrayList<>();
        List<Segment> segments = new ArrayList<>();

        for (int i = 0; i < lines.rows(); i++) {
            double[] line = lines.get(i, 0);
            int x1 = (int)line[0];
            int y1 = (int)line[1];
            int x2 = (int)line[2];
            int y2 = (int)line[3];
            Node n1 = null;
            Node n2 = null;
            for (Node n : nodes) {
                if (pointsAreClose(x1, y1, n.getX(), n.getY())) {
                    n1 = n;
                }
                if (pointsAreClose(x2, y2, n.getX(), n.getY())) {
                    n2 = n;
                }
            }
            if (n1 == null) {
                n1 = new Node(x1, y1);
                nodes.add(n1);
            }
            if (n2 == null) {
                n2 = new Node(x2, y2);
                nodes.add(n2);
            }
            Segment newSegment = new Segment(n1, n2);
            n1.addSegment(newSegment);
            n2.addSegment(newSegment);
            segments.add(newSegment);
        }

        splitSegmentsAtBranches(segments, nodes);
        segments = removeDuplicateSegments(segments);
        addCircuitElementsToSegments(segments, nodes, circuitElements);

        return new Circuit(segments, nodes);
    }

    private static void splitSegmentsAtBranches(List<Segment> segments, List<Node> nodes) {
        int nSegments = segments.size();
        for (int i = 0; i < nSegments; i++) {
            Segment s = segments.get(i);
            Node node1 = s.getNode1();
            Node node2 = s.getNode2();

            int x1 = Math.min(node1.getX(), node2.getX());
            int x2 = Math.max(node1.getX(), node2.getX());
            int y1 = Math.min(node1.getY(), node2.getY());
            int y2 = Math.max(node1.getY(), node2.getY());

            if (s.isVertical()) {
                int deltaX = x2 - x1;
                if (deltaX < 10) {
                    x1 = x1 - (10 - deltaX) / 2;
                    x2 = x2 + (10 - deltaX) / 2;
                }
            } else {
                int deltaY = y2 - y1;
                if (deltaY < 10) {
                    y1 = y1 - (10 - deltaY) / 2;
                    y2 = y2 - (10 - deltaY) / 2;
                }
            }

            List<Node> collidingNodes = new ArrayList<>();

            for (Node n : nodes) {
                if (n == node1 || n == node2)
                    continue;
                if (n.getX() > x1 && n.getX() < x2 && n.getY() > y1 && n.getY() < y2) {
                    collidingNodes.add(n);
                }
            }

            Comparator<Node> comparator;
            if (s.isVertical()) {
                comparator = Comparator.comparingInt(Node::getY);
                if (node1.getY() > node2.getY()) {
                    Node tmp = node1;
                    node1 = node2;
                    node2 = tmp;
                }
            } else {
                comparator = Comparator.comparingInt(Node::getX);
                if (node1.getX() > node2.getX()) {
                    Node tmp = node1;
                    node1 = node2;
                    node2 = tmp;
                }
            }

            collidingNodes.sort(comparator);

            if (collidingNodes.size() > 0) {
                for (int j = 0; j <= collidingNodes.size(); j++) {
                    Node first, second;
                    if (j == 0)
                        first = node1;
                    else
                        first = collidingNodes.get(j - 1);

                    if (j == collidingNodes.size())
                        second = node2;
                    else
                        second = collidingNodes.get(j);

                    Segment newSegment = new Segment(first, second);

                    if (j == 0)
                        node1.removeSegment(s);
                    if (j == collidingNodes.size())
                        node2.removeSegment(s);

                    segments.add(newSegment);
                    first.addSegment(newSegment);
                    second.addSegment(newSegment);
                }
                segments.remove(i--);
                nSegments--;
            }
        }
    }

    private static void addCircuitElementsToSegments(List<Segment> segments, List<Node> nodes, List<CircuitElement> circuitElements) {
        for (CircuitElement circuitElement : circuitElements) {
            Rect rect = circuitElement.getRect();
            Node n1 = null;
            Node n2 = null;
            for (Node n : nodes) {
                int direction = pointByRect(n.getX(), n.getY(), rect);
                if (direction != -1) {
                    if (n1 == null) {
                        n1 = n;
                    } else {
                        n2 = n;
                        break;
                    }
                }
            }
            Element element = new Element(n1, n2, circuitElement.getBestGuessIndex());
            if (n1 != null)
                n1.addSegment(element);
            if (n2 != null)
                n2.addSegment(element);
            segments.add(element);
        }
    }

    private static List<Segment> removeDuplicateSegments(List<Segment> inSegments) {
        List<Segment> outSegments = new ArrayList<>(inSegments.size() / 2);
        for (Segment segment1 : inSegments) {
            boolean found = false;
            for (Segment segment2 : outSegments) {
                Node n11 = segment1.getNode1();
                Node n12 = segment1.getNode2();
                Node n21 = segment2.getNode1();
                Node n22 = segment2.getNode2();

                if ((n11.equals(n21) && n12.equals(n22)) || (n11.equals(n22) && n12.equals(n21))) {
                    found = true;
                    break;
                }
            }
            if (!found)
                outSegments.add(segment1);
        }
        return outSegments;
    }

    private static boolean pointsAreClose(int x1, int y1, int x2, int y2) {
        final int threshold = 30;
        int deltaX = Math.abs(x1 - x2);
        int deltaY = Math.abs(y1 - y2);

        return deltaX <= threshold && deltaY <= threshold;
    }

    // Returns side (0 right, 1 top, 2 left, 3 bottom, -1 none)
    private static int pointByRect(int x, int y, Rect rect) {
        final int threshold = 25;
        int halfThreshold = threshold / 2;

        Point p = new Point(x, y);

        for (int dir = 0; dir < 4; dir++) {
            int checkX, checkY, checkWidth, checkHeight;
            if (dir == 0) {
                checkX = rect.x + rect.width - halfThreshold;
                checkY = rect.y;
                checkWidth = threshold;
                checkHeight = rect.height;
            } else if (dir == 1) {
                checkX = rect.x;
                checkY = rect.y - halfThreshold;
                checkWidth = rect.width;
                checkHeight = threshold;
            } else if (dir == 2) {
                checkX = rect.x - halfThreshold;
                checkY = rect.y;
                checkWidth = threshold;
                checkHeight = rect.height;
            } else /* if (dir == 3) */ {
                checkX = rect.x;
                checkY = rect.y + rect.height - halfThreshold;
                checkWidth = rect.width;
                checkHeight = threshold;
            }

            Rect checkRect = new Rect(checkX, checkY, checkWidth, checkHeight);
            if (checkRect.contains(p)) {
                return dir;
            }
        }

        return -1;
    }

    public void invertY(int height) {
        for (Node n : nodes) {
            n.setY(height - n.getY());
        }
    }

    public void straightenSingleDim(int dimension) {
        List<Node> exploredNodes = new ArrayList<>();

        List<Node> sortedNodes = new ArrayList<>(this.nodes);
        sortedNodes.sort((n1, n2) -> dimension == 0 ? n1.getX() - n2.getX() : n1.getY() - n2.getY());

        int direction = dimension == 0 ? 0 : 3;

        for (Node n : sortedNodes) {
            if (exploredNodes.contains(n))
                continue;
            List<Node> connectedNodes = new ArrayList<>();

            Node nextNode = n;
            int sum = 0;
            do {
                sum += dimension == 0 ? nextNode.getY() : nextNode.getX();
                connectedNodes.add(nextNode);
                exploredNodes.add(nextNode);
            } while ((nextNode = nextNode.getNodeOnDirection(direction)) != null);

            int average = sum / connectedNodes.size();

            for (Node nn : connectedNodes) {
                if (dimension == 0)
                    nn.setY(average);
                else
                    nn.setX(average);
            }
        }
    }

    public void straighten() {
        straightenSingleDim(0);
        straightenSingleDim(1);
    }

    public void straightenScale() {
        for (Node node : nodes) {
            node.setX(node.getX() / 100);
            node.setY(node.getY() / 100);
        }
    }

    public String toCircuiTikZ() {
        StringBuilder strb = new StringBuilder("\\begin{circuitikz}[american,x=0.01cm,y=0.01cm]\n");
        for (Segment segment : segments) {
            String draw = segment.getCircuiTikZDraw();
            if (draw != null)
                strb.append(draw);
        }
        strb.append("\\end{circuitikz}");
        return strb.toString();
    }
}
