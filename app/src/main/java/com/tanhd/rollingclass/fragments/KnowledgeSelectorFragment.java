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

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.ChapterData;
import com.tanhd.rollingclass.server.data.ClassData;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.KnowledgeData;
import com.tanhd.rollingclass.server.data.LessonSampleData;
import com.tanhd.rollingclass.server.data.SchoolData;
import com.tanhd.rollingclass.server.data.SectionData;
import com.tanhd.rollingclass.server.data.SubjectData;
import com.tanhd.rollingclass.server.data.TeacherData;
import com.tanhd.rollingclass.server.data.TeachingMaterialData;
import com.tanhd.rollingclass.server.data.UserData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class KnowledgeSelectorFragment extends Fragment {
    private class ItemData {
        String title;
        String name;
        KnowledgeData knowledgeData;
    }
    public static interface SelectorKnowledgeListener {
        void onKnowledgeSelected(KnowledgeData knowledgeData);
    }

    private ArrayList<ItemData> mItemList = new ArrayList<>();
    private ListView mListView;
    private KnowledgeAdapter mAdapter;
    private SelectorKnowledgeListener mListener;

    public static KnowledgeSelectorFragment newInstance(SelectorKnowledgeListener listener) {
        KnowledgeSelectorFragment fragment = new KnowledgeSelectorFragment();
        fragment.setListener(listener);
        return fragment;
    }

    public void setListener(SelectorKnowledgeListener listener) {
        this.mListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_knowledge_selector, container, false);
        mListView = view.findViewById(R.id.list);
        mAdapter = new KnowledgeAdapter();
        mListView.setAdapter(mAdapter);
        initData();
        return view;
    }

    private void initData() {
        new InitDataTask().execute();
    }

    private class KnowledgeAdapter extends BaseAdapter {

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
                        mListener.onKnowledgeSelected(itemData.knowledgeData);

                    if (getParentFragment() instanceof FrameDialog) {
                        FrameDialog dialog = (FrameDialog) getParentFragment();
                        dialog.dismiss();
                    }

                }
            });
            return view;
        }
    }

    private class InitDataTask extends AsyncTask<Void, Void, Void> {

        private void queryLessonSamples(ClassData classData, int subjectCode, List<TeachingMaterialData> teachingMaterialDataList) {
            for (TeachingMaterialData materialData: teachingMaterialDataList) {
                List<TeachingMaterialData> materialDataList =
                        ScopeServer.getInstance().QueryTeachingMaterial(classData.StudysectionCode,
                                classData.GradeCode, subjectCode, materialData.TeachingMaterialCode);
                if (materialDataList == null)
                    continue;

                for (TeachingMaterialData teachingMaterialData: materialDataList) {
                    if (teachingMaterialData.Chapters == null)
                        continue;

                    for (ChapterData chapterData: teachingMaterialData.Chapters) {
                        if (chapterData.Sections == null)
                            continue;

                        for (SectionData sectionData: chapterData.Sections) {
                            if (sectionData.KnowledgeList == null)
                                continue;

                            for (KnowledgeData knowledgeData: sectionData.KnowledgeList) {
                                ItemData itemData = new ItemData();
                                itemData.knowledgeData = knowledgeData;
                                itemData.name = materialData.TeachingMaterialName + "-" + teachingMaterialData.SubjectName + "-" + chapterData.ChapterName + "-" + sectionData.SectionName;
                                itemData.title = knowledgeData.KnowledgePointName;
                                mItemList.add(itemData);
                            }
                        }
                    }
                }

            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            List<TeachingMaterialData> teachingMaterialDataList = ScopeServer.getInstance().QueryTeachingMaterialVersionList();
            UserData userData = ExternalParam.getInstance().getUserData();
            ClassData classData = ExternalParam.getInstance().getClassData();
            if (userData.isTeacher()) {
                TeacherData teacherData = (TeacherData) userData.getUserData();
                queryLessonSamples(classData, teacherData.SubjectCode, teachingMaterialDataList);
            } else {
                SubjectData subjectData = ExternalParam.getInstance().getSubject();
                queryLessonSamples(classData, subjectData.SubjectCode, teachingMaterialDataList);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mAdapter.notifyDataSetChanged();
        }
    }
}
