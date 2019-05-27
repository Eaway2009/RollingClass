package com.tanhd.rollingclass.utils;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;


import java.text.DecimalFormat;

public class MyValueFormatter extends ValueFormatter
{

    private final DecimalFormat mFormat;
    private String suffix0;
    private String suffix1;

    public MyValueFormatter(String suffix0, String suffix1) {
        mFormat = new DecimalFormat("###,###,###,##0");
        this.suffix0 = suffix0;
        this.suffix1 = suffix1;
    }

    @Override
    public String getFormattedValue(float value) {
        if (value == 0.0f)
            return "";

        return suffix0 + mFormat.format(value) + suffix1;
    }

    @Override
    public String getAxisLabel(float value, AxisBase axis) {
        if (value == 0.0f)
            return "";

        if (axis instanceof XAxis) {
            return mFormat.format(value);
        } else if (value > 0) {
            return suffix0 + mFormat.format(value) + suffix1;
        } else {
            return mFormat.format(value);
        }
    }
}
