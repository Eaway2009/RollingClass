package com.tanhd.rollingclass.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
    public static interface StudentSelectListener {
        void onStudentSelected(ArrayList<StudentData> studentList);
    }

    private static class Args implements Serializable {
        public boolean single;
        public HashSet<String> filter;
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

    public void setListener(StudentSelectListener listener) {
        this.mListener = listener;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Args args = (Args) getArguments().get("args");
        isSingle = args.single;
        mFilter = args.filter;

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
            Button allAskBtn = view.findViewById(R.id.btn_all_ask);
            Button randomAskBtn = view.findViewById(R.id.btn_random_ask);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
            allAskBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSelList.clear();
                    for (GroupData data : mAllGroupList) {
                        mSelList.addAll(data.StudentList);
                    }
                    dismiss();
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
                        dismiss();
                    } catch (Exception e) {

                    }
                }
            });
        }

        ViewGroup layoutView = view.findViewById(R.id.layout);
        ClassData classData = ExternalParam.getInstance().getClassData();
        if (classData == null) {
            Toast.makeText(getContext().getApplicationContext(), "请先选择班级后再进行操作!", Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }

        layoutView.removeAllViews();
        TextView classNameView = view.findViewById(R.id.class_name);

        classNameView.setText(classData.ClassName);
        if (classData.Groups == null) {
            return;
        }
        mAllGroupList.clear();
        mAllGroupList.addAll(classData.Groups);
        for (GroupData groupData : classData.Groups) {
            final ViewGroup viewGroup = (ViewGroup) getLayoutInflater().inflate(R.layout.item_student_selector_group, layoutView, false);
            TextView groupName = viewGroup.findViewById(R.id.group_name);
            groupName.setText(groupData.GroupName);
            groupName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ViewGroup layout = viewGroup.findViewById(R.id.container);
                    for (int i = 0; i < layout.getChildCount(); i++) {
                        View studentView = layout.getChildAt(i);
                        studentView.callOnClick();
                    }
                }
            });

            for (StudentData studentData : groupData.StudentList) {
                ViewGroup layout = viewGroup.findViewById(R.id.container);
                View v = getLayoutInflater().inflate(R.layout.item_student_selector, layout, false);
                TextView nameView = v.findViewById(R.id.name);
                nameView.setText(studentData.Username);
                TextView statusView = v.findViewById(R.id.status);
                v.setTag(studentData);

                int status = studentData.Status;
                if (mFilter != null) {
                    if (mFilter.contains(studentData.StudentID))
                        status = -1;
                }

                if (status == -1) {
                    statusView.setBackgroundColor(getResources().getColor(R.color.button_disable));
                    statusView.setText("已提问");
                } else {
                    if (status == 1) {
                        statusView.setBackgroundColor(getResources().getColor(R.color.online));
                        statusView.setText("在线");
                    } else {
                        statusView.setBackgroundColor(getResources().getColor(R.color.offline));
                        statusView.setText("离线");
                    }

                    v.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            StudentData studentData = (StudentData) v.getTag();
                            View overView = v.findViewById(R.id.over);
                            if (mSelList.contains(studentData)) {
                                mSelList.remove(studentData);
                                overView.setVisibility(View.GONE);
                            } else {
                                mSelList.add(studentData);
                                overView.setVisibility(View.VISIBLE);
                            }

                            if (isSingle)
                                dismiss();
                        }
                    });
                }

                layout.addView(v);
            }

            layoutView.addView(viewGroup);
        }
    }

    private void dismiss() {
        if (!mSelList.isEmpty()) {
            ArrayList arrayList = new ArrayList();
            arrayList.addAll(mSelList);
            mListener.onStudentSelected(arrayList);
        }

        if (getParentFragment() instanceof FrameDialog) {
            DialogFragment dialog = (DialogFragment) getParentFragment();
            dialog.dismiss();
        }
    }
}
