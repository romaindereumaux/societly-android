package com.lorentzos.flingswipe;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;

/**
 * Created by dionysis_lorentzos on 5/8/14
 * for package com.lorentzos.swipecards
 * and project Swipe cards.
 * Use with caution dinausaurs might appear!
 *
 * Changed by Lauris Kruusamäe on 9/22/15 to
 * add support for swiping up/down and movement
 * areas withing the fling container
 */


public class FlingCardListener implements View.OnTouchListener {

    private static final String TAG = FlingCardListener.class.getSimpleName();
    private static final int INVALID_POINTER_ID = -1;

    private static final int POSITION_TOP = 1;
    private static final int POSITION_RIGHT = 2;
    private static final int POSITION_BOTTOM = 3;
    private static final int POSITION_LEFT = 4;

    private final float objectX;
    private final float objectY;
    private final float xAdjust;
    private final float yAdjust;
    private final int objectH;
    private final int objectW;
    private final int parentWidth;
    private final int parentHeight;
    private final int centerHalfEdge;
    private final FlingListener mFlingListener;
    private final Object dataObject;
    private float BASE_ROTATION_DEGREES;

    private float aPosX;
    private float aPosY;
    private float aDownTouchX;
    private float aDownTouchY;

    // The active pointer is the one currently moving our object.
    private int mActivePointerId = INVALID_POINTER_ID;
    private View frame = null;

    private final int TOUCH_ABOVE = 0;
    private final int TOUCH_BELOW = 1;
    private int touchPosition;
    private final Object obj = new Object();
    private boolean isAnimationRunning = false;
    private float MAX_COS = (float) Math.cos(Math.toRadians(45));
    private int activeArea;


    public FlingCardListener(View frame, Object itemAtPosition, FlingListener flingListener) {
        this(frame, itemAtPosition, 15f, flingListener);
    }

    public FlingCardListener(View frame, Object itemAtPosition, float rotation_degrees, FlingListener flingListener) {
        super();
        this.frame = frame;
        this.objectX = frame.getX();
        this.objectY = frame.getY();
        this.objectH = frame.getHeight();
        this.objectW = frame.getWidth();
        this.dataObject = itemAtPosition;
        this.parentWidth = ((ViewGroup) frame.getParent()).getWidth();
        this.parentHeight = ((ViewGroup) frame.getParent()).getHeight();
        this.centerHalfEdge = Math.round(parentWidth * 0.12f);
        this.BASE_ROTATION_DEGREES = rotation_degrees;
        this.mFlingListener = flingListener;
        this.activeArea = SwipeFlingAdapterView.onFlingListener.AREA_OUT_OF_BOUNDS;

        this.yAdjust = parentHeight / 2.f - objectY;
        this.xAdjust = parentWidth / 2.f - objectX;
    }


