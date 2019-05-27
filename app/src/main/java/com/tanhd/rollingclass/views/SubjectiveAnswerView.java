package com.tanhd.rollingclass.views;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.utils.ResultClass;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class SubjectiveAnswerView extends LinearLayout {
    public static interface AnswerListener {
        void onNext();
    }

    private GridView mGridView;
    private OptionAdapter mAdapter;
    private HashSet<String> mResult = new HashSet<>();
    private List<String> mOptionList = new ArrayList<>();
    private boolean mIsSingle = true;
    private AnswerListener mListener;
    private ResultClass mResultClass;

    public SubjectiveAnswerView(Context context) {
        super(context);
    }

    public SubjectiveAnswerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SubjectiveAnswerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SubjectiveAnswerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setListener(AnswerListener listener) {
        mListener = listener;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mGridView = findViewById(R.id.grid_view);
        mAdapter = new OptionAdapter();
        mGridView.setAdapter(mAdapter);
    }

    public void setData(boolean isSingle, List<String> optionList, String text) {
        mIsSingle = isSingle;
        mOptionList = optionList;
        mResult.clear();
        if (text != null) {
            for (int i=0; i<text.length(); i++) {
                mResult.add(String.valueOf(text.charAt(i)));
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    public String getResult() {
        String result = "";
        for (String v: mResult) {
            result = result + v;
        }
        return result;
    }

    private class OptionAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mOptionList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = inflate(getContext(), R.layout.item_subjective_answer, null);
            }

            final String title = mOptionList.get(position);
            TextView titleView = view.findViewById(R.id.title);
            titleView.setText(title);

            if (mResult.contains(title)) {
                titleView.setTextColor(getResources().getColor(R.color.white));
                titleView.setBackgroundResource(R.drawable.circle_checked_background);
            } else {
                titleView.setTextColor(getResources().getColor(R.color.black));
                titleView.setBackgroundResource(R.drawable.circle_uncheck_background);
            }

            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mIsSingle)
                        mResult.clear();

                    if (mResult.contains(title)) {
                        mResult.remove(title);
                    } else {
                        mResult.add(title);
                        if (mListener != null)
                            mListener.onNext();
                    }

                    if (mResultClass != null) {
                        String text = "";
                        for (String r: mResult) {
                            text = text + r;
                        }
                        mResultClass.text = text;
                        mResultClass.mode = 1;
                    }

                    notifyDataSetChanged();
                }
            });

            return view;
        }
    }
}
