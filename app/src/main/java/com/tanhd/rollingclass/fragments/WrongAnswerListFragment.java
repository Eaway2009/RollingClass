package com.tanhd.rollingclass.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.activity.DocumentEditActivity;
import com.tanhd.rollingclass.base.BaseFragment;
import com.tanhd.rollingclass.fragments.resource.AnswerDisplayFragment;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.AnswerData;
import com.tanhd.rollingclass.server.data.AnswerModel;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.KnowledgeDetailMessage;
import com.tanhd.rollingclass.server.data.KnowledgeModel;
import com.tanhd.rollingclass.server.data.StudentData;
import com.tanhd.rollingclass.server.data.UserData;
import com.tanhd.rollingclass.server.data.WrongAnswerList;
import com.tanhd.rollingclass.utils.annotate.InjectView;
import com.tanhd.rollingclass.views.OnItemClickListener;
import com.tanhd.rollingclass.views.PopFliterRes;

import java.util.ArrayList;
import java.util.List;

/**
 * 错题本
 */
public class WrongAnswerListFragment extends BaseFragment implements View.OnClickListener {

    private static final int MODULE_ID_QUESTION_LIST = 0;
    private static final int MODULE_ID_PHOTO_LIST = 1;
    private static final int ROOT_LAYOUT_ID = R.id.content_layout;
    private KnowledgeModel mKnowledgeModel;
    private KnowledgeDetailMessage mKnowledgeDetailMessage;

    private int mPageSize = 100;
    private boolean mIsRequesting = false;
    private Callback mCallback;

    private AnswerDisplayFragment mQuestionResourceFragment;
    private WrongAnswerBookPhotoFragment mWrongAnswerBookPhotoFragment;
    private TextView tv_spinner;
    private View mBackButton;
    private View mQuestionListView;
    private View mPhotoListView;
    private int mCurrentShowModuleId = -1;
    private List<KnowledgeDetailMessage> kldList;
    private PopFliterRes popFliterRes;

