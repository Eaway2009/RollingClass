package com.tanhd.rollingclass.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.base.BaseActivity;
import com.tanhd.rollingclass.fragments.WrongAnswerBookPhotoFragment;
import com.tanhd.rollingclass.fragments.WrongAnswerListFragment;
import com.tanhd.rollingclass.fragments.pages.ChaptersFragment;
import com.tanhd.rollingclass.server.data.KnowledgeModel;

/**
 * 错题集列表
 */
public class WrongAnswerListActivity extends BaseActivity implements View.OnClickListener, WrongAnswerListFragment.Callback,ChaptersFragment.ChapterListener {

    private static final int MODULE_ID_QUESTION_PAGE = 1;
    private static final int MODULE_ID_PHOTO_PAGE = 2;

    private View mBackButton;
    private View mQuestionListButton;
    private View mPhotoListButton;

    private int mCurrentShowModuleId = -1;

    private WrongAnswerListFragment mWrongAnswerListFragment;
    private WrongAnswerBookPhotoFragment mWrongAnswerBookPhotoFragment;
    private ChaptersFragment mChapterFragment;
    private KnowledgeModel mKnowledgeModel;

    public static void startMe(Fragment context) {
        Intent intent = new Intent();
        intent.setClass(context.getActivity(), DocumentEditActivity.class);
        context.startActivityForResult(intent, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
        initFragment();
    }

    private void initViews() {
        setContentView(R.layout.page_wrong_answer_list);

        mBackButton = findViewById(R.id.back_button);
        mPhotoListButton = findViewById(R.id.photo_list);
        mQuestionListButton = findViewById(R.id.question_list);

        mBackButton.setOnClickListener(this);
        mPhotoListButton.setOnClickListener(this);
        mQuestionListButton.setOnClickListener(this);
    }

    private void initFragment() {
        mChapterFragment = ChaptersFragment.newInstance(this);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_chapter_menu, mChapterFragment).commit();
    }

    private void resetData() {
        if (mWrongAnswerListFragment != null) {
            mWrongAnswerListFragment.resetData(mKnowledgeModel);
        }
        if (mWrongAnswerBookPhotoFragment != null) {
            mWrongAnswerBookPhotoFragment.resetData(mKnowledgeModel);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.question_list:
                showQuestionListFragment();
                break;
            case R.id.photo_list:
                showPhotoListFragment();
                break;
        }
    }


    public void showQuestionListFragment() {
        if (mCurrentShowModuleId == MODULE_ID_QUESTION_PAGE) {
            return;
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (mWrongAnswerListFragment == null) {
            mWrongAnswerListFragment = WrongAnswerListFragment.newInstance(mKnowledgeModel,this);
            transaction.add(R.id.content_layout, mWrongAnswerListFragment);
        }
        if (mWrongAnswerBookPhotoFragment != null) {
            transaction.hide(mWrongAnswerBookPhotoFragment);
        }
        transaction.show(mWrongAnswerListFragment);
        transaction.commitAllowingStateLoss();
        mCurrentShowModuleId = MODULE_ID_QUESTION_PAGE;
    }

    /**
     * [展示指定Id的页面]<BR>
     */
    public void showPhotoListFragment() {
        if (mCurrentShowModuleId == MODULE_ID_PHOTO_PAGE) {
            return;
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (mWrongAnswerBookPhotoFragment == null) {
            mWrongAnswerBookPhotoFragment = WrongAnswerBookPhotoFragment.newInstance(mKnowledgeModel, new WrongAnswerBookPhotoFragment.Callback() {
                @Override
                public void onBack() {

                }
            });
            transaction.add(R.id.content_layout, mWrongAnswerBookPhotoFragment);
        }
        if (mWrongAnswerListFragment != null) {
            transaction.hide(mWrongAnswerListFragment);
        }
        transaction.show(mWrongAnswerBookPhotoFragment);
        transaction.commitAllowingStateLoss();

        mCurrentShowModuleId = MODULE_ID_PHOTO_PAGE;
    }

    @Override
    public void onBack() {
        backToList();
    }

    @Override
    public void onBackPressed() {
        backToList();
    }

    private void backToList() {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onTeacherCheckChapter(String school_id, String teacher_id, String chapter_id, String chapter_name, String section_id, String section_name, int subject_code, String subject_name, String teaching_material_id) {

    }

    @Override
    public void onStudentCheckChapter(String school_id, String class_id, String chapter_id, String chapter_name, String section_id, String section_name, int subject_code, String subject_name, String teaching_material_id) {
        mKnowledgeModel = new KnowledgeModel(school_id, null, chapter_id, chapter_name, section_id, section_name, subject_code, subject_name, teaching_material_id, class_id);
        resetData();
    }
}
