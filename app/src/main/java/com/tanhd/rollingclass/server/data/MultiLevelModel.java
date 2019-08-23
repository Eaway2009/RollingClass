package com.tanhd.rollingclass.server.data;

import java.io.Serializable;
import java.util.List;

/**
 * 多级列表模型
 */

public interface MultiLevelModel<Child> {

    List<Child> getChildren();

}
