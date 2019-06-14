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

        private List<MicroCourseData> mMicroCourseList;

        public void setData(List<MicroCourseData> dataList) {
            mMicroCourseList = dataList;
        }

        @Override
        public int getCount() {
            if (mMicroCourseList != null) {
                return mMicroCourseList.size();
            } else {
                return 0;
            }
        }

        @Override
        public MicroCourseData getItem(int position) {
            if (mMicroCourseList == null) {
                return null;
            } else {
                return mMicroCourseList.get(position);
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_microcourse_listview, parent, false);
            }

            final MicroCourseData itemData = getItem(position);
            if (itemData == null) {
                return convertView;
            }

            TextView nameView = convertView.findViewById(R.id.name);
            nameView.setText(itemData.MicroCourseName);
            if (itemData.MicroCourseID.equals(mSelMicroCourseID)) {
                convertView.setBackgroundColor(getResources().getColor(R.color.button_blue));
            } else {
                convertView.setBackground(null);
            }

            convertView.setOnClickListener(new View.OnClickListener() {
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
                convertView.callOnClick();
            }
            return convertView;
        }
    }

    private class InitDataTask extends AsyncTask<Void, Void, List<MicroCourseData>> {

        @Override
        protected List<MicroCourseData> doInBackground(Void... voids) {
            ClassData classData = ExternalParam.getInstance().getClassData();
            if (classData == null) {
                return null;
            }
            List<MicroCourseData> sampleList = ScopeServer.getInstance().QureyMicroCourseByClassID(classData.ClassID);
            return sampleList;
        }

        @Override
        protected void onPostExecute(List<MicroCourseData> dataList) {
            mAdapter = new MicroCourseAdapter();
            mListView.setAdapter(mAdapter);
            if (dataList == null || dataList.size() == 0) {
                Toast.makeText(getContext().getApplicationContext(), "没有找到相关的微课资源!", Toast.LENGTH_LONG).show();
                return;
            }
            mAdapter.setData(dataList);
        }
    }
}
