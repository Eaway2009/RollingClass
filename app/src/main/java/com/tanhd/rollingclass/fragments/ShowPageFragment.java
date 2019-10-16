package com.tanhd.rollingclass.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.activity.DatasActivity;
import com.tanhd.rollingclass.fragments.statistics.StudentStatisticsFragment;
import com.tanhd.rollingclass.server.data.KnowledgeModel;
import com.tanhd.rollingclass.fragments.pages.ChaptersFragment;
import com.tanhd.rollingclass.fragments.pages.DocumentsPageFragment;
import com.tanhd.rollingclass.fragments.resource.ResourcesPageFragment;
import com.tanhd.rollingclass.fragments.statistics.StatisticsPageFragment;

/**
 * 学案|资源|学情
 */
public class ShowPageFragment extends Fragment implements View.OnClickListener, ChaptersFragment.ChapterListener {

    private PagesListener mListener;
    private TextView mDocumentView;
    private TextView mResourceView;
    private TextView mStatisticsView;
    private View mTitleView;
    private View mWrongAnswerTitleView;
    private ChaptersFragment mChapterFragment;
    private int mCurrentShowModuleId = -1;
    private static final int MODULE_ID_DOCUMENTS = 0;
    private static final int MODULE_ID_RESOURCES = 1;
    private static final int MODULE_ID_STATISTICS = 2;
    private static final int MODULE_ID_WRONG_ANSWER = 3;
    private static final int MODULE_ID_ANSWER_STATISTICS = 4;
    private static final int ROOT_LAYOUT_ID = R.id.content_layout;
    private DocumentsPageFragment mDocumentsFragment;
    private ResourcesPageFragment mResourcesFragment;
    private StatisticsPageFragment mStatisticsFragment;
    private WrongAnswerListFragment mWrongAnswerListFragment;
    private StudentStatisticsFragment mStudentStatisticsFragment;
    private int mPageId;
    private boolean mIsStudentPage;
    private KnowledgeModel mKnowledgeModel;
    private Fragment mModuleFragment;

    public static ShowPageFragment newInstance(int pageId, boolean student, ShowPageFragment.PagesListener listener) {
        Bundle args = new Bundle();
        args.putInt(DatasActivity.PAGE_ID, pageId);
        args.putBoolean(DatasActivity.PAGE_TYPE, student);
        ShowPageFragment page = new ShowPageFragment();
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
        View view = inflater.inflate(R.layout.page_show_pages, container, false);
        initParams();
        showModulePage(mPageId);
        initViews(view);
        return view;
    }

    private void initParams() {
        mPageId = getArguments().getInt(DatasActivity.PAGE_ID, DatasActivity.PAGE_ID_DOCUMENTS);
        mIsStudentPage = getArguments().getBoolean(DatasActivity.PAGE_TYPE, false);
    }

