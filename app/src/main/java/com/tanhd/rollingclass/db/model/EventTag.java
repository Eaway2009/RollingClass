package com.tanhd.rollingclass.db.model;

/**
 * EventBus事件
 * Created by YangShlai on 2019-09-24.
 */
public class EventTag {
    private String tag;
    //刷新学案列表
    public static final String REFRESH_CASE = "refresh_case";
    //显示举手提问按钮
    public static final String SHOW_HAND_BTN = "show_hand_btn";

    public EventTag(String tag){
        this.tag = tag;
    }


    public String getTag() {
        return tag;
    }
}
