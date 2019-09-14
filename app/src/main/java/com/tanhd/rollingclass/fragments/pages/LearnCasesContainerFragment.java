package com.tanhd.rollingclass.fragments.pages;

import static com.tanhd.rollingclass.views.PointPopupWindow.ITEM_ANSWER;
import static com.tanhd.rollingclass.views.PointPopupWindow.ITEM_EXRCISE;
import static com.tanhd.rollingclass.views.PointPopupWindow.ITEM_LOCK;
import static com.tanhd.rollingclass.views.PointPopupWindow.ITEM_MUTE;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
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
import com.tanhd.rollingclass.fragments.FrameDialog;
import com.tanhd.rollingclass.fragments.ImageShowFragment;
import com.tanhd.rollingclass.fragments.ShowDocumentFragment;
import com.tanhd.rollingclass.fragments.ShowPptFragment;
import com.tanhd.rollingclass.fragments.VideoPlayerFragment;
import com.tanhd.rollingclass.fragments.resource.QuestionResourceFragment;
import com.tanhd.rollingclass.server.data.ClassData;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.ResourceModel;
import com.tanhd.rollingclass.server.data.StudentData;
import com.tanhd.rollingclass.server.data.UserData;
import com.tanhd.rollingclass.views.PointPopupWindow;
import com.tanhd.rollingclass.views.PointPopupWindow.PopupClickCallBack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import com.tanhd.rollingclass.db.KeyConstants.SYNC_MODE;

public class LearnCasesContainerFragment extends Fragment implements OnClickListener {

    PagesListener mListener;
    private ImageView mIvSetting1;
    private ImageView mIvSetting2;
    private ImageView mIvFullScreen;
    private RelativeLayout mRlSettingLayout;
    private boolean mIsFullScreen;

    private static final String PARAM_CLASS_DATA = "PARAM_CLASS_DATA";
    private static final String PARAM_TEACHING_MATERIALID = "PARAM_TEACHING_MATERIALID";

    private PointPopupWindow mPopupWindow1;
    private PointPopupWindow mPopupWindow2;

    private int mCurrentShowModuleId = -1;
    private ShowDocumentFragment mDocumentPageFragment;
    private ShowPptFragment mPptFragment;
    private QuestionResourceFragment mQuestionFragment;
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

    public static LearnCasesContainerFragment newInstance(int typeId, PagesListener listener) {
        Bundle args = new Bundle();
        args.putInt(LearnCasesActivity.PARAM_CLASS_STUDENT_PAGE, typeId);
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

    public void setParam(ClassData classData, String teachingMaterialId) {

        Bundle args = getArguments();
        args.putSerializable(PARAM_CLASS_DATA, classData);
        args.putSerializable(PARAM_TEACHING_MATERIALID, teachingMaterialId);

        initParams();
    }

    private void initParams() {
        Bundle args = getArguments();
        mPageType = args.getInt(LearnCasesActivity.PARAM_CLASS_STUDENT_PAGE);
        if (args.containsKey(PARAM_CLASS_DATA)) {
            mClassData = (ClassData) args.getSerializable(PARAM_CLASS_DATA);
            mTeachingMaterialId = args.getString(PARAM_TEACHING_MATERIALID);
        }
    }

    private void iniViews(View view) {
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

        mPopupWindow1 = new PointPopupWindow();
        mPopupWindow1.create(getActivity(), PointPopupWindow.TYPE_SETTING_1);
        mPopupWindow1.setmListener(clickCallBack);
        mPopupWindow2 = new PointPopupWindow();
        mPopupWindow2.create(getActivity(), PointPopupWindow.TYPE_SETTING_2);
        mPopupWindow2.setmListener(clickCallBack);

        if (mPageType == KeyConstants.ClassPageType.STUDENT_CLASS_PAGE) {
            mStudentHandsupLayout.setVisibility(View.VISIBLE);
            mHandsupLayout.setVisibility(View.GONE);
            mHandsupHideLayout.setVisibility(View.GONE);
            mIsFullScreen = true;
            fullScreen();
            mIvFullScreen.setVisibility(View.GONE);
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

    PopupClickCallBack clickCallBack = new PopupClickCallBack() {
        @Override
        public void onClick(int type, boolean isClick) {
            switch (type) {
                case ITEM_ANSWER:
                    break;
                case ITEM_EXRCISE:
                    if (mClassData != null) {
                        FrameDialog.show(getFragmentManager(), ClassTestingFragment.getInstance(mClassData, mTeachingMaterialId));
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
                break;
            case R.id.iv_full_screen:
                mIsFullScreen = !mIsFullScreen;
                fullScreen();
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
                    mPptFragment = ShowPptFragment.newInstance(getActivity(), resourceModel.pdf_url,resourceModel.thumbs, SYNC_MODE.MASTER);
                } else {
                    mPptFragment.refreshPpt(resourceModel.pdf_url, resourceModel.thumbs);
                }
                return mPptFragment;
            case KeyConstants.ResourceType.VIDEO_TYPE:
                if (mVideoPlayerFragment == null) {
                    mVideoPlayerFragment = VideoPlayerFragment.newInstance(resourceModel.resource_id, resourceModel.url);
                }else{
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
                    mQuestionFragment = QuestionResourceFragment.newInstance();
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
                    Toast.makeText(getActivity(), studentName + "举手提问", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onDestroy() {
        super.onDestroy();


        EventBus.getDefault().unregister(this);
    }
}
