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
import com.tanhd.rollingclass.activity.DocumentEditActivity;
import com.tanhd.rollingclass.db.Document;
import com.tanhd.rollingclass.server.data.KnowledgeModel;
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

    public static DocumentsPageFragment newInstance(DocumentsPageFragment.DocumentListener listener) {
        Bundle args = new Bundle();
        DocumentsPageFragment page = new DocumentsPageFragment();
        page.setArguments(args);
        page.setListener(listener);
        return page;
    }

    public void resetData(KnowledgeModel model) {
        Bundle args = new Bundle();
        args.putSerializable(DocumentEditActivity.PARAM_KNOWLEDGE_DATA, model);
        setArguments(args);

    }

    private void setListener(DocumentsPageFragment.DocumentListener listener) {
        mListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.page_documents, container, false);
        initViews(view);
        new InitDataTask().execute();
        return view;
    }

    private void initViews(View view) {
        mAddDocumentView = view.findViewById(R.id.add_document_view);
        mGridView = view.findViewById(R.id.grid_view);

        mAdapter = new DocumentAdapter(getActivity());
        mGridView.setAdapter(mAdapter);

        mAddDocumentView.setOnClickListener(this);
    }


    private class InitDataTask extends AsyncTask<Void, Void, List<Document>> {

        @Override
        protected List<Document> doInBackground(Void... voids) {
            List<Document> documentList = ScopeServer.getInstance().QureyDocuments(123);
            return documentList;
        }

        @Override
        protected void onPostExecute(List<Document> documentList) {
            mAdapter.setData(documentList);
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
        switch (v.getId()) {
            case R.id.add_document_view:
                KnowledgeModel model = (KnowledgeModel) getArguments().getSerializable(DocumentEditActivity.PARAM_KNOWLEDGE_DATA);
                DocumentEditActivity.startMe(getActivity(), DocumentEditActivity.PAGE_ID_ADD_DOCUMENTS, model);
                break;
        }
    }

    public interface DocumentListener {
        void onDocumentClicked(int documentId);
    }
}
