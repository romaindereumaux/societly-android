package com.lorentzos.flingswipe;

import android.graphics.Point;

import ee.mobi.scrolls.Log;

/**
 * Created by Lauris Kruusamäe on 21/09/15.
 * A class that implements a Triangle shape.
 * Has the ability to see if a point (x, y) is within the bounds of the triangle
 */
public class Triangle {

    private static final boolean DEBUG = false;
    private Log log = Log.getInstance(this);

    private Point left;
    private Point top;
    private Point right;
    private boolean upsideDown;

    private LinearRegression leftSide;
    private LinearRegression rightSide;
    private LinearRegression bottomSide;

    public Triangle(Point left, Point top, Point right) {
        this.left = left;
        this.top = top;
        this.right = right;

        upsideDown = (top.y < Math.max(left.y, right.y));
        leftSide = new LinearRegression(
                new float[] {intToFloat(left.x), intToFloat(top.x)},
                new float[] {intToFloat(left.y), intToFloat(top.y)}
        );
        rightSide = new LinearRegression(
                new float[] {intToFloat(top.x), intToFloat(right.x)},
                new float[] {intToFloat(top.y), intToFloat(right.y)}
        );
        bottomSide = new LinearRegression(
                new float[] {intToFloat(left.x), intToFloat(right.x)},
                new float[] {intToFloat(left.y), intToFloat(right.y)}
        );
    }

    /*
    Calculates the predicted y bounds for each side of the triangle at point x
    Then checks if the given y value is within the bounds of the predictions
     */
    public boolean contains(double x, double y) {

        double yLeft = leftSide.predict(x);
        double yRight = rightSide.predict(x);
        double yBottom = bottomSide.predict(x);

        log("contains() called");
        log("Point: " + x + "; " + y);
        log("Triangle: " + this.toString());
        log("yLeft: " + yLeft);
        log("yRight: " + yRight);
        log("yBottom: " + yBottom);

        if (upsideDown) {
            if (y > yBottom) {
                log("Out of bottom bounds");
                return false;
            } else if (y < yLeft) {
                log("Out of left bounds");
                return false;
            } else if (y < yRight) {
                log("Out of right bounds");
                return false;
            }
        } else {
            if (y < yBottom) {
                log("Out of bottom bounds");
                return false;
            } else if (y > yLeft) {
                log("Out of left bounds");
                return false;
            } else if (y > yRight) {
                log("Out of right bounds");
                return false;
            }
        }

        return true;
    }

    private float intToFloat(int integer) {
        return integer * 1.0f;
    }

    private void log(String message) {
        if (DEBUG) {
            log.d(message);
        }
    }

    @Override
    public String toString() {
        return "Triangle{" +
                "upsideDown=" + upsideDown +
                "left=" + left +
                ", top=" + top +
                ", right=" + right +
                '}';
    }
}
