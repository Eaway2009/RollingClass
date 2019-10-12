package com.tanhd.rollingclass.views;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tanhd.rollingclass.MainApp;
import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.activity.DocumentEditActivity;
import com.tanhd.rollingclass.activity.LearnCasesActivity;
import com.tanhd.rollingclass.db.KeyConstants;
import com.tanhd.rollingclass.fragments.ClassRecordsFragment;
import com.tanhd.rollingclass.fragments.FrameDialog;
import com.tanhd.rollingclass.server.RequestCallback;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.KnowledgeDetailMessage;
import com.tanhd.rollingclass.server.data.KnowledgeModel;
import com.tanhd.rollingclass.server.data.RequestShareKnowledge;
import com.tanhd.rollingclass.server.data.SchoolData;
import com.tanhd.rollingclass.server.data.TeacherData;
import com.tanhd.rollingclass.server.data.UserData;
import com.tanhd.rollingclass.utils.StringUtils;
import com.tanhd.rollingclass.utils.ToastUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 学案适配器
 */
public class DocumentAdapter extends BaseAdapter implements RequestCallback {

    private boolean mIsTeacher;
    private Fragment mContext;
    private List<KnowledgeDetailMessage> mDataList = new ArrayList<>();

    private Callback mListener;

    public DocumentAdapter(Fragment context, boolean isTeacher, Callback callback) {
        mContext = context;
        mListener = callback;
        mIsTeacher = isTeacher;
    }

    public void setData(List<KnowledgeDetailMessage> datas) {
        mDataList = datas;
        notifyDataSetChanged();
    }

