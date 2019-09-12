package com.tanhd.rollingclass.fragments.pages;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.KnowledgeData;
import com.tanhd.rollingclass.server.data.KnowledgeDetailMessage;
import com.tanhd.rollingclass.server.data.LessonSampleData;
import com.tanhd.rollingclass.server.data.StudentData;
import com.tanhd.rollingclass.server.data.TeachingMaterialData;
import com.tanhd.rollingclass.views.StudentListView;

import java.io.Serializable;
import java.util.List;

public class CountExamPage extends Fragment {
    private static final String PARAM_STUDENT_DATA = "PARAM_STUDENT_DATA";
    private static final String PARAM_TEACHING_MATERIAL = "PARAM_TEACHING_MATERIAL";

    private StudentListView mStudentListView;
    private Spinner mKnowLedgeSpinner;
    private List<TeachingMaterialData> mItemList;
    private ArrayAdapter mAdapter;
    private StudentData mStudentData;
    private String mTeachingMaterialId;

    public static CountExamPage getInstance(StudentData studentData, String teachingMaterial) {
        CountExamPage countExamPage = new CountExamPage();
        Bundle bundle = new Bundle();
        bundle.putSerializable(PARAM_STUDENT_DATA, studentData);
        bundle.putSerializable(PARAM_TEACHING_MATERIAL, teachingMaterial);
        countExamPage.setArguments(bundle);
        return countExamPage;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.page_count_exam, container, false);
        initParams();
        initViews(view);
        initDatas();
        return view;
    }

    private void initParams() {
        Bundle args = getArguments();
        mStudentData = (StudentData) args.getSerializable(PARAM_STUDENT_DATA);
        mTeachingMaterialId = args.getString(PARAM_TEACHING_MATERIAL);
    }

    private void initViews(View view) {
        //关联控件
        mKnowLedgeSpinner = (Spinner) view.findViewById(R.id.knowledge_select_view);

        // 将可选内容与ArrayAdapter连接起来

        mAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_item) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                TextView textView = convertView.findViewById(android.R.id.text1);
                LessonSampleData item = (LessonSampleData) getItem(position);
                textView.setText(item.LessonSampleName);
                return super.getView(position, convertView, parent);
            }
        };
        // 第1个参数为Context对象
        // 第2个参数为设置Spinner的样式

        // 设置Spinner中每一项的样式
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // 设置Spinner数据来源适配器
        mKnowLedgeSpinner.setAdapter(mAdapter);

        // 使用内部类形式来实现事件监听
        mKnowLedgeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                /*
                 * 第一个参数parent是你当前所操作的Spinner，可根据parent.getId()与R.id.
                 * currentSpinner是否相等，来判断是否你当前操作的Spinner,一般在onItemSelected
                 * 方法中用switch语句来解决多个Spinner问题。
                 * 第二个参数view一般不用到；
                 * 第三个参数position表示下拉中选中的选项位置，自上而下从0开始；
                 * 第四个参数id表示的意义与第三个参数相同。
                 */

                //对选中项进行显示
                //Toast用于临时信息的显示
                //第一个参数是上下文环境，可用this；
                //第二个参数是要显示的字符串；
                //第三个参数是显示的时间长短；
                KnowledgeDetailMessage lessonSampleData = (KnowledgeDetailMessage) parent.getItemAtPosition(position);
                Toast.makeText(getActivity(), "您选择了：" + lessonSampleData.knowledge_point_name, Toast.LENGTH_LONG)
                        .show();
                checkData(lessonSampleData);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub

            }
        });
    }

    private void initDatas() {
        new InitDataTask().execute();
    }

    public void checkData(KnowledgeDetailMessage lessonSampleData) {
        CountStudentExamPage page = CountStudentExamPage.newInstance(mStudentData, lessonSampleData);
        showFragment(page);
    }

    private class InitDataTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            List<KnowledgeDetailMessage> sampleList = ScopeServer.getInstance().QureyKnowledgeByChapterAndTeacherID(ExternalParam.getInstance().getUserData().getOwnerID(), mTeachingMaterialId);
            if (sampleList == null)
                return null;

            for (KnowledgeDetailMessage sampleData : sampleList) {
                KnowledgeData knowledgeData = ScopeServer.getInstance().QureyKnowledgeByID(sampleData.knowledge_id);
                if (knowledgeData == null)
                    continue;

                TeachingMaterialData materialData = ScopeServer.getInstance().QueryTeachingMaterialById(knowledgeData.TeachingMaterialID);
                if (materialData == null)
                    continue;
                mItemList.add(materialData);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mAdapter.addAll(mItemList);
            mAdapter.notifyDataSetChanged();
        }
    }

    private void showFragment(Fragment fragment) {
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction beginTransaction = fragmentManager.beginTransaction();
        beginTransaction.replace(R.id.framelayout, fragment);
        beginTransaction.addToBackStack("exam");
        beginTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        beginTransaction.commit();
    }
}
