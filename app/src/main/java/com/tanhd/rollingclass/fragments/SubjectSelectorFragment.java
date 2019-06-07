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

    public static final String SELECTED_SUBJECT = "selected_subject";

    private List<SubjectData> mItemList;
    private GridView mGridView;
    private SubjectAdapter mAdapter;
    private Fragment mNextFragment;
    private SelectorSubjectListener mListener;
    private boolean mDismissDialog;

    public static SubjectSelectorFragment newInstance(boolean dismissDialog, SelectorSubjectListener listener) {
        Bundle args = new Bundle();
        args.putBoolean("dismissDialog", dismissDialog);

        SubjectSelectorFragment fragment = new SubjectSelectorFragment();
        fragment.setListener(listener);
        fragment.setArguments(args);
        return fragment;
    }

    public static SubjectSelectorFragment newInstance(Fragment nextFragment) {
        SubjectSelectorFragment fragment = new SubjectSelectorFragment();
        fragment.setNextFragment(nextFragment);
        return fragment;
    }

    public void setListener(SelectorSubjectListener listener) {
        this.mListener = listener;
    }

    public void setNextFragment(Fragment nextFragment) {
        this.mNextFragment = nextFragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_subject_selector, container, false);
        mGridView = view.findViewById(R.id.grid_view);
        Bundle args = getArguments();
        if (args != null) {
            mDismissDialog = args.getBoolean("dismissDialog");
        }
        new InitDataTask().execute();
        return view;
    }

    private class InitDataTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            SchoolData schoolData = ExternalParam.getInstance().getSchoolData();
            if (schoolData == null)
                return null;

            List<SubjectData> subjectList = ScopeServer.getInstance().qureySubject(schoolData.SchoolID);
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
                    if (mListener != null) {
                        mListener.onSubjectSelected(itemData);
                        return;
                    }

                    if (mNextFragment != null && getParentFragment() instanceof FrameDialog) {
                        FrameDialog dialog = (FrameDialog) getParentFragment();
                        Bundle args = mNextFragment.getArguments();
                        if (args == null) {
                            args = new Bundle();
                        }
                        args.putSerializable(SELECTED_SUBJECT, itemData);
                        mNextFragment.setArguments(args);
                        dialog.replaceFragment(mNextFragment);

                    }

                }
            });
            return view;
        }
    }
}
