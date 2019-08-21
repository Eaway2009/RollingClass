package com.tanhd.rollingclass.db;

import java.io.Serializable;
import java.util.List;

/**
 * 多级列表模型
 */

public abstract class MultiLevelModel<Child> implements Serializable {
    private List<Child> children;//子类

    public abstract List<Child> getChildren();

}
