package com.tanhd.rollingclass.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.KnowledgeDetailMessage;
import com.tanhd.rollingclass.server.data.UserData;
import com.tanhd.rollingclass.utils.StringUtils;
import com.tanhd.rollingclass.utils.TimeUtils;
import com.tanhd.rollingclass.views.KnowledgeRecordAdapter;
import com.tanhd.rollingclass.views.OnItemClickListener;
import com.tanhd.rollingclass.views.PopFliterRes;
import com.tanhd.rollingclass.views.RecordAdapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 上课记录/学习记录
 */
public class ClassRecordsFragment extends Fragment {

    private static final String PARAM_KNOWLEDGE = "PARAM_KNOWLEDGE";
    private KnowledgeDetailMessage mKnowledgeMessage;
    private ListView mRecordsListView;
    private RecordAdapter mAdapter;
    private KnowledgeRecordAdapter knowledgeRecordAdapter;
    private TextView tv_one_lbl,tv_two_lbl,tv_three_lbl;
    private TextView tv_year;
    private TextView tv_month;
    private PopFliterRes popYear, popMoneth;

    private String mYearText;
    private String mMonthText;

    public static ClassRecordsFragment getInstance(KnowledgeDetailMessage knowledgeDetailMessage) {
        ClassRecordsFragment classRecordsFragment = new ClassRecordsFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(PARAM_KNOWLEDGE, knowledgeDetailMessage);
        classRecordsFragment.setArguments(bundle);
        return classRecordsFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_class_records, null);
        initParams();
        initViews(contentView);
        initData();
        return contentView;
    }

    private void initParams() {
        Bundle args = getArguments();
        mKnowledgeMessage = (KnowledgeDetailMessage) args.getSerializable(PARAM_KNOWLEDGE);
    }

    private void initViews(View contentView) {
        tv_one_lbl = contentView.findViewById(R.id.tv_one_lbl);
        tv_two_lbl = contentView.findViewById(R.id.tv_two_lbl);
        tv_three_lbl = contentView.findViewById(R.id.tv_three_lbl);
        mRecordsListView = contentView.findViewById(R.id.records_listview);



        if (ExternalParam.getInstance().getUserData().isTeacher()){ //老师端
            mAdapter = new RecordAdapter(getActivity());
            mRecordsListView.setAdapter(mAdapter);
        }else{  //学生端学习记录
            tv_one_lbl.setText(R.string.study_the_data);
            tv_two_lbl.setText(R.string.study_the_time);
            tv_three_lbl.setText(R.string.study_the_status);

            knowledgeRecordAdapter = new KnowledgeRecordAdapter(getActivity());
            mRecordsListView.setAdapter(knowledgeRecordAdapter);
        }

        tv_year = contentView.findViewById(R.id.tv_year);
        tv_month = contentView.findViewById(R.id.tv_month);

        List<String> monthList = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            monthList.add((i < 10 ? ("0" + i) : i) + getResources().getString(R.string.lbl_month));
        }
        popMoneth = new PopFliterRes(getActivity());
        popMoneth.setDatas(monthList);
        popMoneth.setRootWidth((int) getResources().getDimension(R.dimen.dp_110));
        popMoneth.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                mMonthText = popMoneth.getDatas().get(position);
                tv_month.setText(mMonthText);
                //选中
                flitData(mYearText, mMonthText);
            }
        });

        List<String> yearList = new ArrayList<>();
        yearList.add("2019" + getResources().getString(R.string.lbl_year));
        yearList.add("2018" + getResources().getString(R.string.lbl_year));
        yearList.add("2017" + getResources().getString(R.string.lbl_year));

        popYear = new PopFliterRes(getActivity());
        popYear.setDatas(yearList);
        popYear.setRootWidth((int) getResources().getDimension(R.dimen.dp_110));
        popYear.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                mYearText = popYear.getDatas().get(position);
                tv_year.setText(mYearText);
                //选中
                flitData(mYearText, mMonthText);
            }
        });

        tv_year.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popYear.showMask(false).showAsDropDown(v);
            }
        });

        //月
        tv_month.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popMoneth.showMask(false).showAsDropDown(v);
            }
        });
    }

    private void initData() {
        mYearText = StringUtils.getFormatYear(new Date());
        mMonthText = StringUtils.getFormatMonth(new Date());

        for (int i = 0; i < popMoneth.getDatas().size(); i++) {
            if (popMoneth.getDatas().get(i).equals(mMonthText)) {
                popMoneth.setNowPos(i);
                break;
            }
        }

        tv_month.setText(mMonthText);
        tv_year.setText(mYearText);
        flitData(mYearText, mMonthText);
    }

    private void flitData(String year, String month) {
        mYearText = year;
        mMonthText = month;

        if (ExternalParam.getInstance().getUserData().isTeacher()){ //老师
            List<KnowledgeDetailMessage.Record> records = new ArrayList<>();
            for (KnowledgeDetailMessage.Record record : mKnowledgeMessage.records) {
                if (StringUtils.getFormatYear(record.time_record).equals(year) && StringUtils.getFormatMonth(record.time_record).equals(month)) {
                    records.add(record);
                }
            }
            mAdapter.setData(records);
        }else{
            List<KnowledgeDetailMessage.KnowledgeRecord> records = new ArrayList<>();
            for (KnowledgeDetailMessage.KnowledgeRecord record : mKnowledgeMessage.knowledge_records) {
                String ymd = TimeUtils.longToStr(record.getCreate_time(),TimeUtils.DEFAULT_FORMAT);
                if (TextUtils.isEmpty(ymd)) continue;
                String[] ymdArray = ymd.split("-");
                if (ymdArray.length == 0) continue;
                String mYear = ymdArray[0] + "年";
                String mMonth = ymdArray[1] + "月";
                if (year.equals(mYear) && month.equals(mMonth)) {
                    records.add(record);
                }
            }
            knowledgeRecordAdapter.setData(records);
        }
    }
}
