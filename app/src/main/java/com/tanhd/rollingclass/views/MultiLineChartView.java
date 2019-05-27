package com.tanhd.rollingclass.views;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.utils.MyValueFormatter;

import java.util.List;

public class MultiLineChartView extends LinearLayout {
    private LineChart mLineChart;

    public MultiLineChartView(Context context) {
        super(context);
    }

    public MultiLineChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MultiLineChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void initChart(LineChart chart, int max) {
        // no description text
        chart.getDescription().setEnabled(false);

        // enable touch gestures
        chart.setTouchEnabled(true);

        chart.setDragDecelerationFrictionCoef(0.9f);

        // enable scaling and dragging
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setDrawGridBackground(false);
        chart.setHighlightPerDragEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        chart.setPinchZoom(true);


        // set an alternative background color
        chart.setBackgroundColor(Color.WHITE);

        chart.animateX(500);

        // get the legend (only possible after setting data)
        Legend l = chart.getLegend();

        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);
//        l.setTextSize(11f);
        l.setTextColor(Color.BLACK);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        // l.setYOffset(11f);

        XAxis xAxis = chart.getXAxis();
        xAxis.setTextSize(11f);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setAxisMaximum(max);
        xAxis.setAxisMinimum(0);
        xAxis.setDrawGridLines(true);
        xAxis.setDrawAxisLine(true);
        xAxis.setValueFormatter(new MyValueFormatter("", ""));

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setTextColor(ColorTemplate.getHoloBlue());
        leftAxis.setAxisMinimum(0);
        leftAxis.setDrawGridLines(true);
        leftAxis.setGranularityEnabled(true);
        leftAxis.setValueFormatter(new MyValueFormatter("第", "次"));

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setDrawAxisLine(true);
        rightAxis.setDrawLabels(false);
        rightAxis.setTextColor(Color.BLACK);
        rightAxis.setDrawGridLines(false);
        rightAxis.setDrawZeroLine(false);
        rightAxis.setGranularityEnabled(false);
    }

    public void addData(List<Entry> value, int color, String label, int duration) {
        if (mLineChart == null) {
            mLineChart = findViewById(R.id.mutil_linechart);
            if (mLineChart == null)
                return;

            initChart(mLineChart, duration);
        }

        LineDataSet set = new LineDataSet(value, label);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(color);
        set.setCircleColor(Color.BLACK);
        set.setLineWidth(2f);
        set.setCircleRadius(3f);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setDrawCircleHole(false);
        set.setDrawValues(false);

        if (mLineChart.getData() != null) {
            LineData lineData = mLineChart.getData();
            lineData.addDataSet(set);
            lineData.notifyDataChanged();
            mLineChart.notifyDataSetChanged();
        } else {
            LineData lineData = new LineData(set);
            mLineChart.setData(lineData);
            mLineChart.invalidate();
        }
    }

    public void clearData() {
        if (mLineChart == null || mLineChart.getData() == null) {
            return;
        }

        mLineChart.getData().clearValues();
        mLineChart.invalidate();
    }
}