    private void initViews(View view) {
        mTitleView = view.findViewById(R.id.title_view);
        mDocumentView = view.findViewById(R.id.document_textview);
        mResourceView = view.findViewById(R.id.resource_textview);
        mStatisticsView = view.findViewById(R.id.statistics_textview);
        if (mIsStudentPage) {
            mDocumentView.setText(R.string.learning_seft);
            mResourceView.setVisibility(View.GONE);
        }

        view.findViewById(R.id.back_button).setOnClickListener(this);
        mDocumentView.setOnClickListener(this);
        mResourceView.setOnClickListener(this);
        mStatisticsView.setOnClickListener(this);

        mChapterFragment = ChaptersFragment.newInstance(this);
        getFragmentManager().beginTransaction().replace(R.id.fragment_chapter_menu, mChapterFragment).commit();

        changeToFragment(mPageId);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            resetData();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.document_textview: //学案 or 自学
                changeToFragment(MODULE_ID_DOCUMENTS);
                break;
            case R.id.resource_textview: //资源
                changeToFragment(MODULE_ID_RESOURCES);
                break;
            case R.id.statistics_textview: //学情
                changeToFragment(MODULE_ID_STATISTICS);
                break;
            case R.id.back_button:
                if (mListener != null) {
                    mListener.onBack();
                }
                break;
        }
    }

    private void changeToFragment(int pageId) {
        showModulePage(pageId);
        mDocumentView.setEnabled(true);
        mResourceView.setEnabled(true);
        mStatisticsView.setEnabled(true);
        switch (pageId) {
            case MODULE_ID_DOCUMENTS:
                mDocumentView.setEnabled(false);
                break;
            case MODULE_ID_RESOURCES:
                mResourceView.setEnabled(false);
                break;
            case MODULE_ID_STATISTICS:
                mStatisticsView.setEnabled(false);
                break;
        }
    }

    /**
     * [展示指定Id的页面]<BR>
     */
    public void showModulePage(int moduleId) {
        if (mCurrentShowModuleId == moduleId) {
            return;
        }
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        if (moduleId == MODULE_ID_DOCUMENTS) { //学案
            if (mDocumentsFragment == null) {
                mDocumentsFragment = DocumentsPageFragment.newInstance(new DocumentsPageFragment.DocumentListener() {
                    @Override
                    public void onDocumentClicked(int documentId) {
                    }

                    @Override
                    public void onOpenWrongListBook(KnowledgeModel model) {
                        mTitleView.setVisibility(View.GONE);
                        showModulePage(MODULE_ID_WRONG_ANSWER);
                    }
                });
                transaction.add(ROOT_LAYOUT_ID, mDocumentsFragment);
            }
            if (mModuleFragment != null) {
                transaction.hide(mModuleFragment);
            }
            mModuleFragment = mDocumentsFragment;
        } else if (moduleId == MODULE_ID_RESOURCES) {  //资源
            if (mResourcesFragment == null) {
                mResourcesFragment = ResourcesPageFragment.newInstance(mKnowledgeModel);
                transaction.add(ROOT_LAYOUT_ID, mResourcesFragment);
            }
            if (mModuleFragment != null) {
                transaction.hide(mModuleFragment);
            }
            mModuleFragment = mResourcesFragment;
        } else if (moduleId == MODULE_ID_STATISTICS) {  //学情
            if (mStatisticsFragment == null) {
                mStatisticsFragment = StatisticsPageFragment.newInstance(mKnowledgeModel, new StatisticsPageFragment.Callback() {
                    @Override
                    public void onOpenStatistics(KnowledgeModel model) {
                        mTitleView.setVisibility(View.GONE);
                        showModulePage(MODULE_ID_ANSWER_STATISTICS);
                    }
                });
                transaction.add(ROOT_LAYOUT_ID, mStatisticsFragment);
            }
            if (mModuleFragment != null) {
                transaction.hide(mModuleFragment);
            }
            mModuleFragment = mStatisticsFragment;
        } else if (moduleId == MODULE_ID_WRONG_ANSWER) {  //错题本
            if (mWrongAnswerListFragment == null) {
                mWrongAnswerListFragment = WrongAnswerListFragment.newInstance(mKnowledgeModel, new WrongAnswerListFragment.Callback() {
                    @Override
                    public void onBack() {
                        mTitleView.setVisibility(View.VISIBLE);
                        changeToFragment(MODULE_ID_DOCUMENTS);
                    }
                });
                transaction.add(ROOT_LAYOUT_ID, mWrongAnswerListFragment);
            }
            if (mModuleFragment != null) {
                transaction.hide(mModuleFragment);
            }
            mModuleFragment = mWrongAnswerListFragment;
        } else if (moduleId == MODULE_ID_ANSWER_STATISTICS) {  //习题数据
            if (mStudentStatisticsFragment == null) {
                mStudentStatisticsFragment = StudentStatisticsFragment.newInstance(mKnowledgeModel, new StudentStatisticsFragment.Callback() {
                    @Override
                    public void onBack() {
                        mTitleView.setVisibility(View.GONE);
                        changeToFragment(MODULE_ID_STATISTICS);
                    }
                });
                transaction.add(ROOT_LAYOUT_ID, mStudentStatisticsFragment);
            }
            if (mModuleFragment != null) {
                transaction.hide(mModuleFragment);
            }
            mModuleFragment = mStudentStatisticsFragment;
        }
        transaction.show(mModuleFragment);
        transaction.commitAllowingStateLoss();

        mCurrentShowModuleId = moduleId;
    }

    @Override
    public void onTeacherCheckChapter(String school_id, String teacher_id, String chapter_id, String chapter_name, String section_id, String section_name, int subject_code, String subject_name, String teaching_material_id) {
        mKnowledgeModel = new KnowledgeModel(school_id, teacher_id, chapter_id, chapter_name, section_id, section_name, subject_code, subject_name, teaching_material_id, null);
        resetData();
    }

    @Override
    public void onStudentCheckChapter(String school_id, String class_id, String chapter_id, String chapter_name, String section_id, String section_name, int subject_code, String subject_name, String teaching_material_id) {
        mKnowledgeModel = new KnowledgeModel(school_id, null, chapter_id, chapter_name, section_id, section_name, subject_code, subject_name, teaching_material_id, class_id);
        resetData();
    }

    public void resetDocumentsData() {
        if (mCurrentShowModuleId == MODULE_ID_DOCUMENTS) {
            mDocumentsFragment.resetData(mKnowledgeModel);
        }
    }

    private void resetData() {
        if (mDocumentsFragment != null) {
            mDocumentsFragment.resetData(mKnowledgeModel);
        }
        if (mResourcesFragment != null) {
            mResourcesFragment.resetData(mKnowledgeModel);
        }
        if (mStatisticsFragment != null) {
            mStatisticsFragment.resetData(mKnowledgeModel);
        }
        if (mWrongAnswerListFragment != null) {
            mWrongAnswerListFragment.resetData(mKnowledgeModel);
        }
    }

    public interface PagesListener {
        void onPageChange(int id);

        void onBack();
    }

}
