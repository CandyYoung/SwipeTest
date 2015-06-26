package com.example.zhanghq.swipetest;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;

/**
 * Created by Administrator on 2015/6/26 0026.
 */
public class SwipeLayoutGestrueDetector {

    public enum AnimateType {
        Hide,
        Show,
    }

    private Context mContext;

    private int mTouchSlopSquare;
    private int mMinimumFlingVelocity;
    private int mMaximumFlingVelocity;

    // private final Handler mHandler;
    private final SwipeLayoutGestureListener mListener;

    private boolean mAlwaysInTapRegion;

    private MotionEvent mCurrentDownEvent;
    private MotionEvent mPreviousUpEvent;

    private float mLastFocusX;
    private float mLastFocusY;
    private float mDownFocusX;
    private float mDownFocusY;


    /**
     * Determines speed during touch scrolling
     */
    private VelocityTracker mVelocityTracker;

    public SwipeLayoutGestrueDetector(Context context,
                                      SwipeLayoutGestureListener listener) {
        mContext = context;
        mListener = listener;
        init();
    }

    private void init() {
        if (mListener == null) {
            throw new NullPointerException("OnGestureListener must not be null");
        }

        // Fallback to support pre-donuts releases
        int touchSlop, doubleTapSlop, doubleTapTouchSlop;
        if (mContext == null) {
            // noinspection deprecation
            touchSlop = ViewConfiguration.getTouchSlop();
            doubleTapTouchSlop = touchSlop; // Hack rather than adding a hiden
            // method for this
            // doubleTapSlop = ViewConfiguration.getDoubleTapSlop();
            // noinspection deprecation
            mMinimumFlingVelocity = ViewConfiguration.getMinimumFlingVelocity();
            mMaximumFlingVelocity = ViewConfiguration.getMaximumFlingVelocity();
        } else {
            final ViewConfiguration configuration = ViewConfiguration
                    .get(mContext);
            touchSlop = configuration.getScaledTouchSlop();
            // doubleTapTouchSlop = configuration.getScaledDoubleTapTouchSlop();
            doubleTapSlop = configuration.getScaledDoubleTapSlop();
            mMinimumFlingVelocity = configuration
                    .getScaledMinimumFlingVelocity();
            mMaximumFlingVelocity = configuration
                    .getScaledMaximumFlingVelocity();
        }
        mTouchSlopSquare = touchSlop * touchSlop;
    }

    public boolean onTouchEvent(MotionEvent ev) {

        Log.d("SwipeLayoutGestrueDetector", "onTouchEvent");
        final int action = ev.getAction();

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);

        final boolean pointerUp = (action & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_UP;
        final int skipIndex = pointerUp ? ev.getActionIndex() : -1;

        // Determine focal point
        float sumX = 0, sumY = 0;
        final int count = ev.getPointerCount();
        for (int i = 0; i < count; i++) {
            if (skipIndex == i)
                continue;
            sumX += ev.getX(i);
            sumY += ev.getY(i);
        }
        final int div = pointerUp ? count - 1 : count;
        final float focusX = sumX / div;
        final float focusY = sumY / div;

        boolean handled = false;

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_POINTER_DOWN:
                mDownFocusX = mLastFocusX = focusX;
                mDownFocusY = mLastFocusY = focusY;
                // Cancel long press and taps
                cancelTaps();
                break;

            case MotionEvent.ACTION_POINTER_UP:
                mDownFocusX = mLastFocusX = focusX;
                mDownFocusY = mLastFocusY = focusY;

                // Check the dot product of current velocities.
                // If the pointer that left was opposing another velocity vector,
                // clear.
                mVelocityTracker
                        .computeCurrentVelocity(1000, mMaximumFlingVelocity);
                final int upIndex = ev.getActionIndex();
                final int id1 = ev.getPointerId(upIndex);
                final float x1 = mVelocityTracker.getXVelocity(id1);
                final float y1 = mVelocityTracker.getYVelocity(id1);
                for (int i = 0; i < count; i++) {
                    if (i == upIndex)
                        continue;

                    final int id2 = ev.getPointerId(i);
                    final float x = x1 * mVelocityTracker.getXVelocity(id2);
                    final float y = y1 * mVelocityTracker.getYVelocity(id2);

                    final float dot = x + y;
                    if (dot < 0) {
                        mVelocityTracker.clear();
                        break;
                    }
                }
                break;

            case MotionEvent.ACTION_DOWN:
                Log.e("GestureDetector", "MotionEvent.ACTION_DOWN");

                mDownFocusX = mLastFocusX = focusX;
                mDownFocusY = mLastFocusY = focusY;
                if (mCurrentDownEvent != null) {
                    mCurrentDownEvent.recycle();
                }
                mCurrentDownEvent = MotionEvent.obtain(ev);
                mAlwaysInTapRegion = true;

                handled |= mListener.onDown(ev);
                break;

            case MotionEvent.ACTION_MOVE:
                Log.e("GestureDetector", "MotionEvent.ACTION_MOVE");
                final float scrollX = mLastFocusX - focusX;
                final float scrollY = mLastFocusY - focusY;
                if (mAlwaysInTapRegion) {
                    final int deltaX = (int) (focusX - mDownFocusX);
                    final int deltaY = (int) (focusY - mDownFocusY);
                    int distance = (deltaX * deltaX) + (deltaY * deltaY);
                    if (distance > mTouchSlopSquare) {
                        handled = mListener.onScroll(mCurrentDownEvent, ev,
                                scrollX, scrollY);
                        mLastFocusX = focusX;
                        mLastFocusY = focusY;
                        mAlwaysInTapRegion = false;
                    }
                } else if ((Math.abs(scrollX) >= 1) || (Math.abs(scrollY) >= 1)) {
                    handled = mListener.onScroll(mCurrentDownEvent, ev, scrollX,
                            scrollY);
                    mLastFocusX = focusX;
                    mLastFocusY = focusY;
                }
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                Log.e("GestureDetector", "MotionEvent.ACTION_UP");
                MotionEvent currentUpEvent = MotionEvent.obtain(ev);

                // A fling must travel the minimum tap distance
                final VelocityTracker velocityTracker = mVelocityTracker;
                final int pointerId = ev.getPointerId(0);
                velocityTracker.computeCurrentVelocity(1000, mMaximumFlingVelocity);
                final float velocityY = velocityTracker.getYVelocity(pointerId);
                final float velocityX = velocityTracker.getXVelocity(pointerId);

                if ((Math.abs(velocityY) > mMinimumFlingVelocity)
                        || (Math.abs(velocityX) > mMinimumFlingVelocity)) {
                    handled = mListener.onFling(mCurrentDownEvent, ev, velocityX,
                            velocityY);
                } else {
                    handled = mListener.onUp(ev);
                }
                if (mPreviousUpEvent != null) {
                    mPreviousUpEvent.recycle();
                }
                // Hold the event we obtained above - listeners may have changed the
                // original.
                mPreviousUpEvent = currentUpEvent;
//			if (mVelocityTracker != null) {
//				// This may have been cleared when we called out to the
//				// application above.
//				mVelocityTracker.recycle();
//				mVelocityTracker = null;
//			}
                cancel();
                break;

//		case MotionEvent.ACTION_CANCEL:
//			Log.e("GestureDetector", "MotionEvent.ACTION_CANCEL");
//			cancel();
//			break;
        }

        return handled;
    }

    private void cancel() {
        if (mVelocityTracker!=null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
        mAlwaysInTapRegion = false;
    }

    private void cancelTaps() {
        mAlwaysInTapRegion = false;
    }
}
