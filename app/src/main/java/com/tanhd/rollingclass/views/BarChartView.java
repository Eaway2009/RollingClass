package com.tanhd.rollingclass.views;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.StackedValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.utils.AppUtils;
import com.tanhd.rollingclass.utils.MyValueFormatter;

import java.util.ArrayList;
import java.util.List;

public class BarChartView extends LinearLayout {
    private BarChart mBarChart;
    private TextView mTitleView;
    private List<BarEntry> yEntries;
    private List<String> xLabels;
    private String mTitle;
    private String[] mStackLables;
    private MyValueFormatter yValueFormatter;
    private MyValueFormatter xValueFormatter;
    private String mAppendix;

    public BarChartView(Context context) {
        super(context);
    }

    public BarChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BarChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mBarChart = findViewById(R.id.barchart);
        mTitleView = findViewById(R.id.chart_title);
        init(mBarChart);
    }

    private void init(BarChart chart) {
        if (yEntries == null)
            return;

        chart.getDescription().setEnabled(false);

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        chart.setMaxVisibleValueCount(40);

        // scaling can now only be done on x- and y-axis separately
        chart.setPinchZoom(false);

        chart.setDrawGridBackground(false);
        chart.setDrawBarShadow(false);

        chart.setDrawValueAboveBar(false);
        chart.setHighlightFullBarEnabled(false);
        //chart.animateXY(2000, 2000);
        chart.animateY(500);

        // change the position of the y-labels
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setValueFormatter(yValueFormatter);
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)
        leftAxis.setDrawTopYLabelEntry(false);
        //leftAxis.setDrawGridLines(false);
        //leftAxis.setDrawAxisLine(false);
        chart.getAxisRight().setEnabled(false);

        XAxis xLabels = chart.getXAxis();
        xLabels.setPosition(XAxis.XAxisPosition.BOTTOM);
        xLabels.setValueFormatter(xValueFormatter);
        //xLabels.setDrawGridLines(false);

        // chart.setDrawXLabels(false);
        // chart.setDrawYLabels(false);

        Legend l = chart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setFormSize(8f);
        l.setFormToTextSpace(4f);
        l.setXEntrySpace(6f);

        mTitleView.setText(mTitle);
        BarDataSet set1;

        if (mBarChart.getData() != null &&
                mBarChart.getData().getDataSetCount() > 0) {
            set1 = (BarDataSet) mBarChart.getData().getDataSetByIndex(0);
            set1.setValues(yEntries);
            mBarChart.getData().notifyDataChanged();
            mBarChart.notifyDataSetChanged();
        } else {
            set1 = new BarDataSet(yEntries, mTitle);
            set1.setColors(ColorTemplate.rgb("#2ecc71"), ColorTemplate.rgb("#e74c3c"), ColorTemplate.rgb("#f1c40f"));
            if (mStackLables != null) {
                set1.setStackLabels(mStackLables);
            }

            set1.setDrawValues(true);

            ArrayList<IBarDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1);

            BarData data = new BarData(dataSets);
            data.setValueFormatter(new MyValueFormatter("", mAppendix));
            //data.setValueFormatter(new StackedValueFormatter(true, mAppendix, 0));
            data.setValueTextColor(Color.WHITE);

            mBarChart.setData(data);
        }

        mBarChart.setFitBars(true);
        mBarChart.invalidate();
    }

    public void setData(String title, String[] stackLabels, List<BarEntry> entries, MyValueFormatter xFormatter, MyValueFormatter yFormatter, String appendix) {
        mTitle = title;
        mStackLables = stackLabels;
        yEntries = entries;
        xValueFormatter = xFormatter;
        yValueFormatter = yFormatter;
        mAppendix = appendix;

        mBarChart = findViewById(R.id.barchart);
        mTitleView = findViewById(R.id.chart_title);
        init(mBarChart);


    }

    public void setOnChartValueSelectedListener(OnChartValueSelectedListener listener) {
        mBarChart.setOnChartValueSelectedListener(listener);
    }

}
