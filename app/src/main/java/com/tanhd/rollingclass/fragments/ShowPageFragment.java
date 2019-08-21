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
import com.tanhd.rollingclass.fragments.pages.ChaptersFragment;
import com.tanhd.rollingclass.fragments.pages.DocumentsPageFragment;
import com.tanhd.rollingclass.fragments.pages.ResourcesPageFragment;
import com.tanhd.rollingclass.fragments.pages.StatisticsPageFragment;

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

    private void initParams(){
        mPageId = getArguments().getInt(DatasActivity.PAGE_ID, DatasActivity.PAGE_ID_DOCUMENTS);
    }

    private void initViews(View view){
        mDocumentView = view.findViewById(R.id.document_textview);
        mResourceView = view.findViewById(R.id.resource_textview);
        mStatisticsView = view.findViewById(R.id.statistics_textview);

        view.findViewById(R.id.back_button).setOnClickListener(this);
        mDocumentView.setOnClickListener(this);
        mResourceView.setOnClickListener(this);
        mStatisticsView.setOnClickListener(this);

        mDocumentsFragment = new DocumentsPageFragment();
        mResourcesFragment = new ResourcesPageFragment();
        mStatisticsFragment = new StatisticsPageFragment();

        mChapterFragment = ChaptersFragment.newInstance(this);
        getFragmentManager().beginTransaction().replace(R.id.fragment_chapter_menu, mChapterFragment).commit();

        mChapterFragment.refreshData();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.document_textview:
                showModulePage(MODULE_ID_DOCUMENTS);
                break;
            case R.id.resource_textview:
                showModulePage(MODULE_ID_RESOURCES);
                break;
            case R.id.statistics_textview:
                showModulePage(MODULE_ID_STATISTICS);
                break;
            case R.id.back_button:
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
                mDocumentsFragment = new DocumentsPageFragment();
                transaction.add(ROOT_LAYOUT_ID, mDocumentsFragment);
            }
            moduleFragment = mDocumentsFragment;
            if (mResourcesFragment != null) {
                transaction.hide(mResourcesFragment);
            }
            if (mStatisticsFragment != null) {
                transaction.hide(mStatisticsFragment);
            }
        } else if(moduleId == MODULE_ID_RESOURCES) {
            if (mResourcesFragment == null) {
                mResourcesFragment = new ResourcesPageFragment();
                transaction.add(ROOT_LAYOUT_ID, mResourcesFragment);
            }
            moduleFragment = mResourcesFragment;
            if (mDocumentsFragment != null) {
                transaction.hide(mDocumentsFragment);
            }
            if (mStatisticsFragment != null) {
                transaction.hide(mStatisticsFragment);
            }
        }else if(moduleId == MODULE_ID_STATISTICS) {
            if (mStatisticsFragment == null) {
                mStatisticsFragment = new StatisticsPageFragment();
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
    public void onCheckChapter(long chapterId) {
        mDocumentsFragment.reRequestData(chapterId);
//        mResourcesFragment.reRequestData(chapterId);
//        mStatisticsFragment.reRequestData(chapterId);
    }

    public interface PagesListener{
        void onPageChange(int id);
    }

}
