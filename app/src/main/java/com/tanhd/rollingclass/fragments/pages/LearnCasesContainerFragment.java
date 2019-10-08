package com.tanhd.rollingclass.fragments.pages;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tanhd.library.mqtthttp.MqttListener;
import com.tanhd.library.mqtthttp.MyMqttService;
import com.tanhd.library.mqtthttp.PushMessage;
import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.activity.LearnCasesActivity;
import com.tanhd.rollingclass.db.KeyConstants;
import com.tanhd.rollingclass.db.KeyConstants.SYNC_MODE;
import com.tanhd.rollingclass.fragments.FrameDialog;
import com.tanhd.rollingclass.fragments.ImageShowFragment;
import com.tanhd.rollingclass.fragments.QuestionDisplayFragment;
import com.tanhd.rollingclass.fragments.ShowDocumentFragment;
import com.tanhd.rollingclass.fragments.ShowPptFragment;
import com.tanhd.rollingclass.fragments.VideoPlayerFragment;
import com.tanhd.rollingclass.server.data.ClassData;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.ResourceModel;
import com.tanhd.rollingclass.server.data.StudentData;
import com.tanhd.rollingclass.server.data.UserData;
import com.tanhd.rollingclass.utils.RxTimerUtil;
import com.tanhd.rollingclass.utils.ToastUtil;
import com.tanhd.rollingclass.views.PointPopupWindow;
import com.tanhd.rollingclass.views.PointPopupWindow.PopupClickCallBack;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.tanhd.rollingclass.views.PointPopupWindow.ITEM_ANSWER;
import static com.tanhd.rollingclass.views.PointPopupWindow.ITEM_EXRCISE;
import static com.tanhd.rollingclass.views.PointPopupWindow.ITEM_LOCK;
import static com.tanhd.rollingclass.views.PointPopupWindow.ITEM_MUTE;

/**
 * 学案中间部分
 */
public class LearnCasesContainerFragment extends Fragment implements OnClickListener {

    PagesListener mListener;
    private ImageView mIvSetting1;
    private ImageView mIvSetting2;
    private ImageView mIvFullScreen;
    private RelativeLayout mRlSettingLayout;
    private boolean mIsFullScreen;

    private static final String PARAM_CLASS_DATA = "PARAM_CLASS_DATA";
    private static final String PARAM_TEACHING_MATERIALID = "PARAM_TEACHING_MATERIALID";
    private static final String PARAM_KNOWLEDGE_ID = "PARAM_KNOWLEDGE_ID";
    private static final String PARAM_KNOWLEDGE_NAME = "PARAM_KNOWLEDGE_NAME";

    private PointPopupWindow mPopupWindow1;
    private PointPopupWindow mPopupWindow2;

    private int mCurrentShowModuleId = -1;
    private ShowDocumentFragment mDocumentPageFragment;
    private ShowPptFragment mPptFragment;
    private QuestionDisplayFragment mQuestionFragment;
    private ImageShowFragment mImageFragment;
    private VideoPlayerFragment mVideoPlayerFragment;
    private Fragment mCurrentFragment;
    private int mResourceType;
    private int mPageType;
    private View mHandsupLayout;
    private View mStudentHandsupLayout;
    private ImageView mShowOrHideIcon;
    private LinearLayout mHandsupListLayout;
    private View mHandsupHideLayout;

    private List<String> mHandsupStudentName = new ArrayList<>();
    private ClassData mClassData;
    private String mTeachingMaterialId;
    private String mKnowledgeId;
    private String mKnowledgeName;
    private FrameLayout container_layout;

    public static LearnCasesContainerFragment newInstance(String knowledgeId, String knowledgeName, int typeId, PagesListener listener) {
        Bundle args = new Bundle();
        args.putInt(LearnCasesActivity.PARAM_CLASS_STUDENT_PAGE, typeId);
        args.putString(PARAM_KNOWLEDGE_ID, knowledgeId);
        args.putString(PARAM_KNOWLEDGE_NAME, knowledgeName);
        LearnCasesContainerFragment page = new LearnCasesContainerFragment();
        page.setArguments(args);
        page.setListener(listener);
        return page;
    }

    private void setListener(PagesListener listener) {
        mListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.page_learncases_container, container, false);

