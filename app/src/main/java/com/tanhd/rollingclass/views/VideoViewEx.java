package com.tanhd.rollingclass.views;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.widget.VideoView;

public class VideoViewEx extends VideoView {
    private int mStartPosition;
    private long mStartTime;
    private MediaPlayer.OnCompletionListener mOnCompletionListener;

    public VideoViewEx(Context context) {
        super(context);
    }

    public VideoViewEx(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoViewEx(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void start() {
        mStartPosition = getCurrentPosition();
        mStartTime = System.currentTimeMillis();
        super.start();
    }

    public int getStartPosition() {
        return mStartPosition;
    }

    public long getStartTime() {
        return mStartTime;
    }

    @Override
    public void pause() {
        super.pause();
        if (mOnCompletionListener != null)
            mOnCompletionListener.onCompletion(null);
    }

    @Override
    public void setOnCompletionListener(MediaPlayer.OnCompletionListener l) {
        super.setOnCompletionListener(l);
        mOnCompletionListener = l;
    }
}
