package com.tanhd.rollingclass.server.data;

import com.tanhd.rollingclass.server.data.BaseJsonClass;

public class KnowledgeModel extends BaseJsonClass {

    /**
     * 学校id
     */
    public String school_id;
    /**
     * 学校id
     */
    public String teacher_id;
    /**
     * 章的Id
     */
    public String chapter_id;
    /**
     * 章的名称
     */
    public String chapter_name;
    /**
     * 知识点
     */
    public String knowledge_point_name;

    /**
     * 节的id
     */
    public String section_id;
    /**
     * 节的名称
     */
    public String section_name;
    /**
     * 科目Id
     */
    public int subject_code;
    /**
     * 科目名
     */
    public String subject_name;
    /**
     * 教案id
     */
    public String teaching_material_id;
    /**
     * 教案id
     */
    public String classID;

    public KnowledgeModel(String school_id, String teacher_id, String chapter_id, String chapter_name, String section_id, String section_name, int subject_code, String subject_name, String teaching_material_id, String classID) {
        this.school_id = school_id;
        this.teacher_id = teacher_id;
        this.chapter_id = chapter_id;
        this.chapter_name = chapter_name;
        this.section_id = section_id;
        this.section_name = section_name;
        this.subject_code = subject_code;
        this.subject_name = subject_name;
        this.teaching_material_id = teaching_material_id;
        this.classID = classID;
    }
}