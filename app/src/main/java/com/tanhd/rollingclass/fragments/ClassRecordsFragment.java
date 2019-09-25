package com.tanhd.rollingclass.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.server.data.KnowledgeDetailMessage;
import com.tanhd.rollingclass.utils.StringUtils;
import com.tanhd.rollingclass.views.RecordAdapter;
import com.tanhd.rollingclass.views.RecordsSpinnerAdapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ClassRecordsFragment extends Fragment {

    private static final String PARAM_KNOWLEDGE = "PARAM_KNOWLEDGE";
    private KnowledgeDetailMessage mKnowledgeMessage;
    private Spinner mYearSpinner;
    private View mYearSpinnerView;
    private Spinner mMonthSpinner;
    private View mMonthSpinnerView;
    private ListView mRecordsListView;
    private RecordAdapter mAdapter;
    private RecordsSpinnerAdapter mYearAdapter;
    private RecordsSpinnerAdapter mMonthAdapter;
    private TextView mYearTextView;
    private TextView mMonthTextView;
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
        View contentView = inflater.inflate(R.layout.fragment_class_answering, null);
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
        mRecordsListView = contentView.findViewById(R.id.records_listview);
        mAdapter = new RecordAdapter();
        mRecordsListView.setAdapter(mAdapter);

        mYearSpinner = contentView.findViewById(R.id.year_spinner);
        mYearTextView = contentView.findViewById(R.id.year_textview);
        mYearSpinnerView = contentView.findViewById(R.id.year_spinner_layout);

        mMonthSpinner = contentView.findViewById(R.id.month_spinner);
        mMonthTextView = contentView.findViewById(R.id.month_textview);
        mMonthSpinnerView = contentView.findViewById(R.id.month_spinner_layout);

        mYearAdapter = new RecordsSpinnerAdapter(getContext(), true);
        mYearSpinner.setAdapter(mYearAdapter);
        mYearAdapter.setDataList(mKnowledgeMessage.records);
        mYearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String yearText = StringUtils.getFormatYear(((KnowledgeDetailMessage.Record) mYearAdapter.getItem(pos)).time_record);
                flitData(yearText, mMonthText);
                mYearTextView.setText(yearText);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        mMonthAdapter = new RecordsSpinnerAdapter(getContext(), false);
        mMonthSpinner.setAdapter(mMonthAdapter);
        mMonthAdapter.setDataList(mKnowledgeMessage.records);
        mMonthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String monthText = StringUtils.getFormatMonth(((KnowledgeDetailMessage.Record) mMonthAdapter.getItem(pos)).time_record);
                flitData(mYearText, monthText);
                mMonthTextView.setText(monthText);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void initData() {
        flitData(StringUtils.getFormatYear(new Date()), StringUtils.getFormatMonth(new Date()));
    }

    private void flitData(String year, String month) {
        mYearText = year;
        mMonthText = month;
        List<KnowledgeDetailMessage.Record> records = new ArrayList<>();
        for (KnowledgeDetailMessage.Record record : mKnowledgeMessage.records) {
            if (StringUtils.getFormatYear(record.time_record).equals(year) && StringUtils.getFormatMonth(record.time_record).equals(month)) {
                records.add(record);
            }
        }
        mAdapter.setData(records);
    }
}
