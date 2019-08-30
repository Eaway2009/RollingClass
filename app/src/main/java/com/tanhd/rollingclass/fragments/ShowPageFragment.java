package com.tanhd.rollingclass.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.activity.DatasActivity;
import com.tanhd.rollingclass.server.data.KnowledgeModel;
import com.tanhd.rollingclass.fragments.pages.ChaptersFragment;
import com.tanhd.rollingclass.fragments.pages.DocumentsPageFragment;
import com.tanhd.rollingclass.fragments.resource.ResourcesPageFragment;
import com.tanhd.rollingclass.fragments.statistics.StatisticsPageFragment;

public class ShowPageFragment extends Fragment implements View.OnClickListener, ChaptersFragment.ChapterListener {

    private PagesListener mListener;
    private View mDocumentView;
    private View mResourceView;
    private View mStatisticsView;
    private ChaptersFragment mChapterFragment;
    private int mCurrentShowModuleId = -1;
    private static final int MODULE_ID_DOCUMENTS = 0;
    private static final int MODULE_ID_RESOURCES = 1;
    private static final int MODULE_ID_STATISTICS = 2;
    private static final int ROOT_LAYOUT_ID = R.id.content_layout;
    private DocumentsPageFragment mDocumentsFragment;
    private ResourcesPageFragment mResourcesFragment;
    private StatisticsPageFragment mStatisticsFragment;
    private int mPageId;
    private KnowledgeModel mKnowledgeModel;

    public static ShowPageFragment newInstance(int pageId, ShowPageFragment.PagesListener listener) {
        Bundle args = new Bundle();
        args.putInt(DatasActivity.PAGE_ID, pageId);
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
    }

    private void initViews(View view) {
        mDocumentView = view.findViewById(R.id.document_textview);
        mResourceView = view.findViewById(R.id.resource_textview);
        mStatisticsView = view.findViewById(R.id.statistics_textview);

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
        if(isVisibleToUser){
            resetData();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.document_textview:
                changeToFragment(MODULE_ID_DOCUMENTS);
                break;
            case R.id.resource_textview:
                changeToFragment(MODULE_ID_RESOURCES);
                break;
            case R.id.statistics_textview:
                changeToFragment(MODULE_ID_STATISTICS);
                break;
            case R.id.back_button:
                if (mListener != null) {
                    mListener.onBack();
                }
                break;
        }
    }

    private void changeToFragment(int pageId){
        showModulePage(pageId);
        mDocumentView.setEnabled(true);
        mResourceView.setEnabled(true);
        mStatisticsView.setEnabled(true);
        switch (pageId){
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
        Fragment moduleFragment = null;
        if (moduleId == MODULE_ID_DOCUMENTS) {
            if (mDocumentsFragment == null) {
                mDocumentsFragment = DocumentsPageFragment.newInstance(new DocumentsPageFragment.DocumentListener() {
                    @Override
                    public void onDocumentClicked(int documentId) {

                    }
                });
                transaction.add(ROOT_LAYOUT_ID, mDocumentsFragment);
            }
            moduleFragment = mDocumentsFragment;
            if (mResourcesFragment != null) {
                transaction.hide(mResourcesFragment);
            }
            if (mStatisticsFragment != null) {
                transaction.hide(mStatisticsFragment);
            }
        } else if (moduleId == MODULE_ID_RESOURCES) {
            if (mResourcesFragment == null) {
                mResourcesFragment = ResourcesPageFragment.newInstance();
                transaction.add(ROOT_LAYOUT_ID, mResourcesFragment);
            }
            moduleFragment = mResourcesFragment;
            if (mDocumentsFragment != null) {
                transaction.hide(mDocumentsFragment);
            }
            if (mStatisticsFragment != null) {
                transaction.hide(mStatisticsFragment);
            }
        } else if (moduleId == MODULE_ID_STATISTICS) {
            if (mStatisticsFragment == null) {
                mStatisticsFragment = StatisticsPageFragment.newInstance();
                transaction.add(ROOT_LAYOUT_ID, mStatisticsFragment);
            }
            moduleFragment = mStatisticsFragment;
            if (mDocumentsFragment != null) {
                transaction.hide(mDocumentsFragment);
            }
            if (mResourcesFragment != null) {
                transaction.hide(mResourcesFragment);
            }
        }
        transaction.show(moduleFragment);
        transaction.commitAllowingStateLoss();

        mCurrentShowModuleId = moduleId;
    }


    @Override
    public void onCheckChapter(String school_id, String teacher_id, String chapter_id, String chapter_name, String section_id, String section_name, int subject_code, String subject_name, String teaching_material_id) {
        mKnowledgeModel = new KnowledgeModel(school_id, teacher_id, chapter_id, chapter_name, section_id, section_name, subject_code, subject_name, teaching_material_id);
        resetData();
    }

    private void resetData(){
        if(mDocumentsFragment!=null) {
            mDocumentsFragment.resetData(mKnowledgeModel);
        }
        if(mResourcesFragment!=null) {
            mResourcesFragment.resetData(mKnowledgeModel);
        }
        if(mStatisticsFragment!=null) {
            mStatisticsFragment.resetData(mKnowledgeModel);
        }
    }

    public interface PagesListener {
        void onPageChange(int id);

        void onBack();
    }

}
