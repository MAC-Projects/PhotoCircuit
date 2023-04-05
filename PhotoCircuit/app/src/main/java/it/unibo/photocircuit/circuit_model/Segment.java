package it.unibo.photocircuit.circuit_model;

import java.util.Objects;

public class Segment {
    private Node node1;
    private Node node2;

    public Segment(Node node1, Node node2) {
        this.node1 = node1;
        this.node2 = node2;
    }

    public Node getNode1() {
        return node1;
    }

    public void setNode1(Node node1) {
        this.node1 = node1;
    }

    public Node getNode2() {
        return node2;
    }

    public Node getOtherNode(Node node) {
        if (node == node1)
            return node2;
        else
            return node1;
    }

    public void setNode2(Node node2) {
        this.node2 = node2;
    }

    public String getCircuiTikZLabel() {
        return "short";
    }

    public String getCircuiTikZDraw() {
        int x1 = node1.getX();
        int y1 = node1.getY();
        int x2 = node2.getX();
        int y2 = node2.getY();
        String node1Str = "(" + x1 + ", " + y1 + ")";
        String node2Str = "(" + x2 + ", " + y2 + ")";
        return "\\draw " + node1Str + " to [short] " + node2Str + ";\n";
    }

    public boolean isVertical() {
        if (node1 == null || node2 == null)
            return true;
        int x1 = node1.getX();
        int y1 = node1.getY();
        int x2 = node2.getX();
        int y2 = node2.getY();

        int deltaX = Math.abs(x1 - x2);
        int deltaY = Math.abs(y1 - y2);

        return deltaX < deltaY;
    }

    public boolean isHorizontal() {
        return !isVertical();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Segment segment = (Segment) o;
        return Objects.equals(node1, segment.node1) && Objects.equals(node2, segment.node2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(node1, node2);
    }
}
