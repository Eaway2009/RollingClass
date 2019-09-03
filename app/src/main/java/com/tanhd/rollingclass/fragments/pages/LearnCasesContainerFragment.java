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
import android.widget.RelativeLayout;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.db.KeyConstants;
import com.tanhd.rollingclass.fragments.ShowDocumentFragment;
import com.tanhd.rollingclass.fragments.VideoPlayerFragment;
import com.tanhd.rollingclass.fragments.resource.QuestionResourceFragment;
import com.tanhd.rollingclass.fragments.resource.ResourceBaseFragment;
import com.tanhd.rollingclass.fragments.resource.ResourceGrideFragment;
import com.tanhd.rollingclass.server.data.ResourceModel;
import com.tanhd.rollingclass.views.PointPopupWindow;
import com.tanhd.rollingclass.views.PointPopupWindow.PopupClickCallBack;

import java.util.ArrayList;
import java.util.List;

public class LearnCasesContainerFragment extends Fragment implements OnClickListener {
    //1. ppt 2. doc 3. image 4. 微课 5. 习题
    private static final int MODULE_ID_SHOW_DOCUMENT = 1;
    private static final int MODULE_ID_SHOW_DOC = 2;
    private static final int MODULE_ID_SHOW_IMAGE = 3;
    private static final int MODULE_ID_SHOW_VIDEO = 4;
    private static final int MODULE_ID_SHOW_QUESTION = 5;

    PagesListener mListener;
    private ImageView mIvSetting1;
    private ImageView mIvSetting2;
    private ImageView mIvFullScreen;
    private RelativeLayout mRlSettingLayout;
    private boolean mIsFullScreen;

    private PointPopupWindow mPopupWindow1;
    private PointPopupWindow mPopupWindow2;

    private int mCurrentShowModuleId = -1;
    private ShowDocumentFragment mDocumentPageFragment;
    private VideoPlayerFragment mVideoPlayerFragment;
    private Fragment mCurrentFragment;
    private int mResourceType;

    public static LearnCasesContainerFragment newInstance(int typeId, PagesListener listener) {
        Bundle args = new Bundle();
        args.putInt("typeId", typeId);
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
        iniViews(view);
        return view;
    }

    private void iniViews(View view) {
        mIvSetting1 = view.findViewById(R.id.iv_set1);
        mIvSetting2 = view.findViewById(R.id.iv_set2);
        mIvFullScreen = view.findViewById(R.id.iv_full_screen);
        mRlSettingLayout = view.findViewById(R.id.rl_setting);
        mIvSetting1.setOnClickListener(this);
        mIvSetting2.setOnClickListener(this);
        mIvFullScreen.setOnClickListener(this);

        mPopupWindow1 = new PointPopupWindow();
        mPopupWindow1.create(getActivity(), PointPopupWindow.TYPE_SETTING_1);
        mPopupWindow1.setmListener(clickCallBack);
        mPopupWindow2 = new PointPopupWindow();
        mPopupWindow2.create(getActivity(), PointPopupWindow.TYPE_SETTING_2);
        mPopupWindow2.setmListener(clickCallBack);
    }

