package com.tanhd.rollingclass.views;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.ClassData;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.MicroCourseData;

import java.util.List;

public class MicroCourseListView extends LinearLayout {
    public static interface MicroCourseListViewListener {
        void onSelectedItem(MicroCourseData microCourseData);
    }

    private ListView mListView;
    private List<MicroCourseData> mMicroCourseList;
    private String mSelMicroCourseID;
    private MicroCourseAdapter mAdapter;
    private MicroCourseListViewListener mListener;

    public MicroCourseListView(Context context) {
        super(context);
    }

    public MicroCourseListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MicroCourseListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mListView = findViewById(R.id.microcourse_list);
        new InitDataTask().execute();
    }

    public void setListener(MicroCourseListViewListener listener) {
        mListener = listener;
    }

    private class MicroCourseAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mMicroCourseList.size();
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
                view = LayoutInflater.from(getContext()).inflate(R.layout.item_microcourse_listview, parent, false);
            }
            TextView nameView = view.findViewById(R.id.name);
            final MicroCourseData itemData = mMicroCourseList.get(position);
            nameView.setText(itemData.MicroCourseName);

            if (itemData.MicroCourseID.equals(mSelMicroCourseID)) {
                nameView.setTextColor(getResources().getColor(R.color.white));
                view.setBackgroundColor(getResources().getColor(R.color.button_blue));
            } else {
                nameView.setTextColor(getResources().getColor(R.color.black));
                view.setBackground(null);
            }

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSelMicroCourseID = itemData.MicroCourseID;
                    if (mListener != null) {
                        mListener.onSelectedItem(itemData);
                    }
                    notifyDataSetChanged();
                }
            });

            if (mSelMicroCourseID == null && position == 0) {
                view.callOnClick();
            }
            return view;
        }
    }

    private class InitDataTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            ClassData classData = ExternalParam.getInstance().getClassData();
            if (classData == null) {
                return null;
            }
            List<MicroCourseData> sampleList = ScopeServer.getInstance().QureyMicroCourseByClassID(classData.ClassID);
            if (sampleList == null)
                return null;

            mMicroCourseList = sampleList;
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mAdapter = new MicroCourseAdapter();
            mListView.setAdapter(mAdapter);
            mListView.callOnClick();

            if (mMicroCourseList.size() == 0) {
                Toast.makeText(getContext().getApplicationContext(), "没有找到相关的微课资源!", Toast.LENGTH_LONG).show();
            }
        }
    }
}
