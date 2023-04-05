package it.unibo.photocircuit.circuit_model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Node {
    private int x;
    private int y;
    private final List<Segment> segments = new ArrayList<>();

    public Node(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void addSegment(Segment segment) {
        segments.add(segment);
    }

    public boolean removeSegment(Segment segment) {
        return segments.remove(segment);
    }

    public Segment getSegment(int i) {
        return segments.get(i);
    }

    public Segment getSegmentOnDirection(int dir) {
        for (Segment s : segments) {
            Node otherNode = s.getOtherNode(this);
            if (otherNode == null)
                continue;

            int foundDir;
            if (s.isVertical()) {
                if (otherNode.getY() > this.getY())
                    foundDir = 3;
                else
                    foundDir = 1;
            } else {
                if (otherNode.getX() > this.getX())
                    foundDir = 0;
                else
                    foundDir = 2;
            }

            if (foundDir == dir)
                return s;
        }
        return null;
    }

    public Node getNodeOnDirection(int dir) {
        Segment s = getSegmentOnDirection(dir);
        if (s == null)
            return null;
        return s.getOtherNode(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return x == node.x && y == node.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
