package com.tanhd.rollingclass.server.data;

import com.tanhd.rollingclass.utils.AppUtils;

import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class QuestionData extends BaseJsonClass {
    public long CreateTime;
    public String LessonSampleID;
    public String LessonSampleName;
    public String QuestionCoordinate;
    public String QuestionID;
    public String QuestionName;
    public String Remark;
    public String TeacherID;
    public String TeacherName;
    public long UpdateTime;
    public int QuestionType;
    public ContextData Context;

    @Override
    protected void onDealListField(Object object, Field field, JSONObject json, String key) {
        super.onDealListField(object, field, json, key);
        if (key.equals("Context")) {
            ContextData contextData = new ContextData();
            contextData.parse(contextData, json.optJSONObject(key));
            try {
                field.set(object, contextData);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isChoiceType() {
        if (Context.Options != null && !Context.Options.isEmpty())
            return true;
        return false;
    }

    private static class SortComparator implements Comparator {
        @Override
        public int compare(Object lhs, Object rhs) {
            QuestionData a = (QuestionData) lhs;
            QuestionData b = (QuestionData) rhs;

            return (a.Context.OrderIndex - b.Context.OrderIndex);
        }
    }

    public static void sort(List<QuestionData> list) {
        Collections.sort(list, new SortComparator());
    }

    public String htmlText(int score) {
        String css = "<style type=\"text/css\">.container {display: flex;align-items: center;}</style>";
        String title = AppUtils.dealHtmlText(Context.Stem);

        String optionsText = "";
        if (isChoiceType() && Context.Options != null) {
            for (OptionData optionData: Context.Options) {
                String no = AppUtils.OPTION_NO[optionData.OrderIndex-1] + ".";
                String text = AppUtils.dealHtmlText(optionData.OptionText);

                text = "<div class=\"container\">" + no + text + "</div>";
                optionsText = optionsText + "<br>" + text;
            }
        }

        String html;
        if (score == -1) {
            html = css + String.format("<div class=\"container\"><font size=\"4\">第%d题.&nbsp%s</font> </div><font size=\"3\">%s</font>",
                    Context.OrderIndex, title, optionsText);
        } else {
            String color = "#2ecc71";
            if (score == 0)
                color = "e74c3c";
            else if (score != 5)
                color = "f1c40f";

            html = css + String.format("<div class=\"container\"><font size=\"4\"><font color=\"%s\">[得分:%d分]</font>第%d题.&nbsp%s</font> </div><font size=\"3\">%s</font>",
                    color, score, Context.OrderIndex, title, optionsText);
        }

        return html;
    }

    public String htmlText() {
        return htmlText(-1);
    }
}
