package com.tanhd.rollingclass.views;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ScoreView extends LinearLayout {
    public static interface ScoreListener {
        void onClick(String score);
    }
    private ScoreListener mListener;
    private boolean mEnabled = true;

    public ScoreView(Context context) {
        super(context);
    }

    public ScoreView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScoreView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ScoreView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        for (int i=0; i<=5; i++) {
            findViewById(getResId("score" + i)).setOnClickListener(mOnClickListener);
        }
    }

    public void setListener(ScoreListener listener) {
        mListener = listener;
    }

    public int getResId(String name) {
        Resources resources = getContext().getResources();
        int id = resources.getIdentifier(name, "id", getContext().getPackageName());
        return id;
    }

    public void setScore(String score) {
        for (int i=0; i<=5; i++) {
            TextView view = findViewById(getResId("score" + i));
            String v = view.getText().toString();
            if (!score.equals(v)) {
                view.setBackgroundColor(0x00000000);
                view.setTextColor(0xFF000000);
            } else {
                view.setBackgroundColor(0xFF0077FF);
                view.setTextColor(0xFFFFFFFF);
            }
        }
    }

    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!mEnabled)
                return;

            TextView view = (TextView) v;
            String score = view.getText().toString();
            setScore(score);

            if (mListener != null)
                mListener.onClick(score);
        }
    };
}
