package com.tanhd.rollingclass.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.tanhd.library.mqtthttp.MQTT;
import com.tanhd.library.mqtthttp.MqttListener;
import com.tanhd.library.mqtthttp.PushMessage;
import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.VideoPlayerActivity;
import com.tanhd.rollingclass.db.Database;
import com.tanhd.rollingclass.db.MSG_TYPE;
import com.tanhd.rollingclass.fragments.pages.LearningStaticsFragment;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.ClassData;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.KnowledgeData;
import com.tanhd.rollingclass.server.data.LessonSampleData;
import com.tanhd.rollingclass.server.data.MicroCourseData;
import com.tanhd.rollingclass.server.data.QuestionData;
import com.tanhd.rollingclass.server.data.StudentData;
import com.tanhd.rollingclass.server.data.TeacherData;
import com.tanhd.rollingclass.server.data.UserData;
import com.tanhd.rollingclass.utils.AppUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class TeacherFragment extends Fragment {
    private View mLeftToolbar;
    private View mRightToolbar;
    private View mBtnBarView;
    private BackListener mListener;

    public static TeacherFragment newInstance(BackListener listener) {
        TeacherFragment fragment = new TeacherFragment();
        fragment.setListener(listener);
        return fragment;
    }

    public void setListener(BackListener listener) {
        this.mListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_teacher, container, false);
        mLeftToolbar = view.findViewById(R.id.leftbar);
        mRightToolbar = view.findViewById(R.id.rightbar);
        mBtnBarView = view.findViewById(R.id.btn_bar);

        view.findViewById(R.id.btn_lesson_sample).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FrameDialog.show(getChildFragmentManager(), LessonSampleSelectorForTeacherFragment.newInstance(new LessonSampleSelectorForTeacherFragment.OnSelectorLessonSampleListener() {
                    @Override
                    public void onLessonSampleSelected(LessonSampleData lessonSampleData) {
                        ExternalParam.getInstance().setLessonSample(lessonSampleData);
                        if (ExternalParam.getInstance().getStatus() == 0) {
                            showLessonSample(lessonSampleData.UrlContent, ShowDocumentFragment.SYNC_MODE.NONE);
                        } else {
                            showLessonSample(lessonSampleData.UrlContent, ShowDocumentFragment.SYNC_MODE.MASTER);
                            HashMap<String, String> params = new HashMap<>();
                            params.put("UrlContent", lessonSampleData.UrlContent);
                            MQTT.publishMessage(PushMessage.COMMAND.OPEN_DOCUMENT, (List<String>) null, params);
                        }
                    }
                }));
            }
        });

        view.findViewById(R.id.btn_class_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FrameDialog.show(getChildFragmentManager(), TeacherMicroCourseSelectorFragment.newInstance(
                        new TeacherMicroCourseSelectorFragment.SelectorMicroCourseListener() {
                            @Override
                            public void onMicroCourseSelected(MicroCourseData microCourseData) {
                                Intent intent = new Intent(getActivity(), VideoPlayerActivity.class);
                                intent.putExtra("MicroCourseID", microCourseData.MicroCourseID);
                                intent.putExtra("ResourceAddr", ScopeServer.RESOURCE_URL + microCourseData.VideoUrl);
                                startActivity(intent);
                            }
                        }));
            }
        });

        view.findViewById(R.id.ask_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LessonSampleData lessonSampleData = ExternalParam.getInstance().getLessonSample();
                if (lessonSampleData == null) {
                    Toast.makeText(getContext().getApplicationContext(), "请先选择学案!", Toast.LENGTH_LONG).show();
                    return;
                }

                FrameDialog.show(getChildFragmentManager(), NewSetFragment.newInstance());
            }
        });

        view.findViewById(R.id.start_lesson).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                AppUtils.clearFragments(getChildFragmentManager());

                if (ExternalParam.getInstance().getStatus() == 0) {
                    FrameDialog.show(getChildFragmentManager(), ClassBeginFragment.newInstance(true, new ClassBeginFragment.ClassBeginListener() {
                        @Override
                        public void onCompleted() {
                            LessonSampleData lessonSampleData = ExternalParam.getInstance().getLessonSample();
                            showLessonSample(lessonSampleData.UrlContent, ShowDocumentFragment.SYNC_MODE.MASTER);

                            ClassData classData = ExternalParam.getInstance().getClassData();
                            MQTT.getInstance().subscribe(classData.ClassID);
                            ExternalParam.getInstance().setStatus(2);
                            Button button = (Button) v;
                            button.setText(R.string.class_end);
                            classData.resetStudentState(0);
                            notifyEnterClass(null);
                            enableToolbar(false);
                            mBtnBarView.setVisibility(View.VISIBLE);

                            view.findViewById(R.id.result_btn).setEnabled(true);
                            view.findViewById(R.id.ask_btn).setEnabled(true);
                            view.findViewById(R.id.review_papers_btn).setEnabled(true);
                        }
                    }));
                } else {
                    Button button = (Button) v;
                    button.setText(R.string.class_begin);
                    ClassData classData = ExternalParam.getInstance().getClassData();
                    MQTT.publishMessage(PushMessage.COMMAND.CLASS_END, classData.ClassID, null);
                    MQTT.getInstance().unsubscribe(classData.ClassID);
                    ExternalParam.getInstance().setStatus(0);
                    mBtnBarView.setVisibility(View.GONE);
                }

            }
        });

        view.findViewById(R.id.btn_student_wrong).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClassData classData = ExternalParam.getInstance().getClassData();
                LessonSampleData lessonSampleData = ExternalParam.getInstance().getLessonSample();

                if (classData == null || lessonSampleData == null) {
                    FrameDialog.show(getChildFragmentManager(), ClassBeginFragment.newInstance(false, new ClassBeginFragment.ClassBeginListener() {
                        @Override
                        public void onCompleted() {
                            FrameDialog.show(getChildFragmentManager(), new StudentWrongListFragment());
                        }
                    }));
                } else {
                    FrameDialog.show(getChildFragmentManager(), new StudentWrongListFragment());
                }
            }
        });

        view.findViewById(R.id.btn_bar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLeftToolbar.getVisibility() == View.GONE)
                    enableToolbar(true);
                else
                    enableToolbar(false);
            }
        });

        view.findViewById(R.id.result_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ExternalParam.getInstance().getQuestionSetID() == null) {
                    FrameDialog.show(getChildFragmentManager(), new QuestionSetListFragment());
                } else {
                    FrameDialog.show(getChildFragmentManager(), WaitAnswerFragment.newInstance(ExternalParam.getInstance().getQuestionSetID()));
                }

            }
        });

        view.findViewById(R.id.btn_static).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClassData classData = ExternalParam.getInstance().getClassData();
                LessonSampleData lessonSampleData = ExternalParam.getInstance().getLessonSample();
                if (classData == null || lessonSampleData == null) {
                    FrameDialog.show(getChildFragmentManager(), ClassBeginFragment.newInstance(false, new ClassBeginFragment.ClassBeginListener() {
                        @Override
                        public void onCompleted() {
                            showNewFragment(CountClassFragment.newInstance());
                            enableToolbar(false);
                        }
                    }));
                } else {
                    FrameDialog.show(getChildFragmentManager(), CountClassFragment.newInstance());
                }

            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        MQTT.register(mqttListener);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        MQTT.unregister(mqttListener);
    }

    private void enableToolbar(boolean b) {
        if (b) {
            mLeftToolbar.setVisibility(View.VISIBLE);
            mRightToolbar.setVisibility(View.VISIBLE);
        } else {
            mLeftToolbar.setVisibility(View.GONE);
            mRightToolbar.setVisibility(View.GONE);
        }
    }

    private void notifyEnterClass(String studentID) {
        ClassData classData = ExternalParam.getInstance().getClassData();

        //通知学生端打开学案
        TeacherData teacherData = (TeacherData) ExternalParam.getInstance().getUserData().getUserData();
        KnowledgeData knowledgeData = ExternalParam.getInstance().getKnowledge();
        LessonSampleData lessonSampleData = ExternalParam.getInstance().getLessonSample();
        HashMap<String, String> params = new HashMap<>();
        params.put("EnterClass", "1");
        params.put("ClassName", classData.ClassName);
        params.put("SubjectName", AppUtils.getSubjectNameByCode(teacherData.SubjectCode));
        params.put("TeacherName", teacherData.Username);
        params.put("KnowledgePointName", knowledgeData.KnowledgePointName);
        params.put("LessonSampleName", lessonSampleData.LessonSampleName);
        params.put("UrlContent", lessonSampleData.UrlContent);
        MQTT.publishMessage(PushMessage.COMMAND.CLASS_BEGIN, studentID, params);
    }

    private void showLessonSample(String url, ShowDocumentFragment.SYNC_MODE mode) {
        String fragmentTag = "lessonSample";
        ShowDocumentFragment fragment = ShowDocumentFragment.newInstance(ScopeServer.RESOURCE_URL + url, mode);
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction beginTransaction = fragmentManager.beginTransaction();
        beginTransaction.replace(R.id.framelayout, fragment);
        beginTransaction.addToBackStack(fragmentTag);
        beginTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        beginTransaction.commit();
    }

    private void showNewFragment(Fragment fragment) {
        String fragmentTag = "staticFragment";
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction beginTransaction = fragmentManager.beginTransaction();
        beginTransaction.replace(R.id.framelayout, fragment);
        beginTransaction.addToBackStack(fragmentTag);
        beginTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        beginTransaction.commit();
        mListener.showBack(true);
    }

    private MqttListener mqttListener = new MqttListener() {
        @Override
        public void messageArrived(PushMessage message) {
            switch (message.command) {
                case OFFLINE:
                case ONLINE:
                    if (ExternalParam.getInstance().getStatus() == 0)
                        return;

                    ClassData classData = ExternalParam.getInstance().getClassData();
                    if (classData == null)
                        return;

                    classData.setStudentState(message.from, (message.command == PushMessage.COMMAND.ONLINE ? 1 : 0));
                    break;
                case QUERY_CLASS:
                    if (ExternalParam.getInstance().getStatus() == 0)
                        return;

                    notifyEnterClass(message.from);
                    break;
                case ANSWER_COMPLETED:
                    String content = message.parameters.get("content");
                    try {
                        JSONObject json = new JSONObject(content);
                        String examID = json.optString("examID");
                        Database.getInstance().setQuestioning(examID);
                        FrameDialog.show(getChildFragmentManager(), WaitAnswerFragment.newInstance(examID));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    break;
            }
        }

        @Override
        public void networkTimeout(boolean flag) {

        }
    };

    public interface BackListener{
        public void showBack(boolean show);
    }
}