    public static WrongAnswerListFragment newInstance(KnowledgeModel knowledgeModel, Callback callback) {
        Bundle args = new Bundle();
        args.putSerializable(DocumentEditActivity.PARAM_TEACHING_MATERIAL_DATA, knowledgeModel);
        WrongAnswerListFragment page = new WrongAnswerListFragment();
        page.setArguments(args);
        page.setCallback(callback);
        return page;
    }

    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.page_wrong_answer_list, container, false);
        initParams();
        initViews(view);
        initFragment();
        initData();
        return view;
    }

    private void initParams() {
        Bundle args = getArguments();
        mKnowledgeModel = (KnowledgeModel) args.getSerializable(DocumentEditActivity.PARAM_TEACHING_MATERIAL_DATA);
    }

    private void initViews(View view) {
        mBackButton = view.findViewById(R.id.back_button);
        mQuestionListView = view.findViewById(R.id.question_list);
        mPhotoListView = view.findViewById(R.id.photo_list);
        tv_spinner = view.findViewById(R.id.tv_spinner);

        if (popFliterRes == null){
            popFliterRes = new PopFliterRes(getActivity());
        }
        popFliterRes.setRootWidth((int) getResources().getDimension(R.dimen.dp_150));
        popFliterRes.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                tv_spinner.setText(popFliterRes.getDatas().get(position));
                mKnowledgeDetailMessage = kldList.get(position);
                new InitQuestionDataTask().execute();
            }
        });

        tv_spinner.setOnClickListener(this);
        mBackButton.setOnClickListener(this);
        mPhotoListView.setOnClickListener(this);
        mQuestionListView.setOnClickListener(this);
    }

    private void initData(){
        resetData(mKnowledgeModel);
    }

    public void resetData(KnowledgeModel module){
        mIsRequesting = true;
        mKnowledgeModel = module;
        if(mQuestionResourceFragment!=null){
            mQuestionResourceFragment.clearListData();
        }
        if(mWrongAnswerBookPhotoFragment!=null){
            mWrongAnswerBookPhotoFragment.clearListData();
        }
        new InitDataTask().execute();
    }

    private void initFragment(){
        showModulePage(MODULE_ID_QUESTION_LIST);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.photo_list:
                showModulePage(MODULE_ID_QUESTION_LIST);
                break;
            case R.id.question_list:
                showModulePage(MODULE_ID_PHOTO_LIST);
                break;
            case R.id.back_button:
                if(mCallback!=null){
                    mCallback.onBack();
                }
                break;
            case R.id.tv_spinner: //筛选
                popFliterRes.showMask(false).showAsDropDown(v);
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
        if (moduleId == MODULE_ID_QUESTION_LIST) { //习题
            if (mQuestionResourceFragment == null) {
                mQuestionResourceFragment = AnswerDisplayFragment.newInstance(1);
                transaction.add(ROOT_LAYOUT_ID, mQuestionResourceFragment);
            }
            moduleFragment = mQuestionResourceFragment;
            if (mWrongAnswerBookPhotoFragment != null) {
                transaction.hide(mWrongAnswerBookPhotoFragment);
            }
        } else if (moduleId == MODULE_ID_PHOTO_LIST) {  //图片
            if (mWrongAnswerBookPhotoFragment == null) {
                mWrongAnswerBookPhotoFragment = WrongAnswerBookPhotoFragment.newInstance(mKnowledgeModel, null);
                transaction.add(ROOT_LAYOUT_ID, mWrongAnswerBookPhotoFragment);
            }
            moduleFragment = mWrongAnswerBookPhotoFragment;
            if (mQuestionResourceFragment != null) {
                transaction.hide(mQuestionResourceFragment);
            }
        }
        transaction.show(moduleFragment);
        transaction.commitAllowingStateLoss();

        mCurrentShowModuleId = moduleId;
    }

    private class InitDataTask extends AsyncTask<Void, Void, List<KnowledgeDetailMessage>> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected List<KnowledgeDetailMessage> doInBackground(Void... voids) {
            UserData userData = ExternalParam.getInstance().getUserData();
            if (mKnowledgeModel != null) {
                StudentData studentData = (StudentData) userData.getUserData();
                return ScopeServer.getInstance().QureyKnowledgeByClassID(studentData.ClassID, mKnowledgeModel.teaching_material_id);
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<KnowledgeDetailMessage> documentList) {
            kldList = documentList;
            if (documentList != null && documentList.size() > 0) {
                List<String> listStr = new ArrayList<>();
                for (int i = 0;i<documentList.size();i++){
                    listStr.add(documentList.get(i).knowledge_point_name);
                }
                popFliterRes.setDatas(listStr);

                //默认选中第一个
                tv_spinner.setText(listStr.get(0));
                mKnowledgeDetailMessage = kldList.get(0);
                new InitQuestionDataTask().execute();
            } else {
                tv_spinner.setText(R.string.no_knowledge);
            }
        }
    }

    private class InitQuestionDataTask extends AsyncTask<Void, Void, List<AnswerModel>> {

        @Override
        protected void onPreExecute() {
            mIsRequesting = true;
        }

        @Override
        protected List<AnswerModel> doInBackground(Void... voids) {
            UserData userData = ExternalParam.getInstance().getUserData();
            if (!userData.isTeacher()) {
                StudentData studentData = (StudentData) userData.getUserData();
                if (mKnowledgeModel != null) {
                    WrongAnswerList wrongAnswerList = ScopeServer.getInstance().QureyAnswerv2ByStudentIDAndCourseID(
                            studentData.StudentID, mKnowledgeDetailMessage.knowledge_id);
                    return wrongAnswerList.questions;
                }
            } else {
                return null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<AnswerModel> documentList) {
            if (mQuestionResourceFragment == null) {
                mIsRequesting = false;
                return;
            }
            if (documentList != null && documentList.size() > 0) {
                mQuestionResourceFragment.setListData(documentList);
            } else {
                mQuestionResourceFragment.clearListData();
            }
            mIsRequesting = false;
        }
    }

    public interface Callback{
        void onBack();
    }

    private class DocumentSpinnerAdapter extends ArrayAdapter {
        private Context ctx;
        private List<KnowledgeDetailMessage> mDataList = new ArrayList<>();

        public DocumentSpinnerAdapter(@NonNull Context context, int resource) {
            super(context, resource);
            ctx = context;
        }

        public void setDataList(List<KnowledgeDetailMessage> dataList) {
            mDataList.clear();
            mDataList.addAll(dataList);
        }

        private void clearData(){
            mDataList.clear();
        }

        @Override
        public int getCount() {
            return mDataList.size();
        }

        @Override
        public KnowledgeDetailMessage getItem(int position) {
            return mDataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(ctx, R.layout.spinner_check_textview, null);
            }
            KnowledgeDetailMessage message = mDataList.get(position);
            TextView textView = convertView.findViewById(R.id.text1);
            textView.setText(message.knowledge_point_name);
            return convertView;
        }

        @Override
        public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(ctx, R.layout.spinner_down_layout, null);
            }
            KnowledgeDetailMessage message = mDataList.get(position);
            TextView textView = convertView.findViewById(R.id.text1);
            textView.setText(message.knowledge_point_name);
            return convertView;
        }
    }
}
