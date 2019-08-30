package com.tanhd.rollingclass.fragments.resource;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.activity.DocumentEditActivity;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.KnowledgeDetailMessage;
import com.tanhd.rollingclass.server.data.KnowledgeModel;
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
    private GridView mGridView;
    private ResourceAdapter mAdapter;
    private KnowledgeModel mKnowledgeModel;

    public static ResourcesPageFragment newInstance() {
        Bundle args = new Bundle();
        ResourcesPageFragment page = new ResourcesPageFragment();
        page.setArguments(args);
        return page;
    }

    public void resetData(KnowledgeModel model) {
        Bundle args = new Bundle();
        args.putSerializable(DocumentEditActivity.PARAM_TEACHING_MATERIAL_DATA, model);
        setArguments(args);
        initParams();
        new InitDataTask().execute();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.page_show_resource, container, false);
        initParams();
        initViews(view);
        new InitDataTask().execute();


        return view;
    }

    private void initParams(){
        Bundle args = getArguments();
        if(args!=null) {
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
        mGridView = view.findViewById(R.id.grid_view);

        mAdapter = new ResourceAdapter(getActivity());
        mGridView.setAdapter(mAdapter);
        mPptTitleView.setOnClickListener(this);
        mMicroCourseTitleView.setOnClickListener(this);
        mWordTitleView.setOnClickListener(this);
        mQuestionTitleView.setOnClickListener(this);
        mImageTitleView.setOnClickListener(this);
        mUploadFileTitleView.setOnClickListener(this);
    }


    private class InitDataTask extends AsyncTask<Void, Void, List<KnowledgeDetailMessage>> {

        @Override
        protected List<KnowledgeDetailMessage> doInBackground(Void... voids) {
            UserData userData = ExternalParam.getInstance().getUserData();
            if (userData.isTeacher()) {
                if(mKnowledgeModel!=null){
                    return ScopeServer.getInstance().QureyKnowledgeByChapterAndTeacherID(mKnowledgeModel.teacher_id, mKnowledgeModel.teaching_material_id);
                }
            }else {
                return null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<KnowledgeDetailMessage> documentList) {
            if(documentList!=null&&documentList.size()>0) {
                mAdapter.setData(documentList);
            }else{
                mAdapter.clearData();
            }
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ppt_resource_view:
                break;
            case R.id.word_resource_view:
                break;
            case R.id.image_resource_view:
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
