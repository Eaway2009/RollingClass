package com.tanhd.rollingclass.fragments.resource;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Spinner;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.activity.DocumentEditActivity;
import com.tanhd.rollingclass.db.KeyConstants.LevelType;
import com.tanhd.rollingclass.db.KeyConstants.ResourceType;
import com.tanhd.rollingclass.fragments.pages.DocumentsPageFragment;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.KnowledgeModel;
import com.tanhd.rollingclass.server.data.ResourceModel;
import com.tanhd.rollingclass.server.data.UserData;
import com.tanhd.rollingclass.views.ResourceAdapter;

import java.util.List;

public class ResourcesPageFragment extends Fragment implements View.OnClickListener {

    private View mPptTitleView;
    private View mWordTitleView;
    private View mImageTitleView;
    private View mMicroCourseTitleView;
    private View mQuestionTitleView;
    private View mUploadFileTitleView;
//    private GridView mGridView;
//    private ResourceAdapter mAdapter;
    private KnowledgeModel mKnowledgeModel;
    private Spinner mSpinnerSimple;
    Animation myAnimation;

    private int mLevel = LevelType.ALL_LEVEL; //1 公共资源 2 校本资源 3 私藏资源
    private int mResourceType = ResourceType.PPT_TYPE;//1. ppt 2 doc 3 image 4 微课 5 习题
    private int mCurrentPage = 1;
    private final int mDefaultPage = 1;
    private final int mPageSize = 30;
    private static final int ROOT_LAYOUT_ID = R.id.fragment_container;

    public static ResourcesPageFragment newInstance(KnowledgeModel knowledgeModel) {
        Bundle args = new Bundle();
        ResourcesPageFragment page = new ResourcesPageFragment();
        args.putSerializable(DocumentEditActivity.PARAM_TEACHING_MATERIAL_DATA, knowledgeModel);
        page.setArguments(args);
        return page;
    }

    public void resetData(KnowledgeModel model) {
        Bundle args = new Bundle();
        args.putSerializable(DocumentEditActivity.PARAM_TEACHING_MATERIAL_DATA, model);
        setArguments(args);
        initParams();
        request(mDefaultPage, LevelType.ALL_LEVEL, ResourceType.PPT_TYPE);
    }

    private void request(int page, int level, int type) {
        new InitDataTask(page, level, type).execute();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.page_show_resource, container, false);
        initParams();
        initViews(view);
        initSpinner();
        showModulePage(ResourceType.PPT_TYPE);
        return view;
    }

    private void initParams() {
        Bundle args = getArguments();
        if (args != null) {
            mKnowledgeModel = (KnowledgeModel) args.getSerializable(DocumentEditActivity.PARAM_TEACHING_MATERIAL_DATA);
        }
    }

    private void initViews(View view) {
        mPptTitleView = view.findViewById(R.id.ppt_resource_view);
        mMicroCourseTitleView = view.findViewById(R.id.micro_course_resource_view);
        mWordTitleView = view.findViewById(R.id.word_resource_view);
        mQuestionTitleView = view.findViewById(R.id.question_resource_view);
        mImageTitleView = view.findViewById(R.id.image_resource_view);
        mUploadFileTitleView = view.findViewById(R.id.upload_file_resource_view);
        mSpinnerSimple = view.findViewById(R.id.resource_level_spinner);

        mPptTitleView.setOnClickListener(this);
        mMicroCourseTitleView.setOnClickListener(this);
        mWordTitleView.setOnClickListener(this);
        mQuestionTitleView.setOnClickListener(this);
        mImageTitleView.setOnClickListener(this);
        mUploadFileTitleView.setOnClickListener(this);
    }

    private void initSpinner() {
        myAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.spinner_anim);
        mSpinnerSimple.setDropDownWidth(400); //下拉宽度
        mSpinnerSimple.setDropDownHorizontalOffset(100); //下拉的横向偏移
        mSpinnerSimple.setDropDownVerticalOffset(100); //下拉的纵向偏移
        //mSpinnerSimple.setBackgroundColor(AppUtil.getColor(instance,R.color.wx_bg_gray)); //下拉的背景色
        //spinner mode ： dropdown or dialog , just edit in layout xml

        final String[] spinnerItems = {"全部", "校本资源", "我的收藏", "公共资源"};
        //自定义选择填充后的字体样式
        //只能是textview样式，否则报错：ArrayAdapter requires the resource ID to be a TextView
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, spinnerItems);
        //自定义下拉的字体样式
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_resource_dropdown);
        //这个在不同的Theme下，显示的效果是不同的
        //spinnerAdapter.setDropDownViewTheme(Theme.LIGHT);
        mSpinnerSimple.setAdapter(spinnerAdapter);

        mSpinnerSimple.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                    int arg2, long arg3) {
                mSpinnerSimple.setPrompt(spinnerItems[arg2]);
                request(mDefaultPage, arg2, mCurrentPage);
            }

            public void onNothingSelected(AdapterView<?> arg0) {
                mSpinnerSimple.setPrompt(spinnerItems[0]);
            }
        });
        /* 下拉菜单弹出的内容选项触屏事件处理 */
        mSpinnerSimple.setOnTouchListener(new Spinner.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
//                v.startAnimation(myAnimation);
                return false;
            }
        });
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
                if(mKnowledgeModel!=null){
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
            if (documentList != null && documentList.size() > 0 && mCurrentFragment != null) {
                if (mResourceType != ResourceType.QUESTION_TYPE) {
                    ((ResourceGrideFragment) mCurrentFragment).setListData(documentList);
                } else {
                    // TODO: 2019/8/31 习题的fragment，其余类别用上面那个fragment
                }
            } else {
                return;
            }
        }
    }

    private ResourceGrideFragment mPPTFragment;
    private ResourceGrideFragment mWordFragment;
    private Fragment mCurrentFragment;
    /**
     * [展示指定Id的页面]<BR>
     */
    public void showModulePage(int type) {
        if (mResourceType == type) {
            return;
        }
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        Fragment moduleFragment = null;
        if (mResourceType == ResourceType.PPT_TYPE) {
            if (mPPTFragment == null) {
                mPPTFragment = ResourceGrideFragment.newInstance();
                transaction.add(ROOT_LAYOUT_ID, mPPTFragment);
                request(mDefaultPage, LevelType.ALL_LEVEL, ResourceType.PPT_TYPE);
            }
            moduleFragment = mPPTFragment;

        } else if (mResourceType == ResourceType.WORD_TYPE) {
            if (mWordFragment == null) {
                mWordFragment = ResourceGrideFragment.newInstance();
                transaction.add(ROOT_LAYOUT_ID, mWordFragment);
                request(mDefaultPage, LevelType.ALL_LEVEL, ResourceType.PPT_TYPE);
            }
            moduleFragment = mWordFragment;
        }

        if (mCurrentFragment != null) {
            transaction.hide(mCurrentFragment);
        }

        transaction.show(moduleFragment);
        transaction.commitAllowingStateLoss();
        mCurrentFragment = moduleFragment;
        mResourceType = type;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ppt_resource_view:
                mResourceType = ResourceType.PPT_TYPE;
                showModulePage(mResourceType);
                break;
            case R.id.word_resource_view:
                mResourceType = ResourceType.WORD_TYPE;
                showModulePage(mResourceType);
                break;
            case R.id.image_resource_view:
                mResourceType = ResourceType.IMAGE_TYPE;
                showModulePage(mResourceType);
                break;
            case R.id.question_resource_view:
                break;
            case R.id.micro_course_resource_view:
                break;
            case R.id.upload_file_resource_view:
                break;
        }
    }
}
