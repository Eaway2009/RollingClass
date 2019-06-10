package com.tanhd.rollingclass.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.formatter.StackedValueFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.Utils;
import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.utils.AppUtils;
import com.tanhd.rollingclass.utils.MyValueFormatter;

import java.util.ArrayList;
import java.util.List;

public class LineChartView extends LinearLayout {
    private LineChart mLineChart;
    private TextView mTitleView;
    private List<Entry> yEntries;
    private String mTitle;

    public LineChartView(Context context) {
        super(context);
    }

    public LineChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LineChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mLineChart = findViewById(R.id.linechart);
        mTitleView = findViewById(R.id.chart_title);
        init(mLineChart);
    }

    private void initChart(LineChart chart) {
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
        l.setDirection(Legend.LegendDirection.LEFT_TO_RIGHT);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        // l.setYOffset(11f);

        XAxis xAxis = chart.getXAxis();
        xAxis.setTextSize(11f);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setAxisMinimum(0);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setTextColor(ColorTemplate.getHoloBlue());
        leftAxis.setAxisMinimum(0);
        leftAxis.setDrawGridLines(false);
        leftAxis.setGranularityEnabled(true);
        leftAxis.setValueFormatter(new MyValueFormatter("", "次"));

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setDrawAxisLine(false);
        rightAxis.setDrawLabels(false);
        rightAxis.setTextColor(Color.BLACK);
        rightAxis.setDrawGridLines(false);
        rightAxis.setDrawZeroLine(false);
        rightAxis.setGranularityEnabled(false);

    }

    private void init(LineChart chart) {
        if (yEntries == null)
            return;

        initChart(chart);

        mTitleView.setText(mTitle);
        LineDataSet set1;

        if (chart.getData() != null && chart.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet) chart.getData().getDataSetByIndex(0);
            set1.setValues(yEntries);
            set1.notifyDataSetChanged();
            chart.getData().notifyDataChanged();
            chart.notifyDataSetChanged();
        } else {
            // create a dataset and give it a type
            set1 = new LineDataSet(yEntries, mTitle);

            // draw dashed line
            //set1.enableDashedLine(10f, 5f, 0f);

            // black lines and points
            set1.setColor(Color.BLACK);
            set1.setCircleColor(Color.BLACK);

            // line thickness and point size
            set1.setLineWidth(1f);
            set1.setCircleRadius(3f);

            // draw points as solid circles
            set1.setDrawCircleHole(false);

            // customize legend entry
            //set1.setFormLineWidth(1f);
            //set1.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            //set1.setFormSize(15.f);

            // text size of values
            set1.setValueTextSize(9f);

            // draw selection line as dashed
            //set1.enableDashedHighlightLine(10f, 5f, 0f);

            // set the filled area
            //set1.setDrawFilled(true);
            /*set1.setFillFormatter(new IFillFormatter() {
                @Override
                public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
                    return mLineChart.getAxisLeft().getAxisMinimum();
                }
            });*/
            set1.setValueFormatter(new MyValueFormatter("", ""));
            set1.setDrawValues(false);

            // set color of filled area
            if (Utils.getSDKInt() >= 18) {
                // drawables only supported on api level 18 and above
                Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.fade_red);
                set1.setFillDrawable(drawable);
            } else {
                set1.setFillColor(Color.BLACK);
            }

            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1); // add the data sets

            // create a data object with the data sets
            LineData data = new LineData(dataSets);

            // set data
            chart.setData(data);
            chart.invalidate();
        }
    }

    public void setData(String title, List<Entry> entries) {
        mTitle = title;
        yEntries = entries;

        init(mLineChart);
    }

}
