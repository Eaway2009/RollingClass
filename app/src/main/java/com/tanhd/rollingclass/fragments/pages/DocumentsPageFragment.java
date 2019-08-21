package com.tanhd.rollingclass.fragments.pages;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.Toast;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.db.Document;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.views.DocumentAdapter;

import java.util.List;

/**
 * 备课页面
 */
public class DocumentsPageFragment extends Fragment implements View.OnClickListener {

    private View mAddDocumentView;
    private DocumentListener mListener;
    private GridView mGridView;
    private DocumentAdapter mAdapter;
    private long mChapterId;

    public static DocumentsPageFragment newInstance(DocumentsPageFragment.DocumentListener listener) {
        Bundle args = new Bundle();
        DocumentsPageFragment page = new DocumentsPageFragment();
        page.setArguments(args);
        page.setListener(listener);
        return page;
    }

    private void setListener(DocumentsPageFragment.DocumentListener listener) {
        mListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.page_documents, container, false);
        initViews(view);
        return view;
    }

    public void reRequestData(long chapterId){
        mChapterId = chapterId;
        new InitDataTask().execute();
    }

    private void initViews(View view){
        mAddDocumentView = view.findViewById(R.id.add_document_view);
        mGridView = view.findViewById(R.id.grid_view);
        mAdapter = new DocumentAdapter(getActivity());
        mGridView.setAdapter(mAdapter);
    }


    private class InitDataTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            List<Document> documentList = ScopeServer.getInstance().QureyDocuments(123);
            if (documentList == null)
                return null;

            mAdapter.setData(documentList);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mAdapter.notifyDataSetChanged();

            if (mAdapter.getCount() == 0) {
                try {
                    Toast.makeText(getActivity().getApplicationContext(), "没有找到相关的微课资源!", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                }
            }
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.add_document_view:
                break;
        }
    }

    public interface DocumentListener{
        void onDocumentClicked(int documentId);
    }
}
