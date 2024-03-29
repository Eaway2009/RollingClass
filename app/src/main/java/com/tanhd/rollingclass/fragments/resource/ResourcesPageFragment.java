package com.tanhd.rollingclass.fragments.resource;

import static com.tanhd.rollingclass.fragments.pages.ResourceSelectorFragment.PARAM_RESOURCE_TYPE;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.activity.DocumentEditActivity;
import com.tanhd.rollingclass.activity.ResourceShowActivity;
import com.tanhd.rollingclass.db.KeyConstants.LevelType;
import com.tanhd.rollingclass.db.KeyConstants.ResourceType;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.KnowledgeModel;
import com.tanhd.rollingclass.server.data.QuestionModel;
import com.tanhd.rollingclass.server.data.ResourceModel;
import com.tanhd.rollingclass.server.data.UserData;
import com.tanhd.rollingclass.views.OnItemClickListener;
import com.tanhd.rollingclass.views.PopFliterRes;

import java.util.ArrayList;
import java.util.List;

/**
 * 资源
 */
public class ResourcesPageFragment extends Fragment implements View.OnClickListener, ResourceBaseFragment.Callback {

    private View mPptTitleView;
    private View mWordTitleView;
    private View mImageTitleView;
    private View mMicroCourseTitleView;
    private View mQuestionTitleView;
    private View mUploadFileTitleView;
    private KnowledgeModel mKnowledgeModel;
    private TextView tv_spinner;
    Animation myAnimation;
    private boolean mIsRequesting;

    private int mLevel = LevelType.ALL_LEVEL; //1 公共资源 2 校本资源 3 私藏资源
    private int mResourceType = ResourceType.PPT_TYPE;//初始值
    private int mCurrentPage = 1;
    private final int mDefaultPage = 1;
    private final int mPageSize = 30;
    private static final int ROOT_LAYOUT_ID = R.id.fragment_container;
    private ResourceBaseFragment.Callback mListener;
    private Handler mHandler = new Handler();
    private int resourceType = -1;
    private PopFliterRes popFliterRes;

    public static ResourcesPageFragment newInstance(KnowledgeModel knowledgeModel) {
        Bundle args = new Bundle();
        ResourcesPageFragment page = new ResourcesPageFragment();
        args.putSerializable(DocumentEditActivity.PARAM_TEACHING_MATERIAL_DATA, knowledgeModel);
        page.setArguments(args);
        return page;
    }

    public static ResourcesPageFragment newInstance(KnowledgeModel knowledgeModel, int resourceCode, ResourceBaseFragment.Callback callback) {
        Bundle args = new Bundle();
        ResourcesPageFragment page = new ResourcesPageFragment();
        args.putSerializable(DocumentEditActivity.PARAM_TEACHING_MATERIAL_DATA, knowledgeModel);
        args.putInt(PARAM_RESOURCE_TYPE, resourceCode);
        page.setListener(callback);
        page.setArguments(args);
        return page;
    }

    public void setListener(ResourceBaseFragment.Callback callback) {
        mListener = callback;
    }

    public void resetData(KnowledgeModel model) {
        Bundle args = new Bundle();
        args.putSerializable(DocumentEditActivity.PARAM_TEACHING_MATERIAL_DATA, model);
        setArguments(args);
        initParams();
        if (resourceType != -1) {
            request(mDefaultPage, LevelType.ALL_LEVEL, resourceType);
            rerequst(resourceType);
        } else {
            request(mDefaultPage, LevelType.ALL_LEVEL, mResourceType);
            rerequst(mResourceType);
        }
    }