    public boolean onTouch(View view, MotionEvent event) {

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:

                // from http://android-developers.blogspot.com/2010/06/making-sense-of-multitouch.html
                // Save the ID of this pointer

                mActivePointerId = event.getPointerId(0);
                float x = 0;
                float y = 0;
                boolean success = false;
                try {
                    x = event.getX(mActivePointerId);
                    y = event.getY(mActivePointerId);
                    success = true;
                } catch (IllegalArgumentException e) {
                    Log.w(TAG, "Exception in onTouch(view, event) : " + mActivePointerId, e);
                }
                if (success) {
                    // Remember where we started
                    aDownTouchX = x;
                    aDownTouchY = y;
                    //to prevent an initial jump of the magnifier, aposX and aPosY must
                    //have the values from the magnifier frame
                    if (aPosX == 0) {
                        aPosX = frame.getX();
                    }
                    if (aPosY == 0) {
                        aPosY = frame.getY();
                    }

                    if (y < objectH / 2) {
                        touchPosition = TOUCH_ABOVE;
                    } else {
                        touchPosition = TOUCH_BELOW;
                    }
                }
                requestDisallowInterceptTouchEventIfAvailable(view, true);
                break;

            case MotionEvent.ACTION_UP:
                mActivePointerId = INVALID_POINTER_ID;
                resetCardViewOnStack();
                requestDisallowInterceptTouchEventIfAvailable(view, true);
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                break;

            case MotionEvent.ACTION_POINTER_UP:
                // Extract the index of the pointer that left the touch sensor
                final int pointerIndex = (event.getAction() &
                        MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                final int pointerId = event.getPointerId(pointerIndex);
                if (pointerId == mActivePointerId) {
                    // This was our active pointer going up. Choose a new
                    // active pointer and adjust accordingly.
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mActivePointerId = event.getPointerId(newPointerIndex);
                }
                break;
            case MotionEvent.ACTION_MOVE:

                // Find the index of the active pointer and fetch its position
                final int pointerIndexMove = event.findPointerIndex(mActivePointerId);
                final float xMove = event.getX(pointerIndexMove);
                final float yMove = event.getY(pointerIndexMove);

                //from http://android-developers.blogspot.com/2010/06/making-sense-of-multitouch.html
                // Calculate the distance moved
                final float dx = xMove - aDownTouchX;
                final float dy = yMove - aDownTouchY;

                // Move the frame
                aPosX += dx;
                aPosY += dy;

                // calculate the rotation degrees
                float distobjectX = aPosX - objectX;
                float rotation = BASE_ROTATION_DEGREES * 2.f * distobjectX / parentWidth;
                if (touchPosition == TOUCH_BELOW) {
                    rotation = -rotation;
                }

                //in this area would be code for doing something with the view as the frame moves.
                frame.setX(aPosX);
                frame.setY(aPosY);
                frame.setRotation(rotation);
                notifyAreaChanged();
                break;

            case MotionEvent.ACTION_CANCEL: {
                mActivePointerId = INVALID_POINTER_ID;
                requestDisallowInterceptTouchEventIfAvailable(view, false);
                break;
            }
        }

        return true;
    }