        EventBus.getDefault().register(this);
        initParams();
        iniViews(view);
        return view;
    }

    public void setParam(ClassData classData, String teachingMaterialId, String knowledgeId, String knowledgeName) {

        Bundle args = getArguments();
        args.putSerializable(PARAM_CLASS_DATA, classData);
        args.putString(PARAM_TEACHING_MATERIALID, teachingMaterialId);
        args.putString(PARAM_KNOWLEDGE_ID, knowledgeId);
        args.putString(PARAM_KNOWLEDGE_NAME, knowledgeName);
        setArguments(args);
        initParams();
    }

    private void initParams() {
        Bundle args = getArguments();
        mPageType = args.getInt(LearnCasesActivity.PARAM_CLASS_STUDENT_PAGE);
        if (args.containsKey(PARAM_CLASS_DATA)) {
            mClassData = (ClassData) args.getSerializable(PARAM_CLASS_DATA);
            mTeachingMaterialId = args.getString(PARAM_TEACHING_MATERIALID);
            mKnowledgeId = args.getString(PARAM_KNOWLEDGE_ID);
            mKnowledgeName = args.getString(PARAM_KNOWLEDGE_NAME);
        }
    }

    private void iniViews(View view) {
        container_layout = view.findViewById(R.id.container_layout);
        mIvSetting1 = view.findViewById(R.id.iv_set1);
        mIvSetting2 = view.findViewById(R.id.iv_set2);
        mIvFullScreen = view.findViewById(R.id.iv_full_screen);
        mRlSettingLayout = view.findViewById(R.id.rl_setting);
        mHandsupLayout = view.findViewById(R.id.handsup_layout);
        mHandsupHideLayout = view.findViewById(R.id.handsup_hide_layout);
        mHandsupListLayout = view.findViewById(R.id.handsup_list_layout);
        mStudentHandsupLayout = view.findViewById(R.id.student_handsup_layout);
        mShowOrHideIcon = view.findViewById(R.id.show_or_hide_icon);
        mIvSetting1.setOnClickListener(this);
        mIvSetting2.setOnClickListener(this);
        mIvFullScreen.setOnClickListener(this);
        mStudentHandsupLayout.setOnClickListener(this);
        mHandsupLayout.setOnClickListener(this);
        mHandsupHideLayout.setOnClickListener(this);
        view.findViewById(R.id.container_layout).setOnClickListener(this);

        mPopupWindow1 = new PointPopupWindow();
        mPopupWindow1.create(getActivity(), PointPopupWindow.TYPE_SETTING_1);
        mPopupWindow1.setmListener(clickCallBack);
        mPopupWindow2 = new PointPopupWindow();
        mPopupWindow2.create(getActivity(), PointPopupWindow.TYPE_SETTING_2);
        mPopupWindow2.setmListener(clickCallBack);

        if (mPageType == KeyConstants.ClassPageType.STUDENT_CLASS_PAGE) { //学生开始进入课堂 学生端全屏
            mStudentHandsupLayout.setVisibility(View.GONE);
            mHandsupLayout.setVisibility(View.GONE);
            mHandsupHideLayout.setVisibility(View.GONE);
            mIsFullScreen = true;
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) container_layout.getLayoutParams();
            layoutParams.bottomMargin = 0;
            layoutParams.leftMargin = 0;
            layoutParams.rightMargin = 0;
            container_layout.setLayoutParams(layoutParams);
            fullScreen();
            mIvFullScreen.setVisibility(View.GONE);
            //timerGoneHand();
        } else if (mPageType == KeyConstants.ClassPageType.STUDENT_LEARNING_PAGE) {
            mRlSettingLayout.setVisibility(View.GONE);
            mIvFullScreen.setVisibility(View.GONE);
            mHandsupLayout.setVisibility(View.GONE);
            mHandsupHideLayout.setVisibility(View.GONE);
            mStudentHandsupLayout.setVisibility(View.GONE);
        } else {
            mIvFullScreen.setVisibility(View.VISIBLE);
            mRlSettingLayout.setVisibility(View.VISIBLE);
            mHandsupLayout.setVisibility(View.GONE);
            mHandsupHideLayout.setVisibility(View.VISIBLE);
            mStudentHandsupLayout.setVisibility(View.GONE);
        }
    }


    private void timerGoneHand(){
        RxTimerUtil.cancel();
        RxTimerUtil.timer(3 * 1000, new RxTimerUtil.IRxNext() {
            @Override
            public void doNext(long number) {
                mStudentHandsupLayout.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        RxTimerUtil.cancel();
    }

    PopupClickCallBack clickCallBack = new PopupClickCallBack() {
        @Override
        public void onClick(int type, boolean isClick) {
            switch (type) {
                case ITEM_ANSWER: //抢答
                    if (mClassData != null) {
                        FrameDialog.show(getFragmentManager(), ResponderFragment.getInstance(mClassData, mTeachingMaterialId, mKnowledgeId, true));
                    }
                    break;
                case ITEM_EXRCISE:  //测学
                    if (mClassData != null) {
                        FrameDialog.show(getFragmentManager(), ClassTestingFragment.getInstance(mClassData, mTeachingMaterialId, mKnowledgeId, false));
                    }
                    break;
                case ITEM_LOCK:
                    break;
                case ITEM_MUTE:
                    break;
            }
            mPopupWindow1.dimissPopup();
            mPopupWindow2.dimissPopup();
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_set1:
                mPopupWindow2.dimissPopup();
                mPopupWindow1.showPopup(v);
                break;
            case R.id.iv_set2:
                mPopupWindow1.dimissPopup();
                mPopupWindow2.showPopup(v);
                break;
            case R.id.handsup_hide_layout:
                mHandsupLayout.setVisibility(View.VISIBLE);
                mHandsupHideLayout.setVisibility(View.GONE);
                break;
            case R.id.handsup_layout:
                mHandsupLayout.setVisibility(View.GONE);
                mHandsupHideLayout.setVisibility(View.VISIBLE);
                break;
            case R.id.student_handsup_layout:
                HashMap<String, String> hashMap = new HashMap<>();
                UserData userData = ExternalParam.getInstance().getUserData();
                StudentData studentData = (StudentData) userData.getUserData();
                hashMap.put(PushMessage.PARAM_STUDENT_NAME, studentData.Username);
                MyMqttService.publishMessage(PushMessage.COMMAND.HAND_UP, (List<String>) null, hashMap);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mStudentHandsupLayout.setVisibility(View.GONE);
                    }
                }, 3000);
                break;
            case R.id.iv_full_screen:
                mIsFullScreen = !mIsFullScreen;
                fullScreen();
                break;
            case R.id.container_layout:
                if (mPageType == KeyConstants.ClassPageType.STUDENT_CLASS_PAGE) {
                    mStudentHandsupLayout.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    private void fullScreen() {
        if (mListener != null) {
            mListener.onFullScreen(mIsFullScreen);
            if (mIsFullScreen) {
                mIvFullScreen.setBackgroundResource(R.drawable.icon_fullscreen_unable);
            } else {
                mIvFullScreen.setBackgroundResource(R.drawable.icon_fullscreen_enable);
            }
            mRlSettingLayout.setVisibility(mIsFullScreen == true ? View.GONE : View.VISIBLE);
            mPopupWindow1.dimissPopup();
            mPopupWindow2.dimissPopup();
        }
    }

    public void showResource(ResourceModel resourceModel) {
        showFragment(resourceModel.resource_type, resourceModel);
    }

    public void showExercises(ResourceModel resourceModel, String knowledgeId, String knowledgeName, String lessonSampleId, String lessonSampleName) {
        mKnowledgeId = knowledgeId;
        mKnowledgeName = knowledgeName;
        if (mQuestionFragment == null) {
            mQuestionFragment = QuestionDisplayFragment.getInstance(mPageType, resourceModel, mKnowledgeId, mKnowledgeName, lessonSampleId, lessonSampleName);
        } else {
            mQuestionFragment.resetData(resourceModel, lessonSampleId, lessonSampleName);
        }
        if (mCurrentShowModuleId != KeyConstants.ResourceType.QUESTION_TYPE) {
            getChildFragmentManager().beginTransaction().replace(R.id.container_layout, mQuestionFragment).commit();
        }
        mCurrentShowModuleId = KeyConstants.ResourceType.QUESTION_TYPE;
    }

    public void showAnswer(boolean showAnswer) {
        if (mQuestionFragment != null) {
            mQuestionFragment.showAnswer(showAnswer);
        }
    }

    /**
     * [展示指定Id的页面]<BR>
     */
    public void showFragment(int moduleId, ResourceModel resourceModel) {
        Fragment fragment = getFragment(moduleId, resourceModel);
        if (mCurrentShowModuleId != moduleId) {
            getChildFragmentManager().beginTransaction().replace(R.id.container_layout, fragment).commit();
        }
        mCurrentShowModuleId = moduleId;
    }

    private Fragment getFragment(int moduleId, ResourceModel resourceModel) {
        switch (moduleId) {
            case KeyConstants.ResourceType.PPT_TYPE:
                if (mPptFragment == null) {
                    SYNC_MODE sync_mode = SYNC_MODE.MASTER;
                    switch (mPageType) {
                        case KeyConstants.ClassPageType.STUDENT_LEARNING_PAGE:
                            sync_mode = SYNC_MODE.NONE;
                            break;
                        case KeyConstants.ClassPageType.STUDENT_CLASS_PAGE:
                            sync_mode = SYNC_MODE.SLAVE;
                            break;
                    }
                    mPptFragment = ShowPptFragment.newInstance(getActivity(), resourceModel.pdf_url, resourceModel.thumbs, sync_mode);
                } else {
                    mPptFragment.refreshPpt(resourceModel.pdf_url, resourceModel.thumbs);
                }
                return mPptFragment;
            case KeyConstants.ResourceType.VIDEO_TYPE:
                if (mVideoPlayerFragment == null) {
                    mVideoPlayerFragment = VideoPlayerFragment.newInstance(resourceModel.resource_id, resourceModel.url);
                } else {
                    mVideoPlayerFragment.refreshVideo(resourceModel.resource_id, resourceModel.url);
                }
                return mVideoPlayerFragment;
            case KeyConstants.ResourceType.WORD_TYPE:
                if (mDocumentPageFragment == null) {
                    mDocumentPageFragment = ShowDocumentFragment.newInstance(getActivity(), resourceModel.pdf_url, KeyConstants.SYNC_MODE.MASTER);
                } else {
                    mDocumentPageFragment.refreshPdf(resourceModel.pdf_url);
                }
                return mDocumentPageFragment;
            case KeyConstants.ResourceType.IMAGE_TYPE:
                if (mImageFragment == null) {
                    mImageFragment = ImageShowFragment.newInstance(resourceModel.url);
                } else {
                    mImageFragment.resetData(resourceModel.url);
                }
                return mImageFragment;
            case KeyConstants.ResourceType.QUESTION_TYPE:
                if (mQuestionFragment == null) {
                    mQuestionFragment = QuestionDisplayFragment.getInstance(mPageType, resourceModel, mKnowledgeId, mKnowledgeName);
                } else {
                    mQuestionFragment.resetData(resourceModel);
                }
                return mQuestionFragment;
        }
        return null;
    }

    public interface PagesListener {
        void onFullScreen(boolean isFull);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleEventBus(PushMessage pushMessage) {
        if (pushMessage != null) {
            mqttListener.messageArrived(pushMessage);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showHandBtn(){
        if (mPageType != KeyConstants.ClassPageType.STUDENT_CLASS_PAGE) return;
        mStudentHandsupLayout.setVisibility(View.VISIBLE);
        timerGoneHand();
    }


    private MqttListener mqttListener = new MqttListener() {
        @Override
        public void messageArrived(PushMessage message) {
            switch (message.command) {
                case HAND_UP:
                    String studentName = message.parameters.get(PushMessage.PARAM_STUDENT_NAME);
                    if (mHandsupStudentName.size() > 0) {
                        for (String name : mHandsupStudentName) {
                            if (name.equals(studentName)) {
                                return;
                            }
                        }
                    }
                    mHandsupStudentName.add(studentName);
                    ToastUtil.show(studentName + getString(R.string.toast_raise_quiz));
                    TextView handUpNameView = (TextView) getLayoutInflater().inflate(R.layout.view_handsup_name, null);
                    handUpNameView.setText(studentName);
                    mHandsupListLayout.addView(handUpNameView);
                    break;
            }
        }

        @Override
        public void networkTimeout(boolean flag) {

        }
    };

    Handler handler = new Handler();

    @Override
    public void onDestroy() {
        super.onDestroy();


        EventBus.getDefault().unregister(this);
    }
}
