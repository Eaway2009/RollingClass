package com.tanhd.rollingclass.fragments;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.ClassData;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.KnowledgeData;
import com.tanhd.rollingclass.server.data.LessonSampleData;
import com.tanhd.rollingclass.server.data.MicroCourseData;
import com.tanhd.rollingclass.server.data.TeachingMaterialData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MicroCourseSelectorFragment extends Fragment {
    private class ItemData {
        String title;
        String name;
        MicroCourseData microCourseData;
    }
    public static interface SelectorMicroCourseListener {
        void onMicroCourseSelected(MicroCourseData microCourseData);
    }

    private ArrayList<ItemData> mItemList = new ArrayList<>();
    private ListView mListView;
    private MicroCourseAdapter mAdapter;
    private SelectorMicroCourseListener mListener;

    public static MicroCourseSelectorFragment newInstance(SelectorMicroCourseListener listener) {
        MicroCourseSelectorFragment fragment = new MicroCourseSelectorFragment();
        fragment.setListener(listener);
        return fragment;
    }

    public void setListener(SelectorMicroCourseListener listener) {
        this.mListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_micro_course_selector, container, false);
        mListView = view.findViewById(R.id.list);
        mAdapter = new MicroCourseAdapter();
        mListView.setAdapter(mAdapter);
        initData();
        return view;
    }

    private void initData() {
        new InitDataTask().execute();
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

            for (MicroCourseData microCourseData: sampleList) {
//                KnowledgeData knowledgeData = ScopeServer.getInstance().QureyKnowledgeByID(microCourseData.KnowledgeID);
//                TeachingMaterialData materialData = ScopeServer.getInstance().QueryTeachingMaterialById(knowledgeData.TeachingMaterialID);

                ItemData itemData = new ItemData();
                itemData.microCourseData = microCourseData;
                itemData.name = microCourseData.ChapterName + "-" + microCourseData.SectionName + "-" + microCourseData.PointName;
                //"【" + materialData.TeachingMaterialName + "-" + materialData.SubjectName + "】" + knowledgeData.ChapterName + "-" + knowledgeData.SectionName + "-" + knowledgeData.KnowledgePointName;
                itemData.title = microCourseData.MicroCourseInfo.MicroCourseName;
                mItemList.add(itemData);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mAdapter.notifyDataSetChanged();

            if (mItemList.size() == 0 && getContext() != null) {
                try {
                    Toast.makeText(getActivity().getApplicationContext(), "没有找到相关的微课资源!", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                }
                DialogFragment dialog = (DialogFragment) getParentFragment();
                dialog.dismiss();
            }
        }
    }

    private class MicroCourseAdapter extends BaseAdapter {

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
                view = getLayoutInflater().inflate(R.layout.item_knowledge_selector, parent, false);
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
                        mListener.onMicroCourseSelected(itemData.microCourseData);

                    DialogFragment dialog = (DialogFragment) getParentFragment();
                    dialog.dismiss();
                }
            });
            return view;
        }
    }
}
