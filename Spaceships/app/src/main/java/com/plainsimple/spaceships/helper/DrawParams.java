package com.plainsimple.spaceships.helper;

/**
 * Stores parameters for drawing portions of bitmaps.
 */
public class DrawParams {

    // ID of bitmap to be drawn
    private BitmapResource bitmapID;
    // x-coordinate where drawing begins on canvas
    private float canvasX0;
    // y-coordinate where drawing begins on canvas
    private float canvasY0;
    // starting x-coordinate // todo: x- and y- coordinates to begin drawing on canvas
    private float x0;
    // starting y-coordinate
    private float y0;
    // ending x-coordinate
    private float x1;
    // ending y-coordinate
    private float y1;

    public DrawParams(BitmapResource bitmapID, float canvasX0, float canvasY0, float x0, float y0, float x1, float y1) { // todo: setParams method to set all params at once
        this.bitmapID = bitmapID;
        this.canvasX0 = canvasX0;
        this.canvasY0 = canvasY0;
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
    }

    public BitmapResource getBitmapID() {
        return bitmapID;
    }

    public void setBitmapID(BitmapResource bitmapID) {
        this.bitmapID = bitmapID;
    }

    public float getCanvasX0() {
        return canvasX0;
    }

    public void setCanvasX0(float canvasX0) {
        this.canvasX0 = canvasX0;
    }

    public float getCanvasY0() {
        return canvasY0;
    }

    public void setCanvasY0(float canvasY0) {
        this.canvasY0 = canvasY0;
    }

    public float getX0() {
        return x0;
    }

    public void setX0(float x0) {
        this.x0 = x0;
    }

    public float getY0() {
        return y0;
    }

    public void setY0(float y0) {
        this.y0 = y0;
    }

    public float getX1() {
        return x1;
    }

    public void setX1(float x1) {
        this.x1 = x1;
    }

    public float getY1() {
        return y1;
    }

    public void setY1(float y1) {
        this.y1 = y1;
    }
}