    private void requestDisallowInterceptTouchEventIfAvailable(View view, boolean value) {
        ViewParent parent = view.getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(value);
        }
    }

    /**
     * Returns border points in the order: [leftTop, rightTop, rightBottom, leftBottom, center]
     */
    private Point[] getBorderPoints() {
        int leftX = 0;
        int rightX = parentWidth;
        int topY = 0;
        int bottomY = parentHeight;

        int centerX = leftX + (rightX - leftX) / 2;
        int centerY = topY + (bottomY - topY) / 2;

        return new Point[] {
                new Point(leftX, topY), new Point(rightX, topY),        // Top row
                new Point(rightX, bottomY), new Point(leftX, bottomY),  // Bottom row
                new Point(centerX, centerY)                             // Center
        };
    }

    @SwipeFlingAdapterView.onFlingListener.Area
    private int getViewArea() {
        Point[] points = getBorderPoints();
        Point leftTop = points[0];
        Point rightTop = points[1];
        Point rightBottom = points[2];
        Point leftBottom = points[3];
        Point middle = points[4];

        float x = getX();
        float y = getY();
        Rect centerArea = new Rect(middle.x - centerHalfEdge, middle.y - centerHalfEdge, middle.x + centerHalfEdge, middle.y + centerHalfEdge);
        log("Point leftTop=" + leftTop + " rightTop=" + rightTop + " rightBottom=" + rightBottom + " leftBotton=" + leftBottom + " middle=" + middle);
        log("Item=(" + x + ";" + y + ") center=" + centerArea + " oH=" + objectH + " oW=" + objectW);

        Triangle topArea = new Triangle(leftTop, middle, rightTop);
        Triangle rightArea = new Triangle(middle, rightTop, rightBottom);
        Triangle bottomArea = new Triangle(leftBottom, middle, rightBottom);
        Triangle leftArea = new Triangle(leftBottom, leftTop, middle);

        if (centerArea.contains(Math.round(x), Math.round(y))) {
            return SwipeFlingAdapterView.onFlingListener.AREA_CENTER;
        } else if (topArea.contains(x, y)) {
            return SwipeFlingAdapterView.onFlingListener.AREA_TOP;
        } else if (rightArea.contains(x, y)) {
            return SwipeFlingAdapterView.onFlingListener.AREA_RIGHT;
        } else if (bottomArea.contains(x, y)) {
            return SwipeFlingAdapterView.onFlingListener.AREA_BOTTOM;
        } else if (leftArea.contains(x, y)) {
            return SwipeFlingAdapterView.onFlingListener.AREA_LEFT;
        } else {
            return SwipeFlingAdapterView.onFlingListener.AREA_OUT_OF_BOUNDS;
        }
    }

    private void notifyMovePercentage(@SwipeFlingAdapterView.onFlingListener.Area int area) {
        float distanceFromCenter = 0;
        float distanceEdgeToCenter = 0;

        Point center = getBorderPoints()[4];
        float xEdgeToCenter = center.x - leftBorder() - centerHalfEdge;
        float yEdgeToCenter = center.y - topBorder() - centerHalfEdge;
        switch (area) {
            case SwipeFlingAdapterView.onFlingListener.AREA_LEFT:
                // Left position distance from left edge
                log("notifyMovePercentage AREA_LEFT distFromCenter=" + center.x + " - " + centerHalfEdge + " - " + getX());
                distanceEdgeToCenter = xEdgeToCenter;
                distanceFromCenter = center.x - centerHalfEdge - getX();
                break;
            case SwipeFlingAdapterView.onFlingListener.AREA_RIGHT:
                // Right position distance from left edge
                log("notifyMovePercentage AREA_RIGHT distFromCenter=" + getX() + " - (" + center.x + " + " + centerHalfEdge + ")");
                distanceEdgeToCenter = xEdgeToCenter;
                distanceFromCenter = getX() - (center.x + centerHalfEdge);
                break;
            case SwipeFlingAdapterView.onFlingListener.AREA_TOP:
                // Right position distance from left edge
                log("notifyMovePercentage AREA_TOP distFromCenter=" + center.y + " - " + centerHalfEdge + " - " + getY());
                distanceEdgeToCenter = yEdgeToCenter;
                distanceFromCenter = center.y - centerHalfEdge - getY();
                break;
            case SwipeFlingAdapterView.onFlingListener.AREA_BOTTOM:
                log("notifyMovePercentage AREA_BOTTOM distFromCenter=" + getY() + " - (" + center.y + " + " + centerHalfEdge + ")");
                distanceEdgeToCenter = yEdgeToCenter;
                distanceFromCenter = getY() - (center.y + centerHalfEdge);
                break;
        }

        log("distanceFromCenter= " + distanceFromCenter + " distanceEdgeToCenter=" + distanceEdgeToCenter);

        float percentage = Math.max(0, Math.min(1, distanceFromCenter / distanceEdgeToCenter));
        mFlingListener.onMove(area, percentage);
    }

    // We only set the newArea from here..
    @SuppressLint("WrongConstant")
    private void notifyAreaChanged() {
        int newArea = getViewArea();
        // calculate distance from the given area's edge
        if (newArea != activeArea && newArea != SwipeFlingAdapterView.onFlingListener.AREA_OUT_OF_BOUNDS) {
            // Ignore since we moved out of bounds
            mFlingListener.onAreaChanged(activeArea, newArea);
            activeArea = newArea;
        }
        notifyMovePercentage(newArea);
    }

    private boolean resetCardViewOnStack() {
        if (movedBeyondLeftBorder()) {
            // Left Swipe
            onSelected(POSITION_LEFT, getExitPoint(-objectW), 100);
        } else if (movedBeyondRightBorder()) {
            // Right Swipe
            onSelected(POSITION_RIGHT, getExitPoint(parentWidth), 100);
        } else if (movedBeyondTopBorder()) {
            onSelected(POSITION_TOP, -objectH, 100);
        } else if (movedBeyondBottomBorder()) {
            onSelected(POSITION_BOTTOM, parentHeight + objectH, 100);
        } else {
            float abslMoveDistance = Math.abs(aPosX - objectX);
            aPosX = objectX;
            aPosY = objectY;
            aDownTouchX = 0;
            aDownTouchY = 0;
            frame.animate()
                    .setDuration(200)
                    .setInterpolator(new OvershootInterpolator(1.5f))
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            notifyAreaChanged();
                        }
                    })
                    .x(objectX)
                    .y(objectY)
                    .rotation(0);
            if (abslMoveDistance < 4.0) {
                mFlingListener.onClick(dataObject);
            }
        }
        return false;
    }

    private boolean movedBeyondLeftBorder() {
        return getX() < leftBorder();
    }

    private boolean movedBeyondRightBorder() {
        return getX() > rightBorder();
    }

    private boolean movedBeyondTopBorder() {
        return getY() < topBorder();
    }

    private boolean movedBeyondBottomBorder() {
        return getY() > bottomBorder();
    }

    public float leftBorder() {
        return parentWidth / 5.f;
    }

    public float rightBorder() {
        return parentWidth - leftBorder();
    }

    public float topBorder() {
        // Adjust the border by frameY position, because we've manually added top margin to the card
        return parentHeight / 4.f;
    }

    public float bottomBorder() {
        return parentHeight - topBorder();
    }

    private float getX() {
        return aPosX + xAdjust;
    }

    private float getY() {
        return aPosY + yAdjust;
    }

    public void onSelected(final int position, float exitY, long duration) {

        isAnimationRunning = true;
        float exitX;
        if (position == POSITION_LEFT) {
            exitX = -objectW - getRotationWidthOffset();
        } else if (position == POSITION_RIGHT) {
            exitX = parentWidth + getRotationWidthOffset();
        } else {
            exitX = aPosX;
        }

        this.frame.animate()
                .setDuration(duration)
                .setInterpolator(new AccelerateInterpolator())
                .x(exitX)
                .y(exitY)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        switch (position) {
                            case POSITION_TOP:
                                mFlingListener.topExit(dataObject);
                                break;
                            case POSITION_RIGHT:
                                mFlingListener.rightExit(dataObject);
                                break;
                            case POSITION_BOTTOM:
                                mFlingListener.bottomExit(dataObject);
                                break;
                            case POSITION_LEFT:
                                mFlingListener.leftExit(dataObject);
                                break;
                        }
                        aPosX = objectX;
                        aPosY = objectY;
                        notifyAreaChanged();
                        isAnimationRunning = false;
                        mFlingListener.onCardExited();
                    }
                })
                .rotation(getExitRotation(position == POSITION_LEFT));
    }


    /**
     * Starts a default left exit animation.
     */
    public void selectLeft() {
        if (!isAnimationRunning)
            onSelected(POSITION_LEFT, objectY, 200);
    }

    /**
     * Starts a default right exit animation.
     */
    public void selectRight() {
        if (!isAnimationRunning)
            onSelected(POSITION_RIGHT, objectY, 200);
    }

    private float getExitPoint(int exitXPoint) {
        float[] x = new float[2];
        x[0] = objectX;
        x[1] = aPosX;

        float[] y = new float[2];
        y[0] = objectY;
        y[1] = aPosY;

        LinearRegression regression = new LinearRegression(x, y);

        //Your typical y = ax+b linear regression
        return (float) regression.slope() * exitXPoint + (float) regression.intercept();
    }

    private float getExitRotation(boolean isLeft) {
        float rotation = BASE_ROTATION_DEGREES * 2.f * (parentWidth - objectX) / parentWidth;
        if (touchPosition == TOUCH_BELOW) {
            rotation = -rotation;
        }
        if (isLeft) {
            rotation = -rotation;
        }
        return rotation;
    }


    /**
     * When the object rotates it's width becomes bigger.
     * The maximum width is at 45 degrees.
     * <p/>
     * The below method calculates the width offset of the rotation.
     */
    private float getRotationWidthOffset() {
        return objectW / MAX_COS - objectW;
    }


    public void setRotationDegrees(float degrees) {
        this.BASE_ROTATION_DEGREES = degrees;
    }

    public boolean isTouching() {
        return this.mActivePointerId != INVALID_POINTER_ID;
    }

    public PointF getLastPoint() {
        return new PointF(this.aPosX, this.aPosY);
    }

    private void log(String message) {
//        Log.v(TAG, message);
    }

    protected interface FlingListener {
        void onCardExited();

        void leftExit(Object dataObject);

        void rightExit(Object dataObject);

        void topExit(Object dataObject);

        void bottomExit(Object dataObject);

        void onClick(Object dataObject);

        void onAreaChanged(@SwipeFlingAdapterView.onFlingListener.Area int oldArea, @SwipeFlingAdapterView.onFlingListener.Area int newArea);

        void onMove(@SwipeFlingAdapterView.onFlingListener.Area int area, float percentage);
    }

}





