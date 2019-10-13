package com.tanhd.rollingclass.server.data;

import com.tanhd.rollingclass.db.KeyConstants;
import com.tanhd.rollingclass.utils.AppUtils;

import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AnswerModel extends ResourceBaseModel {
    public long create_time;
    public String lesson_sample_id;
    public String lesson_sample_name;
    public String QuestionCoordinate;
    public String question_id;
    public transient String resource_id;
    public String resource_name;
    public String teaching_material_id;
    public String QuestionName;
    public String Remark;
    public String teacher_id;
    public String teacher_name;
    public int context_type;
    public long update_time;
    public int QuestionType;
    public ContextData context;
    public boolean isChecked;
    public int answer_status = KeyConstants.AnswerStatus.NO_ANSWER;
    public int resource_type = KeyConstants.ResourceType.QUESTION_TYPE;

    public String myAnswer;

    @Override
    protected void onDealListField(Object object, Field field, JSONObject json, String key) {
        super.onDealListField(object, field, json, key);
        if (key.equals("context")) {
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
        if (context.Options != null && !context.Options.isEmpty())
            return true;
        return false;
    }

    private static class SortComparator implements Comparator {
        @Override
        public int compare(Object lhs, Object rhs) {
            QuestionModel a = (QuestionModel) lhs;
            QuestionModel b = (QuestionModel) rhs;

            return (a.context.OrderIndex - b.context.OrderIndex);
        }
    }

    public static void sort(List<AnswerModel> list) {
        Collections.sort(list, new AnswerModel.SortComparator());
    }

    public String htmlText(int score) {
        String css = "<style type=\"text/css\">.container {display: flex;align-items: center;}</style>";
        String title = AppUtils.dealHtmlText(context.Stem);

        String optionsText = "";
        if (isChoiceType() && context.Options != null) {
            for (OptionData optionData: context.Options) {
                String no = AppUtils.OPTION_NO[optionData.OrderIndex-1] + ".";
                String text = AppUtils.dealHtmlText(optionData.OptionText);

                text = "<div class=\"container\">" + no + text + "</div>";
                optionsText = optionsText + "<br>" + text;
            }
        }

        String html;
        if (score == -1) {
            html = css + String.format("<div class=\"container\"><font size=\"4\">第%d题.&nbsp%s</font> </div><font size=\"3\">%s</font>",
                    context.OrderIndex, title, optionsText);
        } else {
            String color = "#2ecc71";
            if (score == 0)
                color = "e74c3c";
            else if (score != 5)
                color = "f1c40f";

            html = css + String.format("<div class=\"container\"><font size=\"4\"><font color=\"%s\">[得分:%d分]</font>第%d题.&nbsp%s</font> </div><font size=\"3\">%s</font>",
                    color, score, context.OrderIndex, title, optionsText);
        }

        return html;
    }

    public String htmlText() {
        return htmlText(-1);
    }
}
