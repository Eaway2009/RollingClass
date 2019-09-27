//package com.tanhd.rollingclass.views;
//
//import android.content.DialogInterface;
//import android.support.v7.app.AlertDialog;
//import android.text.TextUtils;
//import android.widget.Toast;
//
//import com.tanhd.rollingclass.R;
//import com.tanhd.rollingclass.server.RequestCallback;
//import com.tanhd.rollingclass.server.ScopeServer;
//import com.tanhd.rollingclass.server.data.RequestShareKnowledge;
//import com.tanhd.rollingclass.server.data.TeacherData;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class ClassCheckWindow {
//
//    private static ClassCheckWindow mClassCheckWindow;
//
//    private ClassCheckWindow(){
//
//    }
//
//    public static ClassCheckWindow getInstance(){
//        if(mClassCheckWindow==null){
//            mClassCheckWindow = new ClassCheckWindow();
//        }
//        return mClassCheckWindow;
//    }
//
//    private void initClassListDialog() {
//        final RequestCallback shareCallback = new RequestCallback() {
//            @Override
//            public void onProgress(boolean b) {
//
//            }
//
//            @Override
//            public void onResponse(String body) {
//            }
//
//            @Override
//            public void onError(String code, String message) {
//                Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
//            }
//        };
//
//        if (teacherDataList == null) {
//            return;
//        }
//        String[] teacherNameItems = new String[teacherDataList.size()];
//        final String[] teacherIdItems = new String[teacherDataList.size()];
//        boolean[] checkedItems = new boolean[teacherDataList.size()];
//        final List<String> checkedIdList = new ArrayList<>();
//
//        for (int i = 0; i < teacherDataList.size(); i++) {
//            teacherNameItems[i] = teacherDataList.get(i).Username;
//            teacherIdItems[i] = teacherDataList.get(i).TeacherID;
//            checkedItems[i] = false;
//        }
//        new AlertDialog.Builder(mContext)
//                .setMultiChoiceItems(teacherNameItems, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
//                        if (isChecked) {
//                            teacherIdItems[which] = teacherDataList.get(which).TeacherID;
//                        } else {
//                            teacherIdItems[which] = "";
//                        }
//                    }
//                })
//                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//
//                    }
//                })
//                .setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        for (int i = 0; i < teacherIdItems.length; i++) {
//                            if (!TextUtils.isEmpty(teacherIdItems[i])) {
//                                checkedIdList.add(teacherIdItems[i]);
//                            }
//                        }
//                        RequestShareKnowledge request = new RequestShareKnowledge();
//                        request.knowledge_id = knowledge_id;
//                        request.teachers = checkedIdList;
//                        if (checkedIdList.size() > 0) {
//                            ScopeServer.getInstance().ShareKnowledgeToTeachers(request, shareCallback);
//                        }
//                    }
//                }).show();
//    }
//}
