package com.tanhd.rollingclass.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tanhd.library.mqtthttp.MQTT;
import com.tanhd.library.mqtthttp.MqttListener;
import com.tanhd.library.mqtthttp.PushMessage;
import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.VideoPlayerActivity;
import com.tanhd.rollingclass.db.Database;
import com.tanhd.rollingclass.db.MSG_TYPE;
import com.tanhd.rollingclass.fragments.pages.CommentAnswerPage;
import com.tanhd.rollingclass.server.ConnectionStatus;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.KnowledgeData;
import com.tanhd.rollingclass.server.data.LessonSampleData;
import com.tanhd.rollingclass.server.data.MicroCourseData;
import com.tanhd.rollingclass.server.data.QuestionData;
import com.tanhd.rollingclass.server.data.SubjectData;
import com.tanhd.rollingclass.server.data.UserData;
import com.tanhd.rollingclass.utils.AppUtils;

import java.util.List;

public class StudentFragment extends Fragment {
    private View mLeftToolbar;
    private View mRightToolbar;
    private ConnectionStatus mConnectionStatus;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_student, container, false);
        mLeftToolbar = view.findViewById(R.id.leftbar);
        mRightToolbar = view.findViewById(R.id.rightbar);

        view.findViewById(R.id.btn_learning).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FrameDialog.show(getChildFragmentManager(), SubjectSelectorFragment.newInstance(
                        LessonSampleSelectorFragment.newInstance(new LessonSampleSelectorFragment.OnSelectorLessonSampleListener() {
                            @Override
                            public void onLessonSampleSelected(KnowledgeData knowledgeData, LessonSampleData lessonSampleData) {
                                ExternalParam.getInstance().setLessonSample(lessonSampleData);
                                ExternalParam.getInstance().setKnowledge(knowledgeData);
                                showLessonSample(lessonSampleData.UrlContent, ShowDocumentFragment.SYNC_MODE.NONE);
                                view.findViewById(R.id.chat).setEnabled(true);

                                if (getParentFragment() instanceof FrameDialog) {
                                    FrameDialog dialog = (FrameDialog) getParentFragment();
                                    dialog.dismiss();
                                }
                            }
                        })
                ));
            }
        });

        view.findViewById(R.id.chat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String chatID = ExternalParam.getInstance().getLessonSample().TeacherID;
                FrameDialog.show(getChildFragmentManager(), ChatFragment.newInstance(chatID));
            }
        });

        view.findViewById(R.id.micro_course).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FrameDialog.show(getChildFragmentManager(), SubjectSelectorFragment.newInstance(MicroCourseSelectorFragment.newInstance(
                        new MicroCourseSelectorFragment.SelectorMicroCourseListener() {
                            @Override
                            public void onMicroCourseSelected(MicroCourseData microCourseData) {
                                Intent intent = new Intent(getActivity(), VideoPlayerActivity.class);
                                intent.putExtra("MicroCourseID", microCourseData.MicroCourseID);
                                intent.putExtra("ResourceAddr", ScopeServer.RESOURCE_URL + microCourseData.VideoUrl);
                                startActivity(intent);
                            }
                        })));
            }
        });

        view.findViewById(R.id.exam).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final LessonSampleData learningLessonSampleData = ExternalParam.getInstance().getLessonSample();
                if (learningLessonSampleData == null) {
                    FrameDialog.show(getChildFragmentManager(), SubjectSelectorFragment.newInstance(
                            LessonSampleSelectorFragment.newInstance(new LessonSampleSelectorFragment.OnSelectorLessonSampleListener() {
                                        @Override
                                        public void onLessonSampleSelected(KnowledgeData knowledgeData, LessonSampleData lessonSampleData) {
                                            FrameDialog.fullShow(getChildFragmentManager(), ExamFragment.newInstance(lessonSampleData.LessonSampleID, null));
                                        }
                                    }
                            )));
                } else {
                    FrameDialog.fullShow(getChildFragmentManager(), ExamFragment.newInstance(learningLessonSampleData.LessonSampleID, null, null));
                }
            }
        });

        view.findViewById(R.id.btn_wrong_question).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ExternalParam.getInstance().getLessonSample() == null) {
                    FrameDialog.show(getChildFragmentManager(), PreLearningFragment.newInstance(new PreLearningFragment.PreLearningListener() {
                        @Override
                        public void onCompleted() {
                            UserData userData = ExternalParam.getInstance().getUserData();
                            FrameDialog.fullShow(getChildFragmentManager(), WrongQuestionShowFragment.newInstance(userData.getOwnerID()));
                        }
                    }));
                } else {
                    UserData userData = ExternalParam.getInstance().getUserData();
                    FrameDialog.fullShow(getChildFragmentManager(), WrongQuestionShowFragment.newInstance(userData.getOwnerID()));
                }
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            MQTT.register(mqttListener);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    MQTT.publishMessage(PushMessage.COMMAND.QUERY_CLASS, (List<String>) null, null);
                }
            }, 2000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        MQTT.publishMessage(PushMessage.COMMAND.OFFLINE, (List<String>) null, null);
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

    private MqttListener mqttListener = new MqttListener() {

        @Override
        public void messageArrived(final PushMessage message) {
            switch (message.command) {
                case CLASS_BEGIN: {
                    if (ExternalParam.getInstance().getStatus() == 0) {
                        ClassPromptFragment dialog = ClassPromptFragment.newInstance(message.parameters, new ClassPromptFragment.ClassPromptListener() {
                            @Override
                            public void onEnter(String url) {
                                AppUtils.clearFragments(getChildFragmentManager());
                                enableToolbar(false);
                                showLessonSample(url, ShowDocumentFragment.SYNC_MODE.SLAVE);
                                MQTT.publishMessage(PushMessage.COMMAND.ONLINE, (List<String>) null, null);
                                ExternalParam.getInstance().setStatus(2);
                            }
                        });
                        dialog.setCancelable(false);
                        dialog.show(getChildFragmentManager(), null);
                        ExternalParam.getInstance().setStatus(1);
                    }
                    break;
                }
                case CLASS_END: {
                    classEnd(message.from);
                    break;
                }
                case QUESTIONING: {
                    String examID = message.parameters.get("examID");
                    final String teacherID = message.parameters.get("teacherID");
                    FrameDialog.fullShow(getChildFragmentManager(), ExamFragment.newInstance(teacherID, examID, new ExamFragment.ExamListener() {
                        @Override
                        public void onFinished() {
                            MQTT.publishMessage(PushMessage.COMMAND.ANSWER_COMPLETED, teacherID, null);
                        }
                    }));
                    break;
                }
                case OPEN_DOCUMENT: {
                    if (ExternalParam.getInstance().getStatus() == 2) {
                        String url = message.parameters.get("UrlContent");
                        showLessonSample(url, ShowDocumentFragment.SYNC_MODE.SLAVE);
                    }
                    break;
                }
                case SERVER_PING: {
                    FrameDialog.show(getChildFragmentManager(), ServerTesterFragment.newInstance());
                    break;
                }
                case QUERY_STATUS: {
                    if (ExternalParam.getInstance().getStatus() == 2)
                        MQTT.publishMessage(PushMessage.COMMAND.ONLINE, (List<String>) null, null);
                    else
                        MQTT.publishMessage(PushMessage.COMMAND.OFFLINE, (List<String>) null, null);
                    break;
                }
            }
        }

        @Override
        public void networkTimeout(boolean flag) {

        }
    };

    private void classEnd(String targetID) {
        enableToolbar(true);
        ExternalParam.getInstance().setStatus(0);
        AppUtils.clearFragments(getChildFragmentManager());
        MQTT.publishMessage(PushMessage.COMMAND.OFFLINE, targetID, null);
        if (mConnectionStatus != null) {
            mConnectionStatus.stop();
            mConnectionStatus = null;
        }
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

}
