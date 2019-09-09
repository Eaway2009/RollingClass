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
import android.widget.TextView;
import android.widget.Toast;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.server.data.ClassData;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.GroupData;
import com.tanhd.rollingclass.server.data.StudentData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class StudentSelectorFragment extends Fragment {
    private ClassData mClassData;

    public static interface StudentSelectListener {
        void onStudentSelected(ArrayList<StudentData> studentList);
    }

    private static class Args implements Serializable {
        public boolean single;
        public HashSet<String> filter;
        public ClassData classData;
    }

    private StudentSelectListener mListener;
    private HashSet<StudentData> mSelList = new HashSet<>();
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
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onStudentCheck();
                }
            });
            allAskBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSelList.clear();
                    for (GroupData data : mAllGroupList) {
                        mSelList.addAll(data.StudentList);
                    }
                    onStudentCheck();
                }
            });
            randomAskBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSelList.clear();
                    try {
                        Random random = new Random();
                        int x = random.nextInt(mAllGroupList.size());
                        List<StudentData> studentList = mAllGroupList.get(x).StudentList;
                        int y = random.nextInt(studentList.size());
                        mSelList.add(studentList.get(y));
                        onStudentCheck();
                    } catch (Exception e) {

                    }
                }
            });
        }

        ViewGroup layoutView = view.findViewById(R.id.layout);

        layoutView.removeAllViews();

        if (mClassData != null && mClassData.Groups == null) {
            return;
        }
        mAllGroupList.clear();
        mAllGroupList.addAll(mClassData.Groups);
        for (GroupData groupData : mClassData.Groups) {
            final ViewGroup viewGroup = (ViewGroup) getLayoutInflater().inflate(R.layout.item_student_selector_group, layoutView, false);
            CheckBox groupName = viewGroup.findViewById(R.id.group_name);
            final GridView studentListLayout = viewGroup.findViewById(R.id.student_list_layout);
            groupName.setText(groupData.GroupName);
            groupName.setTag(groupData.StudentList);
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
                    if(isChecked){
                        mSelList.clear();
                        GroupData groupData = (GroupData) buttonView.getTag();
                        mSelList.addAll(groupData.StudentList);
                        onStudentCheck();
                    }
                }
            });
            StudentAdapter studentAdapter = new StudentAdapter(groupData.StudentList);
            studentListLayout.setAdapter(studentAdapter);
            layoutView.addView(viewGroup);
        }
    }

    private class StudentAdapter extends BaseAdapter {

        List<StudentData> mData;

        public StudentAdapter(List<StudentData> data){
            mData = data;
        }

        @Override
        public int getCount() {
            return mData == null ? 0 : mData.size();
        }

        @Override
        public Object getItem(int position) {
            return mData == null ? null:mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = getActivity().getLayoutInflater().inflate(R.layout.item_student_selectlayout,null);
            }
            CheckBox checkBox = view.findViewById(R.id.check_student);
            StudentData studentData = (StudentData) getItem(position);
            if(studentData!=null) {
                checkBox.setText(studentData.Username);
            }
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        if(!mSelList.contains(mData.get(position))){
                            mSelList.add(mData.get(position));
                        }
                    }else{
                        if(mSelList.contains(mData.get(position))){
                            mSelList.remove(mData.get(position));
                        }
                    }
                    onStudentCheck();
                }
            });
            return view;
        }
    }

    private void onStudentCheck() {
        if (!mSelList.isEmpty()) {
            ArrayList arrayList = new ArrayList();
            arrayList.addAll(mSelList);
            mListener.onStudentSelected(arrayList);
        }
    }
}