    public void clearData() {
        mDataList.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mDataList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout view = (LinearLayout) convertView;
        if (view == null) {
            view = (LinearLayout) mContext.getLayoutInflater().inflate(R.layout.adapter_document, parent, false);
        }

        final KnowledgeDetailMessage data = mDataList.get(position);

        TextView statusView = view.findViewById(R.id.document_status_tv);
        TextView titleView = view.findViewById(R.id.document_title_tv);
        TextView editTimeView = view.findViewById(R.id.edit_time_tv);
        ImageView moreView = view.findViewById(R.id.document_more_ib);
        final LinearLayout moreBottomView = view.findViewById(R.id.more_bottom);
        ImageView moreCopyView = view.findViewById(R.id.more_copy_iv);
        ImageView moreShareView = view.findViewById(R.id.more_share_iv);
        ImageView moreEditView = view.findViewById(R.id.more_edit_iv);
        ImageView moreDeleteView = view.findViewById(R.id.more_delete_iv);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.layout_content:
                        if (mIsTeacher) {
                            teacherOnclickItem(data);
                        } else {
                            LearnCasesActivity.startMe(mContext, data.knowledge_id, data.knowledge_point_name, data.teaching_material_id, KeyConstants.ClassPageType.STUDENT_LEARNING_PAGE);
                        }
                        break;
                    case R.id.more_bottom:
                        moreBottomView.setVisibility(View.INVISIBLE);
                        break;
                    case R.id.document_more_ib: //更多
                        moreBottomView.setVisibility(View.VISIBLE);
                        break;
                    case R.id.more_copy_iv:
                        moreBottomView.setVisibility(View.INVISIBLE);
                        ScopeServer.getInstance().DumpKnowledge(data.knowledge_id, DocumentAdapter.this);
                        break;
                    case R.id.more_share_iv: //分享
                        moreBottomView.setVisibility(View.INVISIBLE);
                        new TeacherListTask(data.knowledge_id).execute();
                        break;
                    case R.id.more_edit_iv:
                        moreBottomView.setVisibility(View.INVISIBLE);
                        KnowledgeModel knowledgeModel = new KnowledgeModel(data.school_id, data.teacher_id, data.chapter_id, data.chapter_name, data.section_id, data.section_name, data.subject_code, data.subject_name, data.teaching_material_id, null);
                        DocumentEditActivity.startMe(mContext, DocumentEditActivity.PAGE_ID_EDIT_DOCUMENTS, knowledgeModel, data);
                        break;
                    case R.id.more_delete_iv: //删除
                        moreBottomView.setVisibility(View.INVISIBLE);
                        showDeleteDialog(data);
                        break;
                    case R.id.document_status_tv:
                        classStatusClick(data);
                        break;
                }
            }
        };
        view.setOnClickListener(onClickListener);
        moreView.setOnClickListener(onClickListener);
        moreBottomView.setOnClickListener(onClickListener);
        moreCopyView.setOnClickListener(onClickListener);
        moreShareView.setOnClickListener(onClickListener);
        moreEditView.setOnClickListener(onClickListener);
        moreDeleteView.setOnClickListener(onClickListener);
        statusView.setOnClickListener(onClickListener);

        if (mIsTeacher) {
            moreView.setVisibility(View.VISIBLE);
        } else {
            moreView.setVisibility(View.GONE);
        }

        StringBuffer publishStatus = new StringBuffer();
        if (!mIsTeacher) {
            publishStatus.append(mContext.getResources().getString(R.string.learning_record));
        } else if ((data.class_before == 1 && data.class_process == 1 && data.class_after == 1)) {
            publishStatus.append(mContext.getResources().getString(R.string.class_record));
        } else {
            if (data.class_before == 0) {
                publishStatus.append(mContext.getResources().getString(R.string.fre_class));
            }
            if (data.class_process == 0) {
                if (publishStatus.length() > 0) {
                    publishStatus.append(mContext.getResources().getString(R.string.comma));
                }
                publishStatus.append(mContext.getResources().getString(R.string.at_class));
            }
            if (data.class_after == 0) {
                if (publishStatus.length() > 0) {
                    publishStatus.append(mContext.getResources().getString(R.string.comma));
                }
                publishStatus.append(mContext.getResources().getString(R.string.after_class));
            }
            if (publishStatus.toString().startsWith(mContext.getResources().getString(R.string.comma))) {
                publishStatus.substring(1, publishStatus.length() - 1);
            }
            publishStatus.append(mContext.getResources().getString(R.string.to_publish));
        }
        statusView.setText(publishStatus);
        if (!mIsTeacher || (data.class_before == 1 && data.class_process == 1 && data.class_after == 1)) {
            statusView.setBackgroundResource(R.drawable.document_status);
        } else {
            statusView.setBackgroundResource(R.drawable.document_status_disssable);
        }
        moreCopyView.setVisibility(data.class_before == 1 && data.class_process == 1 && data.class_after == 1 ? View.VISIBLE : View.GONE);
        moreEditView.setVisibility(data.class_before == 0 || data.class_process == 0 || data.class_after == 0 ? View.VISIBLE : View.GONE);
        titleView.setText(data.knowledge_point_name);
        editTimeView.setText(StringUtils.secondToDate(data.create_time));
        return view;
    }

    private void teacherOnclickItem(KnowledgeDetailMessage data) {
        if (data.class_process == 1) {
            LearnCasesActivity.startMe(mContext, data.knowledge_id, data.knowledge_point_name, data.teaching_material_id, KeyConstants.ClassPageType.TEACHER_CLASS_PAGE);
        } else {
            ToastUtil.show(R.string.class_on_knowledge_warning);
        }
    }

    private void classStatusClick(KnowledgeDetailMessage data) {
        if (!mIsTeacher || (data.class_before == 1 && data.class_process == 1 && data.class_after == 1)) {
            if (data.records != null && data.records.size() > 0) { //上课记录or学习记录
                FrameDialog.show(mContext.getFragmentManager(), ClassRecordsFragment.getInstance(data), 0.54d);
            } else {
                if (mIsTeacher) {
                    Toast.makeText(mContext.getContext(), R.string.no_class_records_warning, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mContext.getContext(), R.string.no_learning_records_warning, Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            KnowledgeModel knowledgeModel = new KnowledgeModel(data.school_id, data.teacher_id, data.chapter_id, data.chapter_name, data.section_id, data.section_name, data.subject_code, data.subject_name, data.teaching_material_id, null);
            DocumentEditActivity.startMe(mContext, DocumentEditActivity.PAGE_ID_EDIT_DOCUMENTS, knowledgeModel, data);
        }
    }

    private void showDeleteDialog(final KnowledgeDetailMessage data) {
        String content = String.format(mContext.getResources().getString(R.string.delete_knowledge_warning), data.knowledge_point_name);
        new YesNoDialog(content, "", "", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScopeServer.getInstance().DeleteKnowledge(data.teaching_material_id, data.knowledge_id, DocumentAdapter.this);
            }
        }, null).show(mContext.getChildFragmentManager(), "YesNoDialog");
    }

    private class TeacherListTask extends AsyncTask<Void, Void, List<TeacherData>> {
        String knowledge_id;

        TeacherListTask(String knowledge_id) {
            this.knowledge_id = knowledge_id;
        }

        @Override
        protected List<TeacherData> doInBackground(Void... voids) {
            SchoolData schoolData = ExternalParam.getInstance().getSchoolData();
            if (schoolData != null) {
                return ScopeServer.getInstance().QureyTeacherBySchoolID(schoolData.SchoolID);
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<TeacherData> teacherDataList) {
            super.onPostExecute(teacherDataList);
            if (teacherDataList != null && teacherDataList.size() > 0) {

                UserData userData = ExternalParam.getInstance().getUserData();
                TeacherData teacherData = null;
                if (userData.isTeacher()) {
                    teacherData = (TeacherData) userData.getUserData();
                }
                if (teacherData != null) {
                    List<TeacherData> newTeacherList = new ArrayList<>();
                    for (TeacherData teacherDataItem : teacherDataList) {
                        if (!teacherData.TeacherID.equals(teacherDataItem.TeacherID)) {
                            newTeacherList.add(teacherDataItem);
                        }
                    }
                    showTeacherListDialog(knowledge_id, newTeacherList);
                } else {
                    showTeacherListDialog(knowledge_id, teacherDataList);
                }
            } else {
                ToastUtil.show(R.string.check_teacher_list_fail);
            }
        }
    }

    private void showTeacherListDialog(final String knowledge_id, final List<TeacherData> teacherDataList) {
        final RequestCallback shareCallback = new RequestCallback() {
            @Override
            public void onProgress(boolean b) {

            }

            @Override
            public void onResponse(String body) {
                ToastUtil.show(R.string.toast_share_success);
            }

            @Override
            public void onError(String code, String message) {
                ToastUtil.show(message);
            }
        };

        if (teacherDataList == null) {
            return;
        }
        String[] teacherNameItems = new String[teacherDataList.size()];
        final String[] teacherIdItems = new String[teacherDataList.size()];
        boolean[] checkedItems = new boolean[teacherDataList.size()];
        final List<String> checkedIdList = new ArrayList<>();

        final ShareDocumentDialog shareDocumentDialog = new ShareDocumentDialog(teacherDataList);
        shareDocumentDialog.show(mContext.getChildFragmentManager(), "shareDocumentDialog");
        shareDocumentDialog.setOkListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<TeacherData> selectList = shareDocumentDialog.getSelectData();
                if (selectList.isEmpty()) {
                    ToastUtil.show(MainApp.getInstance().getString(R.string.toast_min_select_one));
                    return;
                }

                for (int i = 0; i < selectList.size(); i++) {
                    checkedIdList.add(selectList.get(i).TeacherID);
                }
                RequestShareKnowledge request = new RequestShareKnowledge();
                request.knowledge_id = knowledge_id;
                request.teachers = checkedIdList;
                if (checkedIdList.size() > 0) {
                    ScopeServer.getInstance().ShareKnowledgeToTeachers(request, shareCallback);
                }

            }
        });

    }

    @Override
    public void onProgress(boolean b) {

    }

    @Override
    public void onResponse(String body) {
        ToastUtil.show(R.string.operate_success);
        mListener.refreshData();
    }

    @Override
    public void onError(String code, String message) {
        Toast.makeText(mContext.getContext(), message, Toast.LENGTH_SHORT).show();
    }

    public interface Callback {
        void refreshData();
    }
}
