package com.tanhd.rollingclass.fragments.pages;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.Toast;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.activity.DocumentEditActivity;
import com.tanhd.rollingclass.db.Document;
import com.tanhd.rollingclass.db.KeyConstants;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.KnowledgeDetailMessage;
import com.tanhd.rollingclass.server.data.KnowledgeModel;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.SyncSampleToClassRequest;
import com.tanhd.rollingclass.server.data.TeacherData;
import com.tanhd.rollingclass.server.data.UserData;
import com.tanhd.rollingclass.views.DocumentAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * 备课页面
 */
public class DocumentsPageFragment extends Fragment implements View.OnClickListener, DocumentAdapter.Callback {

    private View mAddDocumentView;
    private DocumentListener mListener;
    private GridView mGridView;
    private DocumentAdapter mAdapter;
    private KnowledgeModel mKnowledgeModel;

    public static DocumentsPageFragment newInstance(DocumentsPageFragment.DocumentListener listener) {
        Bundle args = new Bundle();
        DocumentsPageFragment page = new DocumentsPageFragment();
        page.setArguments(args);
        page.setListener(listener);
        return page;
    }

    public void resetData(KnowledgeModel model) {
        Bundle args = new Bundle();
        args.putSerializable(DocumentEditActivity.PARAM_TEACHING_MATERIAL_DATA, model);
        setArguments(args);
        initParams();
        if(mKnowledgeModel!=null){
            mAddDocumentView.setEnabled(true);
        }
        new InitDataTask().execute();
    }

    private void setListener(DocumentsPageFragment.DocumentListener listener) {
        mListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.page_documents, container, false);
        initParams();
        initViews(view);
        refreshData();


        return view;
    }

    private void initParams() {
        Bundle args = getArguments();
        mKnowledgeModel = (KnowledgeModel) args.getSerializable(DocumentEditActivity.PARAM_TEACHING_MATERIAL_DATA);
    }

    private void initViews(View view) {
        mAddDocumentView = view.findViewById(R.id.add_document_view);
        mGridView = view.findViewById(R.id.grid_view);

        mAdapter = new DocumentAdapter(getActivity(), this);
        mGridView.setAdapter(mAdapter);
        mAddDocumentView.setOnClickListener(this);
        if(mKnowledgeModel==null){
            mAddDocumentView.setEnabled(false);
        }
    }

    @Override
    public void refreshData() {
        new InitDataTask().execute();
    }

    private class InitDataTask extends AsyncTask<Void, Void, List<KnowledgeDetailMessage>> {

        @Override
        protected List<KnowledgeDetailMessage> doInBackground(Void... voids) {
            UserData userData = ExternalParam.getInstance().getUserData();
            if (userData.isTeacher()) {
                if (mKnowledgeModel != null) {
                    return ScopeServer.getInstance().QureyKnowledgeByChapterAndTeacherID(mKnowledgeModel.teacher_id, mKnowledgeModel.teaching_material_id);
                }
            } else {
                return null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<KnowledgeDetailMessage> documentList) {
            if (documentList != null && documentList.size() > 0) {
                mAdapter.setData(documentList);
            } else {
                mAdapter.clearData();
            }
        }
    }


//    private void showLessonSampleDialog(final int syncTo) {
//        if (mDataList == null) {
//            return;
//        }
//        String[] sampleNameItems = new String[mDataList.size()];
//        final String[] sampleIdItems = new String[mDataList.size()];
//        boolean[] checkedItems = new boolean[mDataList.size()];
//        final List<String> checkedIdList = new ArrayList<>();
//        for (int i = 0; i < mDataList.size(); i++) {
//            sampleNameItems[i] = mDataList.get(i).lesson_sample_name;
//            sampleIdItems[i] = mDataList.get(i).lesson_sample_id;
//            checkedItems[i] = true;
//        }
//        new AlertDialog.Builder(getActivity())
//                .setTitle(R.string.publish_warning)
//                .setMultiChoiceItems(sampleNameItems, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
//                        if (isChecked) {
//                            sampleIdItems[which] = mDataList.get(which).lesson_sample_id;
//                        } else {
//                            sampleIdItems[which] = "";
//                        }
//                    }
//                })
//                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//
//                    }
//                })
//                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        for (int i = 0; i < sampleIdItems.length; i++) {
//                            if (!TextUtils.isEmpty(sampleIdItems[i])) {
//                                checkedIdList.add(sampleIdItems[i]);
//                            }
//                        }
//                        SyncSampleToClassRequest request = new SyncSampleToClassRequest();
//                        switch (syncTo) {
//                            case KeyConstants.KnowledgeStatus.AFTER_CLASS:
//                                request.class_after_task = checkedIdList;
//                                break;
//                            case KeyConstants.KnowledgeStatus.AT_CLASS:
//                                request.class_process_task = checkedIdList;
//                                break;
//                            case KeyConstants.KnowledgeStatus.FRE_CLASS:
//                                request.class_before_task = checkedIdList;
//                                break;
//                        }
//                        request.cur_status = mStatus;
//                        new RequestSyncTask(request).execute();
//                    }
//                }).show();
//    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_document_view:
                KnowledgeModel model = (KnowledgeModel) getArguments().getSerializable(DocumentEditActivity.PARAM_TEACHING_MATERIAL_DATA);
                DocumentEditActivity.startMe(getActivity(), DocumentEditActivity.PAGE_ID_ADD_DOCUMENTS, model);
                break;
        }
    }

    public interface DocumentListener {
        void onDocumentClicked(int documentId);
    }
}