    private void rerequst(final int resourceType){

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(mCurrentFragment != null && mCurrentFragment.getDataList()==null||mCurrentFragment.getDataList().size()==0){
                    request(mDefaultPage, LevelType.ALL_LEVEL, resourceType);
                }
            }
        },1000);
    }

    private void request(int page, int level, int type) {
        Log.e("mmc", mIsRequesting + "  mIsRequesting");
        if (mIsRequesting) {
            return;
        }
        mIsRequesting = true;
        if (type == ResourceType.QUESTION_TYPE) {
            new InitQuestionDataTask(page, level).execute();
        } else {
            new InitDataTask(page, level, type).execute();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.page_show_resource, container, false);
        initParams();
        initViews(view);
        initSpinner();
        return view;
    }

    private void initParams() {
        Bundle args = getArguments();
        if (args != null) {
            mKnowledgeModel = (KnowledgeModel) args.getSerializable(DocumentEditActivity.PARAM_TEACHING_MATERIAL_DATA);
            resourceType = getArguments().getInt(PARAM_RESOURCE_TYPE, resourceType);
        }
    }

    private void initViews(View view) {
        mPptTitleView = view.findViewById(R.id.ppt_resource_view);
        mMicroCourseTitleView = view.findViewById(R.id.micro_course_resource_view);
        mWordTitleView = view.findViewById(R.id.word_resource_view);
        mQuestionTitleView = view.findViewById(R.id.question_resource_view);
        mImageTitleView = view.findViewById(R.id.image_resource_view);
        mUploadFileTitleView = view.findViewById(R.id.upload_file_resource_view);
        tv_spinner = view.findViewById(R.id.tv_spinner);

        mPptTitleView.setOnClickListener(this);
        mMicroCourseTitleView.setOnClickListener(this);
        mWordTitleView.setOnClickListener(this);
        mQuestionTitleView.setOnClickListener(this);
        mImageTitleView.setOnClickListener(this);
        mUploadFileTitleView.setOnClickListener(this);
        tv_spinner.setOnClickListener(this);

        //若带有指定类型进来则选中指定类型，其余类型不可点击
        if(resourceType != -1) {
            switch (resourceType) {
                case ResourceType.PPT_TYPE:
                    showModulePage(resourceType, mPptTitleView, false);
                    break;
                case ResourceType.IMAGE_TYPE:
                    showModulePage(resourceType, mImageTitleView, false);
                    break;
                case ResourceType.WORD_TYPE:
                    showModulePage(resourceType, mWordTitleView, false);
                    break;
                case ResourceType.VIDEO_TYPE:
                    showModulePage(resourceType, mMicroCourseTitleView, false);
                    break;
                case ResourceType.QUESTION_TYPE:
                    showModulePage(resourceType, mQuestionTitleView, false);
                    break;
            }
        } else {
            showModulePage(ResourceType.PPT_TYPE, mPptTitleView);
        }
    }

    private void resetButtomStatus(boolean isChange) {
        if (isChange) {
            mPptTitleView.setSelected(false);
            mMicroCourseTitleView.setSelected(false);
            mWordTitleView.setSelected(false);
            mQuestionTitleView.setSelected(false);
            mImageTitleView.setSelected(false);
        } else {
            mPptTitleView.setEnabled(false);
            mMicroCourseTitleView.setEnabled(false);
            mWordTitleView.setEnabled(false);
            mQuestionTitleView.setEnabled(false);
            mImageTitleView.setEnabled(false);
        }
    }

    private void initSpinner() {
        final String[] spinnerItems = {getResources().getString(R.string.lbl_all), getResources().getString(R.string.lbl_res_school), getResources().getString(R.string.lbl_my_collect),getResources().getString(R.string.lbl_res_public)};
        List<String> listStr = new ArrayList<>();
        for (int i = 0;i<spinnerItems.length;i++){
            listStr.add(spinnerItems[i]);
        }
        if (popFliterRes == null){
            popFliterRes = new PopFliterRes(getActivity());
        }
        popFliterRes.setRootWidth((int) getResources().getDimension(R.dimen.dp_90));
        popFliterRes.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                tv_spinner.setText(popFliterRes.getDatas().get(position));
                if (mCurrentFragment != null) {
                    request(mDefaultPage, position, mResourceType);
                }
            }
        });
        popFliterRes.setDatas(listStr);
    }

    @Override
    public void itemChecked(ResourceModel resourceModel, QuestionModel questionModel) {
        if (resourceModel != null) {
            switch (resourceModel.resource_type) {
                case ResourceType.PPT_TYPE:
                case ResourceType.WORD_TYPE:
                case ResourceType.VIDEO_TYPE:
                case ResourceType.IMAGE_TYPE:
                    ResourceShowActivity.startMe(getActivity(), resourceModel);
                    break;
            }
        }
    }

    private class InitDataTask extends AsyncTask<Void, Void, List<ResourceModel>> {

        private int currentPage;
        private int level;
        private int type;

        public InitDataTask(int page, int level, int type) {
            this.currentPage = page;
            this.level = level;
            this.type = type;
        }

        @Override
        protected List<ResourceModel> doInBackground(Void... voids) {
            UserData userData = ExternalParam.getInstance().getUserData();
            if (userData.isTeacher()) {
                if (mKnowledgeModel != null) {
                    return ScopeServer.getInstance().QureyResourceByTeacherID(
                            mKnowledgeModel.teacher_id, mKnowledgeModel.teaching_material_id,
                            level, type, currentPage, mPageSize);
                }
            } else {
                return null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<ResourceModel> documentList) {
            if (mCurrentFragment == null) {
                mIsRequesting = false;
                return;
            }
            if (documentList != null && documentList.size() > 0) {
                if (currentPage == mDefaultPage) {
                    mCurrentFragment.clearListData();
                }
                mCurrentFragment.setListData(documentList);
            } else {
                mCurrentFragment.clearListData();
            }
            mIsRequesting = false;
        }
    }

    private class InitQuestionDataTask extends AsyncTask<Void, Void, List<QuestionModel>> {

        private int currentPage;
        private int level;

        public InitQuestionDataTask(int page, int level) {
            this.currentPage = page;
            this.level = level;
        }

        @Override
        protected List<QuestionModel> doInBackground(Void... voids) {
            UserData userData = ExternalParam.getInstance().getUserData();
            if (userData.isTeacher()) {
                if (mKnowledgeModel != null) {
                    return ScopeServer.getInstance().QureyQuestionResourceByTeacherID(
                            mKnowledgeModel.teacher_id, mKnowledgeModel.teaching_material_id,
                            level, currentPage, mPageSize);
                }
            } else {
                return null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<QuestionModel> questionDataList) {
            if (mCurrentFragment == null) {
                mIsRequesting = false;
                return;
            }
            if (questionDataList != null && questionDataList.size() > 0) {
                if (currentPage == mDefaultPage) {
                    mCurrentFragment.clearListData();
                }
                mCurrentFragment.setListData(questionDataList);
            } else {
                mCurrentFragment.clearListData();
            }
            mIsRequesting = false;
        }
    }

    private ResourceGrideFragment mPPTFragment;
    private ResourceGrideFragment mVideoFragment;
    private ResourceGrideFragment mWordFragment;
    private ResourceGrideFragment mImageFragment;
    private QuestionResourceFragment mQuestionFragment;
    private ResourceBaseFragment mCurrentFragment;

    /**
     * [展示指定Id的页面]<BR>
     */
    public void showModulePage(int type, View view, boolean isChange) {
        if (mCurrentFragment != null && mResourceType == type) {
            return;
        }
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        ResourceBaseFragment moduleFragment = null;
        if (type == ResourceType.PPT_TYPE) { //PPT
            if (mPPTFragment == null) {
                if (mListener != null) {
                    mPPTFragment = ResourceGrideFragment.newInstance(true, mListener);
                } else {
                    mPPTFragment = ResourceGrideFragment.newInstance(false, this);
                }
                transaction.add(ROOT_LAYOUT_ID, mPPTFragment);
                request(mDefaultPage, LevelType.ALL_LEVEL, ResourceType.PPT_TYPE);
            }
            moduleFragment = mPPTFragment;

        } else if (type == ResourceType.VIDEO_TYPE) { //微课
            if (mVideoFragment == null) {
                if (mListener != null) {
                    mVideoFragment = ResourceGrideFragment.newInstance(true, mListener);
                } else {
                    mVideoFragment = ResourceGrideFragment.newInstance(false, this);
                }
                transaction.add(ROOT_LAYOUT_ID, mVideoFragment);
                request(mDefaultPage, LevelType.ALL_LEVEL, ResourceType.VIDEO_TYPE);
            }
            moduleFragment = mVideoFragment;
        } else if (type == ResourceType.WORD_TYPE) {
            if (mWordFragment == null) {
                if (mListener != null) {
                    mWordFragment = ResourceGrideFragment.newInstance(true, mListener);
                } else {
                    mWordFragment = ResourceGrideFragment.newInstance(false, this);
                }
                transaction.add(ROOT_LAYOUT_ID, mWordFragment);
                request(mDefaultPage, LevelType.ALL_LEVEL, ResourceType.WORD_TYPE);
            }
            moduleFragment = mWordFragment;
        } else if (type == ResourceType.IMAGE_TYPE) {
            if (mImageFragment == null) {
                if (mListener != null) {
                    mImageFragment = ResourceGrideFragment.newInstance(true, mListener);
                } else {
                    mImageFragment = ResourceGrideFragment.newInstance(false, this);
                }
                transaction.add(ROOT_LAYOUT_ID, mImageFragment);
                request(mDefaultPage, LevelType.ALL_LEVEL, ResourceType.IMAGE_TYPE);
            }
            moduleFragment = mImageFragment;
        } else if (type == ResourceType.QUESTION_TYPE) {
            if (mQuestionFragment == null) {
                if (mListener != null) {
                    mQuestionFragment = QuestionResourceFragment.newInstance(mListener);
                } else {
                    mQuestionFragment = QuestionResourceFragment.newInstance();
                }
                transaction.add(ROOT_LAYOUT_ID, mQuestionFragment);
                request(mDefaultPage, LevelType.ALL_LEVEL, ResourceType.QUESTION_TYPE);
            }
            moduleFragment = mQuestionFragment;
        } else {
            return;
        }

        if (mCurrentFragment != null) {
            transaction.hide(mCurrentFragment);
        }

        resetButtomStatus(isChange);
        view.setEnabled(true);
        view.setSelected(true);
        transaction.show(moduleFragment);
        transaction.commitAllowingStateLoss();
        mCurrentFragment = moduleFragment;
        mResourceType = type;
    }

    public void showModulePage(int type, View view) {
        showModulePage(type, view, true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ppt_resource_view:
                showModulePage(ResourceType.PPT_TYPE, v);
                break;
            case R.id.micro_course_resource_view:
                showModulePage(ResourceType.VIDEO_TYPE, v);
                break;
            case R.id.question_resource_view://习题
                showModulePage(ResourceType.QUESTION_TYPE, v);
                break;
            case R.id.word_resource_view:  //文档
                showModulePage(ResourceType.WORD_TYPE, v);
                break;
            case R.id.image_resource_view:
                showModulePage(ResourceType.IMAGE_TYPE, v);
                break;
            case R.id.upload_file_resource_view:

                break;
            case R.id.tv_spinner: //筛选
                popFliterRes.showMask(false).showAsDropDown(v);
                break;
        }
    }
}
