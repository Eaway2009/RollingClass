package com.tanhd.rollingclass.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.KnowledgeData;
import com.tanhd.rollingclass.server.data.LessonSampleData;
import com.tanhd.rollingclass.server.data.StudentData;
import com.tanhd.rollingclass.server.data.TeachingMaterialData;

import java.util.ArrayList;
import java.util.List;

public class LessonSampleSelectorFragment extends Fragment {

    private static final int QUERY_MODE_SUBJECT = 1;
    private static final int QUERY_MODE_CLASS = 2;

    private int mQueryMode = 2;
    private int mSubjectCode;

    private class ItemData {
        String title;
        String name;
        LessonSampleData lessonSampleData;
        KnowledgeData knowledgeData;
    }

    public static interface OnSelectorLessonSampleListener {
        void onLessonSampleSelected(KnowledgeData knowledgeData, LessonSampleData lessonSampleData);
    }

    private ArrayList<ItemData> mItemList = new ArrayList<>();
    private ListView mListView;
    private LessonSampleAdapter mAdapter;
    private OnSelectorLessonSampleListener mListener;

    public static LessonSampleSelectorFragment newInstance(OnSelectorLessonSampleListener listener) {
        Bundle args = new Bundle();
        args.putInt("query_mode", QUERY_MODE_CLASS);
        LessonSampleSelectorFragment fragment = new LessonSampleSelectorFragment();
        fragment.setArguments(args);
        fragment.setListener(listener);
        return fragment;
    }

    public static LessonSampleSelectorFragment newInstance(int subjectCode, OnSelectorLessonSampleListener listener) {
        Bundle args = new Bundle();
        args.putInt("query_mode", QUERY_MODE_SUBJECT);
        args.putInt("subject_code", subjectCode);
        LessonSampleSelectorFragment fragment = new LessonSampleSelectorFragment();
        fragment.setArguments(args);
        fragment.setListener(listener);
        return fragment;
    }

    public void setListener(OnSelectorLessonSampleListener listener) {
        this.mListener = listener;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lesson_sample_selector, container, false);
        mListView = view.findViewById(R.id.list);
        mAdapter = new LessonSampleAdapter();
        mListView.setAdapter(mAdapter);
        initData();
        return view;
    }

    private void initData() {
        mQueryMode = getArguments().getInt("query_mode");
        new InitDataTask().execute();
    }

    private class InitDataTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            if (ExternalParam.getInstance().getUserData().isTeacher())
                return null;
            List<LessonSampleData> sampleList = null;

            if (mQueryMode == QUERY_MODE_CLASS) {
                StudentData studentData = (StudentData) ExternalParam.getInstance().getUserData().getUserData();

                sampleList = ScopeServer.getInstance().QureyLessonSampleByClassID(studentData.ClassID);
            } else {
                mSubjectCode = getArguments().getInt("subject_code");
                sampleList = ScopeServer.getInstance().QureyLessonSampleBySubject(mQueryMode);
            }

            if (sampleList == null)
                return null;

            for (LessonSampleData sampleData : sampleList) {
                KnowledgeData knowledgeData = ScopeServer.getInstance().QureyKnowledgeByID(sampleData.KnowledgeID);
                if (knowledgeData == null)
                    continue;

                TeachingMaterialData materialData = ScopeServer.getInstance().QueryTeachingMaterialById(knowledgeData.TeachingMaterialID);
                if (materialData == null)
                    continue;

                ItemData itemData = new ItemData();
                itemData.lessonSampleData = sampleData;
                itemData.knowledgeData = knowledgeData;
                itemData.name = "【" + materialData.TeachingMaterialName + "-" + materialData.SubjectName + "-" + "】" +
                        knowledgeData.ChapterName + "-" + knowledgeData.SectionName + "-" + knowledgeData.KnowledgePointName;
                itemData.title = sampleData.LessonSampleName;
                mItemList.add(itemData);
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
            return mItemList.size();
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
                view = getLayoutInflater().inflate(R.layout.item_lesson_sample_selector, parent, false);
            }
            TextView titleView = view.findViewById(R.id.title);
            TextView nameView = view.findViewById(R.id.name);
            final ItemData itemData = mItemList.get(position);
            titleView.setText(itemData.title);
            nameView.setText(itemData.name);

            view.setBackgroundResource(R.drawable.list_item_selector);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null)
                        mListener.onLessonSampleSelected(itemData.knowledgeData, itemData.lessonSampleData);

                    if (getParentFragment() instanceof FrameDialog) {
                        FrameDialog dialog = (FrameDialog) getParentFragment();
                        dialog.dismiss();
                    }

                }
            });
            return view;
        }
    }
}
