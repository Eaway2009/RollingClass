package com.tanhd.rollingclass.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.db.Database;
import com.tanhd.rollingclass.db.Questioning;
import com.tanhd.rollingclass.server.data.ClassData;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.GroupData;
import com.tanhd.rollingclass.server.data.LessonSampleData;
import com.tanhd.rollingclass.server.data.StudentData;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class ShowQuestionResultFragment extends Fragment {
    private View mRootView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_show_questioning_result, container, false);
        init(mRootView);
        return mRootView;
    }

    private void init(View view) {
        ViewGroup layoutView = view.findViewById(R.id.layout);
        ClassData classData = ExternalParam.getInstance().getClassData();
        if (classData == null) {
            Toast.makeText(getContext().getApplicationContext(), "请先选择班级后再进行操作!", Toast.LENGTH_SHORT).show();
            return;
        }

        layoutView.removeAllViews();
        TextView classNameView = view.findViewById(R.id.class_name);

        classNameView.setText(classData.ClassName);
        if (classData.Groups == null)
            return;

        LessonSampleData lessonSampleData = ExternalParam.getInstance().getLessonSample();
        List<Questioning> list = Database.getInstance().readQuestioning(lessonSampleData.LessonSampleID);

        for (GroupData groupData: classData.Groups) {
            ViewGroup viewGroup = (ViewGroup) getLayoutInflater().inflate(R.layout.item_student_selector_group, layoutView, false);
            TextView groupName = viewGroup.findViewById(R.id.group_name);
            groupName.setText(groupData.GroupName);

            if (groupData.StudentList != null) {
                for (final StudentData studentData: groupData.StudentList) {
                    ViewGroup layout = viewGroup.findViewById(R.id.container);
                    View v = getLayoutInflater().inflate(R.layout.item_student_selector, layout, false);
                    TextView nameView = v.findViewById(R.id.name);
                    nameView.setText(studentData.Username);
                    TextView statusView = v.findViewById(R.id.status);

                    final List<String> result = queryData(studentData.StudentID, list);
                    if (result.size() > 0) {
                        if (isCompleted(studentData.StudentID, list)) {
                            statusView.setBackgroundColor(getResources().getColor(R.color.online));
                            statusView.setText("已完成");
                        } else {
                            statusView.setBackgroundColor(getResources().getColor(R.color.offline));
                            statusView.setText("未完成");
                        }

                    } else {
                        statusView.setBackgroundColor(getResources().getColor(R.color.button_disable));
                        statusView.setText("未提问");
                    }

                    layout.addView(v);
                    v.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    });
                }
            }

            layoutView.addView(viewGroup);
        }
    }

    private List<String> queryData(String studentID, List<Questioning> list) {
        HashSet<String> result = new HashSet<>();
        for (Questioning data: list) {
            if (!data.toId.equals(studentID))
                continue;

            JSONArray array;
            try {
                array = new JSONArray(data.ids);
                for (int i=0; i<array.length(); i++) {
                    result.add(array.optString(i));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        List<String> r = new ArrayList<>();
        r.addAll(result);
        return r;
    }

    private boolean isCompleted(String studentID, List<Questioning> list) {
        ArrayList<String> result = new ArrayList<>();
        boolean b = true;
        for (Questioning data: list) {
            if (!data.toId.equals(studentID))
                continue;

            if (data.flag == 0)
                b = false;
        }

        return b;
    }
}
