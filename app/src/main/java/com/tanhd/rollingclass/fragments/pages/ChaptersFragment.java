package com.tanhd.rollingclass.fragments.pages;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.db.ChaptersResponse;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.views.ChaptersAdapter;

import java.util.List;

public class ChaptersFragment extends Fragment implements ExpandableListView.OnChildClickListener {
    private ExpandableListView mExpandableListView;
    private ChaptersAdapter mAdapter;
    private ChapterListener mListener;

    public static ChaptersFragment newInstance(ChaptersFragment.ChapterListener listener) {
        Bundle args = new Bundle();
        ChaptersFragment page = new ChaptersFragment();
        page.setArguments(args);
        page.setListener(listener);
        return page;
    }

    public void setListener(ChapterListener listener) {
        mListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.page_chapters, container, false);

        mExpandableListView = view.findViewById(R.id.expandable_listview);
        mAdapter = new ChaptersAdapter(getActivity());
        mExpandableListView.setVerticalScrollBarEnabled(false);
        mExpandableListView.setGroupIndicator(null);
        mExpandableListView.setHeaderDividersEnabled(false);
        mExpandableListView.setAdapter(mAdapter);
        mExpandableListView.setOnChildClickListener(this);
        return view;
    }

    public void refreshData() {
        new InitDataTask().execute();
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        if (mAdapter.getGroup(groupPosition).getChildren() != null) {
            if (childPosition < mAdapter.getGroup(groupPosition).getChildren().size()) {
                ChaptersResponse.Chapter chapter = mAdapter.getGroup(groupPosition).getChildren().get(childPosition);
                chapter.isChecked = true;
                if (mListener != null) {
                    mListener.onCheckChapter(chapter.chapterId);
                }
            }
        }
        return false;
    }

    private class InitDataTask extends AsyncTask<Void, Void, List<ChaptersResponse.Category>> {

        @Override
        protected List<ChaptersResponse.Category> doInBackground(Void... voids) {
            List<ChaptersResponse.Category> documentList = ScopeServer.getInstance().QureyChapters(123);
            if (documentList == null)
                return null;

            return documentList;
        }

        @Override
        protected void onPostExecute(List<ChaptersResponse.Category> dataList) {
            mAdapter.setDataList(dataList);

            if (mAdapter.getGroupCount() == 0) {
                try {
                    Toast.makeText(getActivity().getApplicationContext(), "没有找到章节!", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                }
                return;
            }

            // 将第一项设置成默认展开;
            if (mAdapter.getGroupCount() > 0 && mAdapter.getGroup(0) != null) {
                mExpandableListView.expandGroup(0);
                if(mAdapter.getGroup(0).getChildren().size()>0){
                    ChaptersResponse.Chapter firstItem = mAdapter.getGroup(0).getChildren().get(0);
                    firstItem.isChecked = true;
                }
            }
            mAdapter.notifyDataSetChanged();

        }
    }

    public interface ChapterListener {
        void onCheckChapter(long chapterId);
    }

}
