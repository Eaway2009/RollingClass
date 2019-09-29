package com.tanhd.rollingclass.views.interfaces;

import android.view.View;

import com.tanhd.rollingclass.db.model.Course;

/**
 * Created by YangShlai on 2019-09-29.
 */
public interface SelectListener {
    void select(View view, int pos, Course.ChaptersBean.SectionsBean item);
}
