package com.tanhd.rollingclass.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.server.data.ClassData;
import com.tanhd.rollingclass.server.data.GroupData;
import com.tanhd.rollingclass.server.data.QuestionSetData;
import com.tanhd.rollingclass.server.data.StudentData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

/**
 * 学生选择
 */
public class StudentSelectorFragment extends Fragment {
    private ClassData mClassData;
    private LinearLayout mLayoutView;

    public static interface StudentSelectListener {
        void onStudentSelected(ArrayList<QuestionSetData.Group> studentList);
    }

    private static class Args implements Serializable {
        public boolean single;
        public HashSet<String> filter;
        public ClassData classData;
    }

    private StudentSelectListener mListener;
    private List<GroupData> mAllGroupList = new ArrayList<>();
    private View mRootView;
    private boolean isSingle;
    private HashSet<String> mFilter;

    public static StudentSelectorFragment newInstance(boolean single, HashSet<String> filter, StudentSelectListener listener) {
        Args data = new Args();
        data.single = single;
        data.filter = filter;

        Bundle args = new Bundle();
        args.putSerializable("args", data);
        StudentSelectorFragment fragment = new StudentSelectorFragment();
        fragment.setArguments(args);
        fragment.setListener(listener);
        return fragment;
    }

    public static StudentSelectorFragment newInstance(boolean single, HashSet<String> filter, ClassData classData, StudentSelectListener listener) {
        Args data = new Args();
        data.single = single;
        data.filter = filter;
        data.classData = classData;

        Bundle args = new Bundle();
        args.putSerializable("args", data);
        StudentSelectorFragment fragment = new StudentSelectorFragment();
        fragment.setArguments(args);
        fragment.setListener(listener);
        return fragment;
    }

    public void setListener(StudentSelectListener listener) {
        this.mListener = listener;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Args args = (Args) getArguments().get("args");
        isSingle = args.single;
        mFilter = args.filter;
        mClassData = args.classData;

        mRootView = inflater.inflate(R.layout.fragment_student_selector, container, false);
        init(mRootView);
        return mRootView;
    }

    private void init(View view) {
        View askLayout = view.findViewById(R.id.layout_ask);
        if (isSingle) {
            askLayout.setVisibility(View.GONE);
        } else {
            askLayout.setVisibility(View.VISIBLE);
            Button btn = view.findViewById(R.id.btn_confirm);
            CheckBox allAskBtn = view.findViewById(R.id.btn_all_ask);
            Button randomAskBtn = view.findViewById(R.id.btn_random_ask);
            btn.setVisibility(View.GONE);
            randomAskBtn.setVisibility(View.GONE);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onStudentCheck();
                }
            });
            allAskBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    for (GroupData groupData : mClassData.Groups) {
                        for (StudentData studentData : groupData.StudentList) {
                            studentData.check = true;
                        }
                    }
                    notifyDatasChange();
                    onStudentCheck();
                }
            });
            randomAskBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Random random = new Random();
                        int x = random.nextInt(mAllGroupList.size());
                        List<StudentData> studentList = mAllGroupList.get(x).StudentList;
                        int y = random.nextInt(studentList.size());
                        onStudentCheck();
                    } catch (Exception e) {

                    }
                }
            });
        }

        mLayoutView = view.findViewById(R.id.layout);

        mLayoutView.removeAllViews();

        if (mClassData != null && mClassData.Groups == null) {
            return;
        }
        mAllGroupList.clear();
        mAllGroupList.addAll(mClassData.Groups);
        for (int i = 0; i < mClassData.Groups.size(); i++) {
            GroupData groupData = mClassData.Groups.get(i);
            ViewGroup viewGroup = (ViewGroup) getLayoutInflater().inflate(R.layout.item_student_selector_group, mLayoutView, false);
            CheckBox groupName = viewGroup.findViewById(R.id.group_name);
            final GridView studentListLayout = viewGroup.findViewById(R.id.student_list_layout);
            groupName.setText(groupData.GroupName);
            groupName.setTag(i);
            groupName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (studentListLayout.getVisibility() == View.VISIBLE) {
                        studentListLayout.setVisibility(View.GONE);
                    } else {
                        studentListLayout.setVisibility(View.VISIBLE);
                    }
                }
            });
            groupName.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Integer index = (Integer) buttonView.getTag();
                    GroupData groupData = mClassData.Groups.get(index);
                    for (StudentData studentData : groupData.StudentList) {
                        studentData.check = isChecked;
                    }
                    notifyDatasChange();
                    onStudentCheck();
                }
            });
            StudentAdapter studentAdapter = new StudentAdapter(i, groupData.StudentList);
            studentListLayout.setAdapter(studentAdapter);
            mLayoutView.addView(viewGroup);
        }
    }

    private void notifyDatasChange() {
        for (int i = 0; i < mLayoutView.getChildCount(); i++) {
            View groupView = mLayoutView.getChildAt(i);
            CheckBox groupName = groupView.findViewById(R.id.group_name);
            if(groupName!=null){
                int index = (int) groupName.getTag();
                groupName.setChecked(groupIsChecked(index));

                GridView studentListLayout = groupView.findViewById(R.id.student_list_layout);
                StudentAdapter studentAdapter = (StudentAdapter) studentListLayout.getAdapter();
                studentAdapter.notifyDataSetChanged();
            }
        }
    }

    private boolean groupIsChecked(int i){
        GroupData groupData = mClassData.Groups.get(i);
        for(StudentData studentData:groupData.StudentList){
            if(studentData.check==false){
                return false;
            }
        }

        return true;
    }

    private class StudentAdapter extends BaseAdapter {

        List<StudentData> mData;
        int mIndex;

        public StudentAdapter(int index, List<StudentData> data) {
            mData = data;
            mIndex = index;
        }

        @Override
        public int getCount() {
            return mData == null ? 0 : mData.size();
        }

        @Override
        public Object getItem(int position) {
            return mData == null ? null : mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = getActivity().getLayoutInflater().inflate(R.layout.item_student_selectlayout, null);
            }
            CheckBox checkBox = view.findViewById(R.id.check_student);
            final StudentData studentData = (StudentData) getItem(position);
            if (studentData != null) {
                checkBox.setText(studentData.Username);
                checkBox.setChecked(studentData.check);
            }
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    studentData.check = isChecked;
                    notifyDatasChange();
                    onStudentCheck();
                }
            });
            return view;
        }
    }

    private void onStudentCheck() {
        ArrayList<QuestionSetData.Group> arrayList = new ArrayList();
        for (GroupData groupData : mClassData.Groups) {
            QuestionSetData.Group group = new QuestionSetData.Group();
            ArrayList<String> studentList = new ArrayList<>();
            for (StudentData studentData : groupData.StudentList) {
                if (studentData.check) {
                    studentList.add(studentData.StudentID);
                }
            }
            if(studentList.size()>0){
                group.students = studentList;
                arrayList.add(group);
            }
        }
        mListener.onStudentSelected(arrayList);
    }
}
