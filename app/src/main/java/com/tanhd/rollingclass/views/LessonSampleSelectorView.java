package com.tanhd.rollingclass.views;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.fragments.FrameDialog;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.BaseJsonClass;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.KnowledgeData;
import com.tanhd.rollingclass.server.data.LessonSampleData;
import com.tanhd.rollingclass.server.data.StudentData;
import com.tanhd.rollingclass.server.data.TeachingMaterialData;
import com.tanhd.rollingclass.server.data.UserData;

import java.util.ArrayList;
import java.util.List;

public class LessonSampleSelectorView extends LinearLayout {
    public static interface SelectorListener {
        void onLessonSampleSelected(LessonSampleData lessonSampleData);
    }
    private SelectorListener mListener;
    private String mSelectedID;
    private ArrayList<LessonSampleData> mLessonSampleList;
    private LessonSampleAdapter mAdapter;
    private ListView mListView;

    public LessonSampleSelectorView(Context context) {
        super(context);
    }

    public LessonSampleSelectorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LessonSampleSelectorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setListener(SelectorListener listener, String selectedID) {
        this.mListener = listener;
        mSelectedID = selectedID;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mListView = findViewById(R.id.lesson_list);
        mAdapter = new LessonSampleAdapter();
        mListView.setAdapter(mAdapter);

        new InitDataTask().execute();
    }

    private class InitDataTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            if (ExternalParam.getInstance().getUserData().isTeacher()) {
                mLessonSampleList = (ArrayList<LessonSampleData>) ScopeServer.getInstance().QureyLessonSampleByTeacherID(ExternalParam.getInstance().getUserData().getOwnerID());
            } else {
                StudentData studentData = (StudentData) ExternalParam.getInstance().getUserData().getUserData();
                mLessonSampleList = (ArrayList<LessonSampleData>) ScopeServer.getInstance().QureyLessonSampleByClassID(studentData.ClassID);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mAdapter.notifyDataSetChanged();
        }
    }

    private class LessonSampleAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if (mLessonSampleList == null)
                return 0;

            return mLessonSampleList.size();
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
                view = LayoutInflater.from(getContext()).inflate(R.layout.item_lessonsample_selector, parent, false);
            }

            final LessonSampleData lessonSampleData = mLessonSampleList.get(position);
            TextView nameView = view.findViewById(R.id.name);
            nameView.setText(lessonSampleData.LessonSampleName);
            if (lessonSampleData.LessonSampleID.equals(mSelectedID)) {
                nameView.setTextColor(getResources().getColor(R.color.white));
                view.setBackgroundColor(getResources().getColor(R.color.button_blue));
            } else {
                nameView.setTextColor(getResources().getColor(R.color.black));
                view.setBackground(null);
            }

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSelectedID = lessonSampleData.LessonSampleID;
                    if (mListener != null)
                        mListener.onLessonSampleSelected(lessonSampleData);
                    mAdapter.notifyDataSetChanged();
                }
            });
            return view;
        }
    }
}
