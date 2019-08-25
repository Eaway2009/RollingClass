package com.tanhd.rollingclass.server.data;

import java.io.Serializable;
import java.util.List;

public class InsertKnowledgeResponse extends BaseJsonClass {
    public String knowledge_id;
    public String knowledge_point_name;
    public String subject_name;
    public String school_id;
    public String teacher_id;
    public String teaching_material_id;
    public String chapter_id;
    public String chapter_name;
    public String section_name;
    public String section_id;
    public List<String> class_ids;
    public List<String> records;
    public int subject_code;
    public int class_process;
    public int class_before;
    public int class_after;
    public int status;
}
