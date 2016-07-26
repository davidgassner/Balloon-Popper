package com.davidgassner.android.balloonpopper;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.davidgassner.android.balloonpopper.utils.PixelHelper;

public class Balloon extends ImageView
        implements View.OnTouchListener,
        Animator.AnimatorListener,
        ValueAnimator.AnimatorUpdateListener {

    public static final String TAG = "Balloon";

    private BalloonListener mListener;
    private ValueAnimator mAnimator;
    private boolean mPopped;

    public Balloon(Context context) {
        super(context);
    }

    public Balloon(Context context, int color, int rawHeight, int level) {
        super(context);

        this.mListener = (BalloonListener) context;

        this.setImageResource(R.drawable.balloon);
        this.setColorFilter(color);

        int rawWidth = rawHeight / 2;

//      Calc balloon height and width as dp
        int dpHeight = PixelHelper.pixelsToDp(rawHeight, context);
        int dpWidth = PixelHelper.pixelsToDp(rawWidth, context);
        ViewGroup.LayoutParams params =
                new ViewGroup.LayoutParams(dpWidth, dpHeight);
        setLayoutParams(params);

        setOnTouchListener(this);
    }

    public void releaseBalloon(int screenHeight, int duration) {
        mAnimator = new ValueAnimator();
        mAnimator.setDuration(duration);
        mAnimator.setFloatValues(screenHeight, 0f);
        mAnimator.setInterpolator(new LinearInterpolator());
        mAnimator.setTarget(this);
        mAnimator.addListener(this);
        mAnimator.addUpdateListener(this);
        mAnimator.start();
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        if (!mPopped) {
            setY((Float) animation.getAnimatedValue());
        }
    }

    public interface BalloonListener {
        void popBalloon(Balloon balloon, boolean touched);
    }

    @Override
    public void onAnimationStart(Animator animation) {
    }

    @Override
    public void onAnimationEnd(Animator animation) {
//      This means the balloon got to the top of the screen
        if (!mPopped) {
            mListener.popBalloon(this, false);
        }
    }

    @Override
    public void onAnimationCancel(Animator animation) {

    }

    @Override
    public void onAnimationRepeat(Animator animation) {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
//      Call the activity's popBalloon() method
//      Cancel the animation so the ValueAnimator doesn't keep going
//      Flip the popped flag
        if (!mPopped && event.getAction() == MotionEvent.ACTION_DOWN) {
            mListener.popBalloon(this, true);
            mPopped = true;
            mAnimator.cancel();
        }
        return true;
    }

    public void setPopped(boolean popped) {
        this.mPopped = popped;
    }

}
