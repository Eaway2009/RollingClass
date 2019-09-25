package com.tanhd.rollingclass.db.model;

/**
 * EventBus事件
 * Created by YangShlai on 2019-09-24.
 */
public class EventTag {
    private String tag;
    //刷新学案列表
    public static final String REFRESH_CASE = "refresh_case";


    public EventTag(String tag){
        this.tag = tag;
    }


    public String getTag() {
        return tag;
    }
}
