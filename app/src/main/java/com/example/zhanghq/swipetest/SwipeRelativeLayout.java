package com.example.zhanghq.swipetest;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;
import com.example.zhanghq.swipetest.SwipeLayoutGestrueDetector.AnimateType;

/**
 * Created by Administrator on 2015/6/26 0026.
 */
public class SwipeRelativeLayout extends RelativeLayout {
    private SwipeLayoutGestrueDetector gestureDetector;
    private Context mContext;

    private View contentView;
    private View menuView;

    private boolean isShowingMenu;
    private boolean blockDetect;

    public SwipeRelativeLayout(Context context) {
        super(context);
        mContext = context;
        gestureDetector = new SwipeLayoutGestrueDetector(mContext, new MyGestureListener());
    }

    public SwipeRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        gestureDetector = new SwipeLayoutGestrueDetector(mContext, new MyGestureListener());
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() == 2) {
            contentView = getChildAt(0);
            menuView = getChildAt(1);
        } else {
            blockDetect = true;
        }
    }

    private boolean doAnimation;

    private float currentDelta;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {

        if (blockDetect) {
            return super.onInterceptTouchEvent(motionEvent);
        } else {
            return gestureDetector.onTouchEvent(motionEvent);
        }
    }

    public void HideMenu() {
        ObjectAnimator animator1 = ObjectAnimator.ofFloat(contentView, "translationX", currentDelta, 0);
        ObjectAnimator animator2 = ObjectAnimator.ofFloat(menuView, "translationX", currentDelta, 0);
        AnimatorSet set = new AnimatorSet();
        set.setInterpolator(new DecelerateInterpolator());
        set.setDuration(200);
        set.playTogether(animator1,animator2);
        set.start();
    }

    private void doAnimation(AnimateType animateType) {
        if (animateType==AnimateType.Show) {
            ObjectAnimator animator1 = ObjectAnimator.ofFloat(contentView, "translationX", currentDelta, -menuView.getWidth());
            ObjectAnimator animator2 = ObjectAnimator.ofFloat(menuView, "translationX", currentDelta, -menuView.getWidth());
            AnimatorSet set = new AnimatorSet();
            set.setDuration(300);
            set.setInterpolator(new DecelerateInterpolator());
            set.playTogether(animator1,animator2);
            set.start();
            isShowingMenu = true;
        } else if (animateType==AnimateType.Hide) {
            ObjectAnimator animator1 = ObjectAnimator.ofFloat(contentView, "translationX", currentDelta, 0);
            ObjectAnimator animator2 = ObjectAnimator.ofFloat(menuView, "translationX", currentDelta, 0);
            AnimatorSet set = new AnimatorSet();
            set.setInterpolator(new DecelerateInterpolator());
            set.setDuration(300);
            set.playTogether(animator1,animator2);
            set.start();
        }
    }

    public boolean isShowingMenu() {
        return isShowingMenu;
    }


    class MyGestureListener implements SwipeLayoutGestureListener{

        @Override
        public boolean onDown(MotionEvent e) {
            Log.e("MyGestureListener", "onDown");
            doAnimation = false;
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            Log.e("MyGestureListener", "onScroll, distanceX="+distanceX+", distanceY="+distanceY);
            if (Math.abs(distanceX) > Math.abs(distanceY)) {
                currentDelta = -distanceX+contentView.getTranslationX();
                if (currentDelta < -menuView.getWidth()) {
                    currentDelta = -menuView.getWidth();
                }
                if (currentDelta > 0) {
                    currentDelta = 0;
                }
                Log.e("MyGestureListener", "onScroll, currentDelta="+currentDelta);
                doAnimation = true;
                contentView.setTranslationX(currentDelta);
                menuView.setTranslationX(currentDelta);
            }
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                               float velocityY) {
            Log.e("MyGestureListener", "onFling, velocityX="+velocityX+", velocityY="+velocityY);
            if (Math.abs(velocityX) > Math.abs(velocityY)) {
                if (currentDelta < -menuView.getWidth()) {
                    currentDelta = -menuView.getWidth();
                }
                if (currentDelta > 0) {
                    currentDelta = 0;
                }
                doAnimation = true;
                AnimateType type;
                if (velocityX>0) {
                    type = AnimateType.Hide;
                } else {
                    type = AnimateType.Show;
                }
                doAnimation(type);
            }
            if (currentDelta <= -menuView.getWidth()) {
                isShowingMenu = true;
            }
            return true;
        }

        @Override
        public boolean onUp(MotionEvent event) {
            if (currentDelta <= -menuView.getWidth()) {
                isShowingMenu = true;
            }
            Log.e("MyGestureListener", "onUp");
            if (doAnimation) {
                AnimateType type;
                if (Math.abs(currentDelta) > menuView.getWidth() / 2) {
                    type = AnimateType.Show;
                } else {
                    type = AnimateType.Hide;
                }
                doAnimation(type);
                return true;
            }
            return false;
        }

    }
}
