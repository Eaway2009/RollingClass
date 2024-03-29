package com.tanhd.rollingclass.views;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.StackedValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
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
        chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
            }

            @Override
            public void onNothingSelected() {
            }
        });

        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        chart.setMaxVisibleValueCount(40);

        // scaling can now only be done on x- and y-axis separately
        chart.setPinchZoom(false);
//        chart.setEnabled(false);
        chart.setDoubleTapToZoomEnabled(false);

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
        //leftAxis.setDrawTopYLabelEntry(false);
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

//        yEntries.clear();
//        for (int i = 0; i < 10; i++) {
//            //float mul = (100 + 1);
////            float val1 = (float) (Math.random() * mul) + mul / 3;
////            float val2 = (float) (Math.random() * mul) + mul / 3;
////            float val3 = (float) (Math.random() * mul) + mul / 3;
//
//            float val1 = (float) 30;
//            float val2 = (float) 20;
//            float val3 = (float) 50;
//
//            yEntries.add(new BarEntry(i, new float[]{val1, val2, val3}));
//        }

        BarDataSet set1;
        if (mBarChart.getData() != null &&
                mBarChart.getData().getDataSetCount() > 0) {
            set1 = (BarDataSet) mBarChart.getData().getDataSetByIndex(0);
            set1.setValues(yEntries);
            mBarChart.getData().notifyDataChanged();
            mBarChart.notifyDataSetChanged();
        } else {
            set1 = new BarDataSet(yEntries,mTitle);
            set1.setColors(ColorTemplate.rgb("#2ecc71"), ColorTemplate.rgb("#e74c3c"), ColorTemplate.rgb("#f1c40f"));
//            if (mStackLables != null) {
//                set1.setStackLabels(mStackLables);
//            }
            set1.setStackLabels(new String[]{"Births", "Divorces", "Marriages"});

            //set1.setDrawValues(true);

            ArrayList<IBarDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1);

            BarData data = new BarData(dataSets);
            data.setValueFormatter(new MyValueFormatter("", mAppendix));
            //data.setValueFormatter(new StackedValueFormatter(true, mAppendix, 0));
//            data.setValueFormatter(new StackedValueFormatter(false, "", 1));
            data.setValueTextColor(Color.WHITE);

            mBarChart.setData(data);
        }

        mBarChart.setFitBars(true);
        mBarChart.invalidate();
    }

    public void setOnChartValueSelectedListener(OnChartValueSelectedListener listener) {
        mBarChart.setOnChartValueSelectedListener(listener);
    }

    /**
     * 设置数据
     *
     * @param title       标题
     * @param stackLabels 图例
     * @param entries     数据
     * @param xFormatter  X 轴
     * @param yFormatter  Y 轴
     * @param appendix
     */
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

}