    PopupClickCallBack clickCallBack = new PopupClickCallBack() {
        @Override
        public void onClick(int type, boolean isClick) {
            switch (type) {
                case ITEM_ANSWER:
                    break;
                case ITEM_EXRCISE:
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
            case R.id.iv_full_screen:
                if (mListener != null) {
                    mIsFullScreen = !mIsFullScreen;
                    mListener.onFullScreen(mIsFullScreen);
                    if (mIsFullScreen) {
                        mIvFullScreen.setBackgroundResource(R.mipmap.icon_fullscreen_unable);
                    } else {
                        mIvFullScreen.setBackgroundResource(R.mipmap.icon_fullscreen_enable);
                    }
                    mRlSettingLayout.setVisibility(mIsFullScreen == true ? View.GONE : View.VISIBLE);
                    mPopupWindow1.dimissPopup();
                    mPopupWindow2.dimissPopup();
                }
                break;
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
        if(mCurrentShowModuleId !=moduleId) {
            getChildFragmentManager().beginTransaction().replace(R.id.container_layout, fragment).commit();
        }
        mCurrentShowModuleId = moduleId;
    }


//    /**
//     * [展示指定Id的页面]<BR>
//     */
//    public void showModulePage(int moduleId, ResourceModel resourceModel) {
//        if (mCurrentFragment != null && mResourceType == moduleId) {
//            return;
//        }
//        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
//        ResourceBaseFragment moduleFragment = null;
//        if (type == KeyConstants.ResourceType.PPT_TYPE) {
//            if (mPPTFragment == null) {
//                if(mListener!=null){
//                    mPPTFragment = ResourceGrideFragment.newInstance(mListener);
//                }else {
//                    mPPTFragment = ResourceGrideFragment.newInstance();
//                }
//                transaction.add(ROOT_LAYOUT_ID, mPPTFragment);
//                request(mDefaultPage, LevelType.ALL_LEVEL, ResourceType.PPT_TYPE);
//            }
//            moduleFragment = mPPTFragment;
//
//        } else if (type == ResourceType.VIDEO_TYPE) {
//            if (mVideoFragment == null) {
//                if(mListener!=null){
//                    mVideoFragment = ResourceGrideFragment.newInstance(mListener);
//                }else {
//                    mVideoFragment = ResourceGrideFragment.newInstance();
//                }
//                transaction.add(ROOT_LAYOUT_ID, mVideoFragment);
//                request(mDefaultPage, LevelType.ALL_LEVEL, ResourceType.VIDEO_TYPE);
//            }
//            moduleFragment = mVideoFragment;
//        } else if (type == ResourceType.WORD_TYPE) {
//            if (mWordFragment == null) {
//                if(mListener!=null){
//                    mWordFragment = ResourceGrideFragment.newInstance(mListener);
//                }else {
//                    mWordFragment = ResourceGrideFragment.newInstance();
//                }
//                transaction.add(ROOT_LAYOUT_ID, mWordFragment);
//                request(mDefaultPage, LevelType.ALL_LEVEL, ResourceType.WORD_TYPE);
//            }
//            moduleFragment = mWordFragment;
//        } else if (type == ResourceType.IMAGE_TYPE) {
//            if (mImageFragment == null) {
//                if(mListener!=null){
//                    mImageFragment = ResourceGrideFragment.newInstance(mListener);
//                }else {
//                    mImageFragment = ResourceGrideFragment.newInstance();
//                }
//                transaction.add(ROOT_LAYOUT_ID, mImageFragment);
//                request(mDefaultPage, LevelType.ALL_LEVEL, ResourceType.IMAGE_TYPE);
//            }
//            moduleFragment = mImageFragment;
//        } else if (type == ResourceType.QUESTION_TYPE) {
//            if (mQuestionFragment == null) {
//                if(mListener!=null){
//                    mQuestionFragment = QuestionResourceFragment.newInstance(mListener);
//                }else {
//                    mQuestionFragment = QuestionResourceFragment.newInstance();
//                }
//                transaction.add(ROOT_LAYOUT_ID, mQuestionFragment);
//                request(mDefaultPage, LevelType.ALL_LEVEL, ResourceType.QUESTION_TYPE);
//            }
//            moduleFragment = mQuestionFragment;
//        } else {
//            return;
//        }
//
//        if (mCurrentFragment != null) {
//            transaction.hide(mCurrentFragment);
//        }
//
//        resetButtomStatus();
//        view.setSelected(true);
//        transaction.show(moduleFragment);
//        transaction.commitAllowingStateLoss();
//        mCurrentFragment = moduleFragment;
//        mResourceType = type;
//    }

    private Fragment getFragment(int moduleId, ResourceModel resourceModel) {
        switch (moduleId) {
            case MODULE_ID_SHOW_DOCUMENT:
                if (mDocumentPageFragment == null) {
                    mDocumentPageFragment = ShowDocumentFragment.newInstance(getActivity(), resourceModel.pdf_url, ShowDocumentFragment.SYNC_MODE.MASTER);
                }else {
                    mDocumentPageFragment.refreshPdf(resourceModel.pdf_url);
                }
                return mDocumentPageFragment;
            case MODULE_ID_SHOW_VIDEO:
                if (mVideoPlayerFragment == null) {
                    mVideoPlayerFragment = VideoPlayerFragment.newInstance(resourceModel.resource_id, resourceModel.url);
                }
                return mDocumentPageFragment;
            case MODULE_ID_SHOW_DOC:
                if (mDocumentPageFragment == null) {
                    mDocumentPageFragment = ShowDocumentFragment.newInstance(getActivity(), resourceModel.pdf_url, ShowDocumentFragment.SYNC_MODE.MASTER);
                }else {
                    mDocumentPageFragment.refreshPdf(resourceModel.pdf_url);
                }
                return mDocumentPageFragment;
            case MODULE_ID_SHOW_IMAGE:
                if (mDocumentPageFragment == null) {
                    mDocumentPageFragment = ShowDocumentFragment.newInstance(getActivity(), resourceModel.pdf_url, ShowDocumentFragment.SYNC_MODE.MASTER);
                }else {
                    mDocumentPageFragment.refreshPdf(resourceModel.pdf_url);
                }
                return mDocumentPageFragment;
            case MODULE_ID_SHOW_QUESTION:
                if (mDocumentPageFragment == null) {
                    mDocumentPageFragment = ShowDocumentFragment.newInstance(getActivity(), resourceModel.pdf_url, ShowDocumentFragment.SYNC_MODE.MASTER);
                }else {
                    mDocumentPageFragment.refreshPdf(resourceModel.pdf_url);
                }
                return mDocumentPageFragment;
        }
        return null;
    }

    private void hideFragments(FragmentTransaction transaction, int moduleId) {
        if (moduleId != MODULE_ID_SHOW_DOCUMENT) {
            transaction.hide(mDocumentPageFragment);
        }
    }

    public interface PagesListener {
        void onFullScreen(boolean isFull);
    }

}
