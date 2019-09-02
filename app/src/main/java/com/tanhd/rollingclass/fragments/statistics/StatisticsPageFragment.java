package com.tanhd.rollingclass.fragments.statistics;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.activity.DocumentEditActivity;
import com.tanhd.rollingclass.db.KeyConstants;
import com.tanhd.rollingclass.fragments.CountClassFragment;
import com.tanhd.rollingclass.fragments.FrameDialog;
import com.tanhd.rollingclass.fragments.TeacherMicroCourseSelectorFragment;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.KnowledgeModel;
import com.tanhd.rollingclass.server.data.MicroCourseData;
import com.tanhd.rollingclass.server.data.ResourceModel;
import com.tanhd.rollingclass.server.data.UserData;

import java.util.List;

public class StatisticsPageFragment extends Fragment implements View.OnClickListener {

    private KnowledgeModel mKnowledgeModel;
    private View mWeikeView;
    private View mXitiView;

    public static StatisticsPageFragment newInstance() {
        Bundle args = new Bundle();
        StatisticsPageFragment page = new StatisticsPageFragment();
        page.setArguments(args);
        return page;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.page_show_statistics, container, false);
        initParams();
        initViews(view);
        return view;
    }

    private void initParams(){
        Bundle args = getArguments();
        if(args!=null) {
            mKnowledgeModel = (KnowledgeModel) args.getSerializable(DocumentEditActivity.PARAM_TEACHING_MATERIAL_DATA);
        }
    }

    private void initViews(View view) {
        mWeikeView = view.findViewById(R.id.weike_view);
        mXitiView = view.findViewById(R.id.xiti_view);

        mWeikeView.setOnClickListener(this);
        mXitiView.setOnClickListener(this);
    }

    public void resetData(KnowledgeModel model) {
        Bundle args = new Bundle();
        args.putSerializable(DocumentEditActivity.PARAM_TEACHING_MATERIAL_DATA, model);
        setArguments(args);
        initParams();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.weike_view:
                FrameDialog.show(getChildFragmentManager(), TeacherMicroCourseSelectorFragment.newInstance(
                        new TeacherMicroCourseSelectorFragment.SelectorMicroCourseListener() {
                            @Override
                            public void onMicroCourseSelected(MicroCourseData microCourseData) {
                                StatisticsActivity.startMe(getActivity(), StatisticsActivity.PAGE_ID_MICRO_COURSE);
                            }
                        }));
                break;
            case R.id.xiti_view:
                StatisticsActivity.startMe(getActivity(), StatisticsActivity.PAGE_ID_QUESTION);
                break;
        }
    }

    private class InitDataTask extends AsyncTask<Void, Void, List<ResourceModel>> {

        private int level;

        public InitDataTask(int level) {
            this.level = level;
        }
        @Override
        protected List<ResourceModel> doInBackground(Void... voids) {
            UserData userData = ExternalParam.getInstance().getUserData();
            if (userData.isTeacher()) {
                if(mKnowledgeModel!=null){
                    return ScopeServer.getInstance().QureyResourceByTeacherID(
                            mKnowledgeModel.teacher_id, mKnowledgeModel.teaching_material_id,
                            level, KeyConstants.ResourceType.VIDEO_TYPE, 1, 40);
                }
            } else {
                return null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<ResourceModel> documentList) {

        }
    }
}
