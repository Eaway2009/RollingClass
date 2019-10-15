package com.tanhd.rollingclass.views;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.StackedValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.server.data.QuestionInfo;
import com.tanhd.rollingclass.utils.MyValueFormatter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YangShlai on 2019-10-15.
 */
public class MyBarChartView extends LinearLayout {
    private BarChart chart;

    public MyBarChartView(Context context) {
        this(context, null);
    }

    public MyBarChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.my_barcahart, this, true);

        chart = findViewById(R.id.barchart);

        chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
            }

            @Override
            public void onNothingSelected() {
            }
        });

        chart.getDescription().setEnabled(false);
        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        chart.setMaxVisibleValueCount(40);


        // scaling can now only be done on x- and y-axis separately
        chart.setPinchZoom(false);
        chart.setDoubleTapToZoomEnabled(false);

        chart.setDrawGridBackground(false);
        chart.setDrawBarShadow(false);

        chart.setDrawValueAboveBar(false);
        chart.setHighlightFullBarEnabled(false);

        // chart.setDrawXLabels(false);
        // chart.setDrawYLabels(false);

        // setting data

        Legend l = chart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setFormSize(8f);
        l.setFormToTextSpace(4f);
        l.setXEntrySpace(6f);
    }

    /**
     * 清空数据
     */
    public void clearData(){
        chart.clear();
    }

    /**
     * 设置数据
     * @param title       标题
     * @param stackLabels 图例
     * @param questionInfoList      数据
     * @param xFormatter  X 轴
     * @param yFormatter  Y 轴
     * @param appendix
     */
    public void setData(String title, String[] stackLabels,List<QuestionInfo> questionInfoList, MyValueFormatter xFormatter, MyValueFormatter yFormatter, String appendix) {
        //X
        XAxis xLabels = chart.getXAxis();
        xLabels.setPosition(XAxis.XAxisPosition.BOTTOM);
        xLabels.setValueFormatter(xFormatter);
        xLabels.setTextColor(Color.parseColor("#8B8B8B"));
        xLabels.setDrawGridLines(false);

        //Y
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setValueFormatter(yFormatter);
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)
        leftAxis.setDrawGridLines(false);
        chart.getAxisRight().setEnabled(false);

        ArrayList<BarEntry> values = new ArrayList<>();
        int size = questionInfoList.size();
        for (int i = 0; i < size; i++) {
            QuestionInfo questionInfo = questionInfoList.get(i);
            float val1 = (float)questionInfo.correct_cnt;
            float val2 = (float)questionInfo.error_cnt;
            float val3 = (float)questionInfo.unanswer_cnt;
            values.add(new BarEntry(i + 1, new float[]{val1, val2, val3}));
        }

        BarDataSet set1;
        if (chart.getData() != null &&
                chart.getData().getDataSetCount() > 0) {
            set1 = (BarDataSet) chart.getData().getDataSetByIndex(0);
            set1.setValues(values);
            chart.getData().notifyDataChanged();
            chart.notifyDataSetChanged();
        } else {
            set1 = new BarDataSet(values,title);
            set1.setColors(ColorTemplate.rgb("#3DC900"), ColorTemplate.rgb("#F94C4C"), ColorTemplate.rgb("#FFFF73"));
            set1.setValueTextColor(ColorTemplate.rgb("#000000"));
            set1.setValueFormatter(new MyValueFormatter("", appendix));
            set1.setValueTextSize(11f);

            if (stackLabels != null && stackLabels.length > 1){
                set1.setStackLabels(stackLabels);
            }


            ArrayList<IBarDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1);

            BarData data = new BarData(dataSets);
            data.setBarWidth(0.5f);
//            data.setValueFormatter(new StackedValueFormatter(false, "", 1));
            //data.setValueTextColor(Color.WHITE);

            chart.setData(data);
        }

        chart.setFitBars(true);
        chart.invalidate();
    }

}
