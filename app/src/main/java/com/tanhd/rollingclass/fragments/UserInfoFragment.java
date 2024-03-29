package com.tanhd.rollingclass.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.server.RequestCallback;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.ClassData;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.SchoolData;
import com.tanhd.rollingclass.server.data.StudentData;
import com.tanhd.rollingclass.server.data.SubjectData;
import com.tanhd.rollingclass.server.data.TeacherData;
import com.tanhd.rollingclass.server.data.UserData;
import com.tanhd.rollingclass.utils.AppUtils;
import com.tanhd.rollingclass.utils.ToastUtil;

import java.util.List;

public class UserInfoFragment extends Fragment {
    private EditText mNameView;
    private EditText mUsernameView;
    private RadioButton mManView;
    private RadioButton mFemaleView;
    private EditText mMobileView;
    private EditText mSchoolView;

    private EditText mGradeView;
    private EditText mClassView;
    private EditText mCategoryView;

    private EditText mStudentXjView;
    private EditText mStudentGradeView;
    private EditText mStudentClassView;


    private EditText mPassword1View;
    private EditText mPassword2View;
    private EditText mPassword3View;
    private Callback mCallback;

    public static UserInfoFragment getInstance(Callback callback){
        UserInfoFragment page = new UserInfoFragment();
        page.setCallback(callback);
        return page;
    }

    public void setCallback(Callback callback){
        mCallback = callback;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_info, container, false);
        init(view);
        initData();
        return view;
    }

    private void init(View view) {
        mNameView = view.findViewById(R.id.name);
        mManView = view.findViewById(R.id.man);
        mFemaleView = view.findViewById(R.id.female);
        mUsernameView = view.findViewById(R.id.username);
        mMobileView = view.findViewById(R.id.mobile);
        mSchoolView = view.findViewById(R.id.school);

        mGradeView = view.findViewById(R.id.grade);
        mClassView = view.findViewById(R.id.class1);
        mCategoryView = view.findViewById(R.id.category);

        mStudentXjView = view.findViewById(R.id.student_xj);
        mStudentGradeView = view.findViewById(R.id.student_grade);
        mStudentClassView = view.findViewById(R.id.student_class);

        mPassword1View = view.findViewById(R.id.password1);
        mPassword2View = view.findViewById(R.id.password2);
        mPassword3View = view.findViewById(R.id.password3);

        UserData userData = ExternalParam.getInstance().getUserData();

        if (userData.isTeacher()) {
            TeacherData teacherData = (TeacherData) userData.getUserData();
            mNameView.setText(teacherData.Username);
            if (teacherData.Sex == 1) {
                mManView.setChecked(true);
            } else {
                mFemaleView.setChecked(true);
            }
            mUsernameView.setText(teacherData.Username);
            mMobileView.setText(teacherData.Mobile);

            view.findViewById(R.id.teacher_layout).setVisibility(View.VISIBLE);
            view.findViewById(R.id.student_layout).setVisibility(View.GONE);

            String grade = AppUtils.getStudySectionNameByCode(teacherData.StudysectionCode);
            mGradeView.setText(grade);

            String cls = "";
            for (ClassData classData: ExternalParam.getInstance().getTeachingClass()) {
                cls = cls + classData.ClassName + " ";
            }
            mClassView.setText(cls);

            String discipline = AppUtils.getSubjectNameByCode(teacherData.SubjectCode);
            mCategoryView.setText(discipline);

        } else {
            StudentData studentData = (StudentData) userData.getUserData();
            mNameView.setText(studentData.Username);
            if (studentData.Sex == 1) {
                mManView.setChecked(true);
            } else {
                mFemaleView.setChecked(true);
            }
            mUsernameView.setText(studentData.Username);
            mMobileView.setText(studentData.Mobile);

            view.findViewById(R.id.teacher_layout).setVisibility(View.GONE);
            view.findViewById(R.id.student_layout).setVisibility(View.VISIBLE);

            ClassData classData = ExternalParam.getInstance().getClassData();
            if (classData != null) {
                mStudentXjView.setText(AppUtils.getStudySectionNameByCode(classData.StudysectionCode));
                mStudentGradeView.setText(AppUtils.getGradeNameByCode(classData.GradeCode));
                mStudentClassView.setText(classData.ClassName);
            }
        }

        view.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                updatePassword();
                if(mCallback!=null){
                    mCallback.onBack();
                }
            }
        });
    }

    private void initData(){
        new InitDataTask().execute();
    }

    private class InitDataTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            SchoolData schoolData = ExternalParam.getInstance().getSchoolData();
            if (schoolData == null) {
                schoolData = ScopeServer.getInstance().getSchoolData();
                ExternalParam.getInstance().setSchoolData(schoolData);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            SchoolData schoolData = ExternalParam.getInstance().getSchoolData();
            if(schoolData!=null) {
                mSchoolView.setText(schoolData.SchoolName);
            }
        }
    }

    public interface Callback{
        void onBack();
    }
}
