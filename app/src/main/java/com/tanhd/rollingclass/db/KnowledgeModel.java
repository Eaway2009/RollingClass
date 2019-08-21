package com.tanhd.rollingclass.db;

import com.tanhd.rollingclass.server.data.BaseJsonClass;

public class KnowledgeModel extends BaseJsonClass {
    /**
     * 章的Id
     */
    String chapter_id;
    /**
     * 章的名称
     */
    String chapter_name;
    /**
     * 知识点
     */
    String knowledge_point_name;

    String remark;
    /**
     * 节的id
     */
    String section_id;
    /**
     * 节的名称
     */
    String section_name;
    /**
     * 科目Id
     */
    int subject_code;
    /**
     * 科目名
     */
    String subject_name;
    /**
     * 教案id
     */
    String teaching_material_id;
}