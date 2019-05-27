package com.tanhd.rollingclass.fragments.pages;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.server.data.QuestionData;
import com.tanhd.rollingclass.utils.ResultClass;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ScantronAnswerPage extends Fragment {
    private static class Args implements Serializable {
        public HashMap<Integer, ResultClass> resultList;
    }

    public static interface ScantronListener {
        void onScrollToPage(int page);
        void onCommintAnswer();
    }

    private GridView mGridView;
    private ItemAdapter mAdapter;
    private HashMap<Integer, ResultClass> mResultList;
    private ScantronListener mListener;

    public static ScantronAnswerPage newInstance(HashMap<Integer, ResultClass> resultList, ScantronListener listener) {
        Args data = new Args();
        data.resultList = resultList;

        Bundle args = new Bundle();
        args.putSerializable("args", data);
        ScantronAnswerPage page = new ScantronAnswerPage();
        page.setArguments(args);
        page.setListener(listener);
        return page;
    }

    private void setListener(ScantronListener listener) {
        mListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Args args = (Args) getArguments().get("args");
        mResultList = args.resultList;
        View view = inflater.inflate(R.layout.page_scantron_answer, container, false);
        mGridView = view.findViewById(R.id.grid_view);
        view.findViewById(R.id.submit_result).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onCommintAnswer();
            }
        });
        mAdapter = new ItemAdapter();
        mGridView.setAdapter(mAdapter);
        return view;
    }

    public void refresh() {
        if (mAdapter != null)
            mAdapter.notifyDataSetChanged();
    }

    private class ItemAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mResultList.size();
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
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.item_subjective_answer, null);
            }

            final String title = String.valueOf(position + 1);
            TextView titleView = view.findViewById(R.id.title);
            titleView.setText(title);

            ResultClass resultClass = mResultList.get(position);

            if (!resultClass.isEmpty()) {
                titleView.setTextColor(getResources().getColor(R.color.white));
                titleView.setBackgroundResource(R.drawable.circle_checked_background);
            } else {
                titleView.setTextColor(getResources().getColor(R.color.black));
                titleView.setBackgroundResource(R.drawable.circle_uncheck_background);
            }

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onScrollToPage(position);
                }
            });

            return view;
        }
    }

}
