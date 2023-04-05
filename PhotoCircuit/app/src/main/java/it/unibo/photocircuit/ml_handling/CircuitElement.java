package it.unibo.photocircuit.ml_handling;

import org.opencv.core.Rect;

public class CircuitElement {
    private float[] probabilities;
    private int bestGuessIndex;
    private String bestGuess;
    private Rect rect;

    public CircuitElement(float[] probabilities, int bestGuessIndex, String bestGuess, Rect rect) {
        this.probabilities = probabilities;
        this.bestGuessIndex = bestGuessIndex;
        this.bestGuess = bestGuess;
        this.rect = rect;
    }

    public float[] getProbabilities() {
        return probabilities;
    }

    public void setProbabilities(float[] probabilities) {
        this.probabilities = probabilities;
    }

    public int getBestGuessIndex() {
        return bestGuessIndex;
    }

    public void setBestGuessIndex(int bestGuessIndex) {
        this.bestGuessIndex = bestGuessIndex;
    }

    public String getBestGuess() {
        return bestGuess;
    }

    public void setBestGuess(String bestGuess) {
        this.bestGuess = bestGuess;
    }

    public Rect getRect() {
        return rect;
    }

    public void setRect(Rect rect) {
        this.rect = rect;
    }
}
