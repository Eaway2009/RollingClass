package com.tanhd.rollingclass.views;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.LessonSampleData;
import com.tanhd.rollingclass.server.data.StudentData;
import com.tanhd.rollingclass.utils.AppUtils;
import com.tanhd.rollingclass.utils.PercentFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.mikephil.charting.animation.Easing.EasingOption.EaseInOutQuad;

public class ExamPieChartView extends LinearLayout {
    private StudentData mStudentData;
    private OnChartValueSelectedListener mListener;

    public ExamPieChartView(Context context) {
        super(context);
    }

    public ExamPieChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ExamPieChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setData(StudentData studentData, OnChartValueSelectedListener listener) {
        mStudentData = studentData;
        mListener = listener;
        new LoadDataTask().execute();
    }

    private class LoadDataTask extends AsyncTask<Void, Void, Map> {

        @Override
        protected Map doInBackground(Void... voids) {
            LessonSampleData lessonSampleData = ExternalParam.getInstance().getLessonSample();
            HashMap<String, List> result = ScopeServer.getInstance().CountStudentLessonSample(mStudentData.StudentID, lessonSampleData.LessonSampleID);
            return result;
        }

        @Override
        protected void onPostExecute(Map map) {
            if (map == null)
                return;

            initPieChart(map);
        }
    }

    private void initPieChart(Map map) {
        PieChart chart = findViewById(R.id.piechart);
        chart.setUsePercentValues(true);
        chart.getDescription().setEnabled(false);
        chart.setExtraOffsets(5, 10, 5, 5);

        chart.setDragDecelerationFrictionCoef(0.95f);
        chart.setCenterText(getResources().getString(R.string.lbl_answer_situation));

        chart.setDrawHoleEnabled(true);
        chart.setHoleColor(Color.WHITE);

        chart.setTransparentCircleColor(Color.WHITE);
        chart.setTransparentCircleAlpha(110);

        chart.setHoleRadius(58f);
        chart.setTransparentCircleRadius(61f);

        chart.setDrawCenterText(true);

        chart.setRotationAngle(0);
        // enable rotation of the chart by touch
        chart.setRotationEnabled(true);
        chart.setHighlightPerTapEnabled(true);

        if (mListener != null)
            chart.setOnChartValueSelectedListener(mListener);

        // chart.setUnit(" €");
        // chart.setDrawUnitsInChart(true);

        // add a selection listener

        chart.animateY(500, EaseInOutQuad);
        // chart.spin(2000, 0, 360);

        Legend l = chart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);

        // entry label styling
        chart.setDrawEntryLabels(false);
        chart.setEntryLabelColor(Color.WHITE);
        chart.setEntryLabelTextSize(18f);

        ArrayList<PieEntry> entries = new ArrayList<>();

        // NOTE: The order of the entries when being added to the entries array determines their position around the center of
        // the chart.
        List correctArray = (List) map.get("CorrectArray");
        List errorArray = (List) map.get("ErrorArray");
        List unAnswerArray = (List) map.get("UnAnswerArray");

        PieEntry entry = new PieEntry(correctArray.size(), getResources().getString(R.string.lbl_exactness));
        entry.setData(correctArray);
        entries.add(entry);

        entry = new PieEntry(errorArray.size(), getResources().getString(R.string.lbl_err));
        entry.setData(errorArray);
        entries.add(entry);

        entry = new PieEntry(unAnswerArray.size(), getContext().getResources().getString(R.string.lbl_unanswered));
        entry.setData(unAnswerArray);
        entries.add(entry);

        PieDataSet dataSet = new PieDataSet(entries, getContext().getResources().getString(R.string.lbl_answer_tj));

        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        // add a lot of colors
        dataSet.setColors(ColorTemplate.rgb("#2ecc71"), ColorTemplate.rgb("#e74c3c"), ColorTemplate.rgb("#f1c40f"));
        //dataSet.setSelectionShift(0f);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(chart));
        data.setValueTextSize(18f);
        data.setValueTextColor(Color.WHITE);
        chart.setData(data);

        // undo all highlights
        chart.highlightValues(null);
        chart.invalidate();
    }
}
