package com.tanhd.rollingclass.fragments;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

public class SubjectSelectorFragment extends Fragment {
    public static interface SelectorSubjectListener {
        void onSubjectSelected(SubjectData subjectData);
    }

    private List<SubjectData> mItemList;
    private GridView mGridView;
    private SubjectAdapter mAdapter;
    private SelectorSubjectListener mListener;

    public static SubjectSelectorFragment newInstance(SelectorSubjectListener listener) {
        SubjectSelectorFragment fragment = new SubjectSelectorFragment();
        fragment.setListener(listener);
        return fragment;
    }

    public void setListener(SelectorSubjectListener listener) {
        this.mListener = listener;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_subject_selector, container, false);
        mGridView = view.findViewById(R.id.grid_view);
        new InitDataTask().execute();
        return view;
    }

    private class InitDataTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            SchoolData schoolData = ExternalParam.getInstance().getSchoolData();
            if (schoolData == null)
                return null;

            List<SubjectData> subjectList =  ScopeServer.getInstance().qureySubject(schoolData.SchoolID);
            mItemList = subjectList;
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (mItemList == null) {
                Toast.makeText(getContext().getApplicationContext(), "没有科目!", Toast.LENGTH_LONG).show();
                DialogFragment dialog = (DialogFragment) getParentFragment();
                dialog.dismiss();
                return;
            }
            mAdapter = new SubjectAdapter();
            mGridView.setAdapter(mAdapter);
        }
    }

    private class SubjectAdapter extends BaseAdapter {

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
                view = getLayoutInflater().inflate(R.layout.item_subject_selector, parent, false);
            }
            TextView nameView = view.findViewById(R.id.name);
            final SubjectData itemData = mItemList.get(position);
            nameView.setText(itemData.SubjectName);
            view.setBackgroundResource(R.drawable.list_item_selector);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null)
                        mListener.onSubjectSelected(itemData);

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
