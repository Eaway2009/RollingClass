package com.tanhd.rollingclass.views;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.activity.DocumentEditActivity;
import com.tanhd.rollingclass.db.KeyConstants;
import com.tanhd.rollingclass.server.RequestCallback;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.KnowledgeDetailMessage;
import com.tanhd.rollingclass.activity.LearnCasesActivity;
import com.tanhd.rollingclass.server.data.KnowledgeModel;
import com.tanhd.rollingclass.server.data.UserData;
import com.tanhd.rollingclass.utils.StringUtils;
import com.tanhd.rollingclass.server.data.RequestShareKnowledge;
import com.tanhd.rollingclass.server.data.SchoolData;
import com.tanhd.rollingclass.server.data.TeacherData;

import java.util.ArrayList;
import java.util.List;

public class DocumentAdapter extends BaseAdapter implements RequestCallback {

    private boolean mIsTeacher;
    private Activity mContext;
    private List<KnowledgeDetailMessage> mDataList = new ArrayList<>();

    private Callback mListener;

    public DocumentAdapter(Activity context, boolean isTeacher, Callback callback) {
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
                            LearnCasesActivity.startMe(mContext, data.knowledge_id, data.knowledge_point_name, KeyConstants.ClassPageType.TEACHER_CLASS_PAGE);
                        } else {
                            LearnCasesActivity.startMe(mContext, data.knowledge_id, data.knowledge_point_name, KeyConstants.ClassPageType.STUDENT_LEARNING_PAGE);
                        }
                        break;
                    case R.id.more_bottom:
                        moreBottomView.setVisibility(View.GONE);
                        break;
                    case R.id.document_more_ib:
                        moreBottomView.setVisibility(View.VISIBLE);
                        break;
                    case R.id.more_copy_iv:
                        moreBottomView.setVisibility(View.GONE);
                        ScopeServer.getInstance().DumpKnowledge(data.knowledge_id, DocumentAdapter.this);
                        break;
                    case R.id.more_share_iv:
                        moreBottomView.setVisibility(View.GONE);
                        new TeacherListTask(data.knowledge_id).execute();
                        break;
                    case R.id.more_edit_iv:
                        moreBottomView.setVisibility(View.GONE);
                        KnowledgeModel knowledgeModel = new KnowledgeModel(data.school_id, data.teacher_id, data.chapter_id, data.chapter_name, data.section_id, data.section_name, data.subject_code, data.subject_name, data.teaching_material_id, null);
                        DocumentEditActivity.startMe(mContext, DocumentEditActivity.PAGE_ID_EDIT_DOCUMENTS, knowledgeModel, data);
                        break;
                    case R.id.more_delete_iv:
                        moreBottomView.setVisibility(View.GONE);
                        showDeleteDialog(data);
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

        StringBuffer publishStatus = new StringBuffer();
        if (data.class_before == 0 && data.class_process == 0 && data.class_after == 0) {
            publishStatus.append(mContext.getResources().getString(R.string.not_publish));
        } else if (data.class_before == 1 && data.class_process == 1 && data.class_after == 1) {
            publishStatus.append(mContext.getResources().getString(R.string.class_record));

        } else {
            if (data.class_before != 0) {
                publishStatus.append(mContext.getResources().getString(R.string.fre_class));
            }
            if (publishStatus.length() != 0) {
                publishStatus.append(mContext.getResources().getString(R.string.comma));
            }
            if (data.class_process != 0) {
                publishStatus.append(mContext.getResources().getString(R.string.at_class));
            }
            if (publishStatus.length() != 0) {
                publishStatus.append(mContext.getResources().getString(R.string.comma));
            }
            if (data.class_after != 0) {
                publishStatus.append(mContext.getResources().getString(R.string.after_class));
            }
            if (publishStatus.toString().endsWith(mContext.getResources().getString(R.string.comma))) {
                publishStatus.deleteCharAt(publishStatus.length() - 1);
            }
            publishStatus.append(mContext.getResources().getString(R.string.to_publish));
        }
        statusView.setText(publishStatus);
        statusView.setEnabled(data.class_before == 1 && data.class_process == 1 && data.class_after == 1);
        moreCopyView.setVisibility(data.class_before == 1 && data.class_process == 1 && data.class_after == 1 ? View.VISIBLE : View.GONE);
        moreEditView.setVisibility(data.class_before == 0 || data.class_process == 0 || data.class_after == 0 ? View.VISIBLE : View.GONE);
        titleView.setText(data.knowledge_point_name);
        editTimeView.setText(StringUtils.secondToDate(data.create_time));
        return view;
    }

    private void showDeleteDialog(final KnowledgeDetailMessage data) {
        new AlertDialog.Builder(mContext)
                .setMessage(R.string.delete_knowledge_warning)
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ScopeServer.getInstance().DeleteKnowledge(data.teaching_material_id, data.knowledge_id, DocumentAdapter.this);
                    }
                }).show();
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
                Toast.makeText(mContext, R.string.check_teacher_list_fail, Toast.LENGTH_SHORT).show();
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
                Toast.makeText(mContext, "分享成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String code, String message) {
                Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
            }
        };

        if (teacherDataList == null) {
            return;
        }
        String[] teacherNameItems = new String[teacherDataList.size()];
        final String[] teacherIdItems = new String[teacherDataList.size()];
        boolean[] checkedItems = new boolean[teacherDataList.size()];
        final List<String> checkedIdList = new ArrayList<>();

        for (int i = 0; i < teacherDataList.size(); i++) {
            teacherNameItems[i] = teacherDataList.get(i).Username;
            teacherIdItems[i] = teacherDataList.get(i).TeacherID;
            checkedItems[i] = false;
        }
        new AlertDialog.Builder(mContext)
                .setMultiChoiceItems(teacherNameItems, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if (isChecked) {
                            teacherIdItems[which] = teacherDataList.get(which).TeacherID;
                        } else {
                            teacherIdItems[which] = "";
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for (int i = 0; i < teacherIdItems.length; i++) {
                            if (!TextUtils.isEmpty(teacherIdItems[i])) {
                                checkedIdList.add(teacherIdItems[i]);
                            }
                        }
                        RequestShareKnowledge request = new RequestShareKnowledge();
                        request.knowledge_id = knowledge_id;
                        request.teachers = checkedIdList;
                        if (checkedIdList.size() > 0) {
                            ScopeServer.getInstance().ShareKnowledgeToTeachers(request, shareCallback);
                        }
                    }
                }).show();
    }

    @Override
    public void onProgress(boolean b) {

    }

    @Override
    public void onResponse(String body) {
        Toast.makeText(mContext, R.string.operate_success, Toast.LENGTH_SHORT).show();
        mListener.refreshData();
    }

    @Override
    public void onError(String code, String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
    }

    public interface Callback {
        void refreshData();
    }
}
