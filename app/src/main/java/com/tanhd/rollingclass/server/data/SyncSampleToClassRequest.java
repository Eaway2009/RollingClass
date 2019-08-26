package com.tanhd.rollingclass.server.data;

import java.util.List;

public class SyncSampleToClassRequest extends BaseJsonClass {
    public List<String> class_after_task;
    public List<String> class_before_task;
    public List<String> class_process_task;
    public int cur_status;
}
