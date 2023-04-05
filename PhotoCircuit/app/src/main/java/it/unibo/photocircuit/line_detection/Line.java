package it.unibo.photocircuit.line_detection;

public class Line {
    public int x0;
    public int y0;
    public int x1;
    public int y1;

    public Line(int x0, int y0, int x1, int y1) {
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
    }

    public Line(double[] houghArray) {
        this.x0 = (int)houghArray[0];
        this.y0 = (int)houghArray[1];
        this.x1 = (int)houghArray[2];
        this.y1 = (int)houghArray[3];
    }
}
