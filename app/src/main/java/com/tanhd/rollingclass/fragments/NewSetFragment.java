//package com.tanhd.rollingclass.fragments;
//
//import android.os.AsyncTask;
//import android.os.Bundle;
//import android.support.annotation.NonNull;
//import android.support.annotation.Nullable;
//import android.support.v4.app.DialogFragment;
//import android.support.v4.app.Fragment;
//import android.support.v4.app.FragmentManager;
//import android.support.v4.app.FragmentTransaction;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Toast;
//
//import com.tanhd.library.mqtthttp.MQTT;
//import com.tanhd.rollingclass.base.MyMqttService;
//import com.tanhd.library.mqtthttp.PushMessage;
//import com.tanhd.rollingclass.R;
//import com.tanhd.rollingclass.server.ScopeServer;
//import com.tanhd.rollingclass.server.data.ExternalParam;
//import com.tanhd.rollingclass.server.data.LessonSampleData;
//import com.tanhd.rollingclass.server.data.QuestionModel;
//import com.tanhd.rollingclass.server.data.QuestionSetData;
//import com.tanhd.rollingclass.server.data.StudentData;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.UUID;
//
//public class NewSetFragment extends Fragment {
//
//    public static NewSetFragment newInstance() {
//        NewSetFragment fragment = new NewSetFragment();
//        return fragment;
//    }
//
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_new_exam, container, false);
//        return view;
//    }
//
//    @Override
//    public void onStart() {
//        super.onStart();
//        showFragment(StudentSelectorFragment.newInstance(false, null, new StudentSelectorFragment.StudentSelectListener() {
//            @Override
//            public void onStudentSelected(final ArrayList<StudentData> studentList) {
//                if (studentList == null || studentList.isEmpty()) {
//                    DialogFragment dialog = (DialogFragment) getParentFragment();
//                    dialog.dismiss();
//                    return;
//                }
//                showFragment(QuestionSelectorFragment.newInstance(new QuestionSelectorFragment.QuestionSelectListener() {
//                    @Override
//                    public void onQuestionSelected(List<QuestionModel> questionList) {
//                        if (questionList == null || questionList.isEmpty()) {
//                            Toast.makeText(getContext().getApplicationContext(), "没有选择题目", Toast.LENGTH_LONG).show();
//                            DialogFragment dialog = (DialogFragment) getParentFragment();
//                            dialog.dismiss();
//                            return;
//                        }
//
//                        new NewSetTask().execute(studentList, questionList);
//                    }
//                }));
//            }
//        }));
//    }
//
//    private void showFragment(Fragment fragment) {
//        String fragmentTag = "New Exam";
//        FragmentManager fragmentManager = getChildFragmentManager();
//        FragmentTransaction beginTransaction = fragmentManager.beginTransaction();
//        beginTransaction.replace(R.id.framelayout, fragment);
//        beginTransaction.addToBackStack(fragmentTag);
//        beginTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
//        beginTransaction.commit();
//    }
//
//    private class NewSetTask extends AsyncTask<List, Void, String> {
//        private ArrayList<StudentData> studentList;
//
//        @Override
//        protected String doInBackground(List... lists) {
//            studentList = (ArrayList<StudentData>) lists[0];
//            List questionList = lists[1];
//
//            LessonSampleData lessonSampleData = ExternalParam.getInstance().getLessonSample();
//            QuestionSetData questionSetData = new QuestionSetData();
//            questionSetData.LessonSampleID = lessonSampleData.LessonSampleID;
//            questionSetData.TeacherID = ExternalParam.getInstance().getUserData().getOwnerID();
//            questionSetData.SetName = "提问";
//
//            questionSetData.QuestionList = new ArrayList<>();
//            for (int i=0; i<questionList.size(); i++) {
//                QuestionModel questionData = (QuestionModel) questionList.get(i);
//                questionSetData.QuestionList.add(questionData.question_id);
//            }
//
//            questionSetData.StudentList = new ArrayList<>();
//            for (int i=0; i<studentList.size(); i++) {
//                StudentData studentData = (StudentData) studentList.get(i);
//                questionSetData.StudentList.add(studentData.StudentID);
//            }
//
//            String result = ScopeServer.getInstance().InsertQuestionSet(questionSetData.toJSON().toString());
//            return result;
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//            if (result == null || studentList == null || studentList.isEmpty()) {
//                Toast.makeText(getContext().getApplicationContext(), "发起提问失败!", Toast.LENGTH_LONG).show();
//                return;
//            }
//
//            List<String> to = new ArrayList<>();
//            for (StudentData studentData: studentList) {
//                to.add(studentData.StudentID);
//            }
//            HashMap<String, String> params = new HashMap<>();
//            params.put("examID", result);
//            params.put("teacherID", ExternalParam.getInstance().getUserData().getOwnerID());
//            MyMqttService.publishMessage(PushMessage.COMMAND.QUESTIONING, to, params);
//            ExternalParam.getInstance().setQuestionSetID(result);
//            showFragment(WaitAnswerFragment.newInstance(result));
//        }
//    }
//
//}
