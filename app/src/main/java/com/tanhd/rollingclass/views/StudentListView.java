package com.tanhd.rollingclass.views;

import android.content.Context;
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
import com.tanhd.rollingclass.fragments.StudentWrongListFragment;
import com.tanhd.rollingclass.server.data.ClassData;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.GroupData;
import com.tanhd.rollingclass.server.data.StudentData;

import java.util.ArrayList;
import java.util.List;

public class StudentListView extends LinearLayout {
    public static interface StudentListViewListener {
        void onSelectedItem(StudentData studentData);
    }

    private ListView mListView;
    private List<StudentData> mStudentList;
    private String mSelStudentID;
    private StudentAdapter mAdapter;
    private StudentListViewListener mListener;
    private View mClassLayout;
    private TextView mClassNameView;
    private boolean mEnableClass = true;

    public StudentListView(Context context) {
        super(context);
    }

    public StudentListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StudentListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        init();
    }

    public void setListener(StudentListViewListener listener) {
        mListener = listener;
    }

    public void setSelectedItem(String studentID) {
        mSelStudentID = studentID;
        mAdapter.notifyDataSetChanged();
    }

    public void enableClass(boolean b) {
        mEnableClass = b;
    }

    private void init() {
        mClassLayout = findViewById(R.id.class_layout);
        mClassNameView = findViewById(R.id.class_name);
        mClassLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectedItem(null);
                mClassNameView.setTextColor(getResources().getColor(R.color.white));
                mClassLayout.setBackgroundColor(getResources().getColor(R.color.button_blue));
                if (mListener != null) {
                    mListener.onSelectedItem(null);
                }
            }
        });

        ClassData classData = ExternalParam.getInstance().getClassData();
        if (classData.Groups == null)
            return;
        TextView classNameView = findViewById(R.id.class_name);
        classNameView.setText(classData.ClassName + "学生列表");

        mStudentList = new ArrayList<>();
        for (GroupData groupData: classData.Groups) {
            mStudentList.addAll(groupData.StudentList);
        }

        mAdapter = new StudentAdapter();
        mListView = findViewById(R.id.student_list);
        mListView.setAdapter(mAdapter);

        if (mEnableClass)
            mClassLayout.callOnClick();
    }

    private class StudentAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if (mStudentList == null)
                return 0;

            return mStudentList.size();
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
                view = LayoutInflater.from(getContext()).inflate(R.layout.item_student_wrong_list_student, parent, false);
            }

            final StudentData studentData = mStudentList.get(position);
            TextView nameView = view.findViewById(R.id.name);
            nameView.setText(studentData.Username);

            if (studentData.StudentID.equals(mSelStudentID)) {
                nameView.setTextColor(getResources().getColor(R.color.white));
                view.setBackgroundColor(getResources().getColor(R.color.button_blue));

                mClassNameView.setTextColor(getResources().getColor(R.color.black));
                mClassLayout.setBackground(null);
            } else {
                nameView.setTextColor(getResources().getColor(R.color.black));
                view.setBackground(null);
            }

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSelStudentID = studentData.StudentID;
                    if (mListener != null) {
                        mListener.onSelectedItem(studentData);
                    }
                    notifyDataSetChanged();
                }
            });
            return view;
        }
    }
}
