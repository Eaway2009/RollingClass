package com.tanhd.rollingclass.db.model;

import com.tanhd.rollingclass.server.data.KnowledgeModel;
import com.tanhd.rollingclass.views.AbstractExpandableItem;
import com.tanhd.rollingclass.views.ChaptersAdapter2;
import com.tanhd.rollingclass.views.MultiItemEntity;

import java.util.List;

/**
 * Created by YangShlai on 2019-09-23.
 */
public class Course extends AbstractExpandableItem<Course.ChaptersBean> implements MultiItemEntity {
    /**
     * Chapters : [{"ChapterID":"5d60e26d1d41c8d5b14ee52d","ChapterName":"第一章","Sections":[{"SectionID":"5d60e26d1d41c8d5b14ee52e","SectionName":"第一节","TeachingMaterialID":"5d60e26d1d41c8d5b14ee52f"},{"SectionID":"5d60e26d1d41c8d5b14ee530","SectionName":"第二节","TeachingMaterialID":"5d60e26d1d41c8d5b14ee531"},{"SectionID":"5d60e26d1d41c8d5b14ee532","SectionName":"第三节","TeachingMaterialID":"5d60e26d1d41c8d5b14ee533"},{"SectionID":"5d60e26d1d41c8d5b14ee534","SectionName":"第四节","TeachingMaterialID":"5d60e26d1d41c8d5b14ee535"},{"SectionID":"5d60e26d1d41c8d5b14ee536","SectionName":"第五节","TeachingMaterialID":"5d60e26d1d41c8d5b14ee537"}]},{"ChapterID":"5d60e26d1d41c8d5b14ee538","ChapterName":"第二章","Sections":[{"SectionID":"5d60e26d1d41c8d5b14ee539","SectionName":"第一节","TeachingMaterialID":"5d60e26d1d41c8d5b14ee53a"},{"SectionID":"5d60e26d1d41c8d5b14ee53b","SectionName":"第二节","TeachingMaterialID":"5d60e26d1d41c8d5b14ee53c"},{"SectionID":"5d60e26d1d41c8d5b14ee53d","SectionName":"第三节","TeachingMaterialID":"5d60e26d1d41c8d5b14ee53e"},{"SectionID":"5d60e26d1d41c8d5b14ee53f","SectionName":"第四节","TeachingMaterialID":"5d60e26d1d41c8d5b14ee540"},{"SectionID":"5d60e26d1d41c8d5b14ee541","SectionName":"第五节","TeachingMaterialID":"5d60e26d1d41c8d5b14ee542"},{"SectionID":"5d60e26d1d41c8d5b14ee543","SectionName":"第六节","TeachingMaterialID":"5d60e26d1d41c8d5b14ee544"}]}]
     * GradeCode : 7
     * GradeName : 七年级下
     * StudySectionCode : 2
     * StudySectionName : 初中
     * SubjectCode : 2
     * SubjectName : 数学
     * TeachingMaterialCode : 1
     * TeachingMaterialID :
     * TeachingMaterialName : 人教版
     */

    private boolean isSelect;
    private int GradeCode;
    private String GradeName;
    private int StudySectionCode;
    private String StudySectionName;
    private int SubjectCode;
    private String SubjectName;
    private int TeachingMaterialCode;
    private String TeachingMaterialID;
    private String TeachingMaterialName;
    private List<ChaptersBean> Chapters;

    public int getGradeCode() {
        return GradeCode;
    }

    public void setGradeCode(int GradeCode) {
        this.GradeCode = GradeCode;
    }

    public String getGradeName() {
        return GradeName;
    }

    public void setGradeName(String GradeName) {
        this.GradeName = GradeName;
    }

    public int getStudySectionCode() {
        return StudySectionCode;
    }

    public void setStudySectionCode(int StudySectionCode) {
        this.StudySectionCode = StudySectionCode;
    }

    public String getStudySectionName() {
        return StudySectionName;
    }

    public void setStudySectionName(String StudySectionName) {
        this.StudySectionName = StudySectionName;
    }

    public int getSubjectCode() {
        return SubjectCode;
    }

    public void setSubjectCode(int SubjectCode) {
        this.SubjectCode = SubjectCode;
    }

    public String getSubjectName() {
        return SubjectName;
    }

    public void setSubjectName(String SubjectName) {
        this.SubjectName = SubjectName;
    }

    public int getTeachingMaterialCode() {
        return TeachingMaterialCode;
    }

    public void setTeachingMaterialCode(int TeachingMaterialCode) {
        this.TeachingMaterialCode = TeachingMaterialCode;
    }

    public String getTeachingMaterialID() {
        return TeachingMaterialID;
    }

    public void setTeachingMaterialID(String TeachingMaterialID) {
        this.TeachingMaterialID = TeachingMaterialID;
    }

    public String getTeachingMaterialName() {
        return TeachingMaterialName;
    }

    public void setTeachingMaterialName(String TeachingMaterialName) {
        this.TeachingMaterialName = TeachingMaterialName;
    }

    public List<ChaptersBean> getChapters() {
        return Chapters;
    }

    public void setChapters(List<ChaptersBean> Chapters) {
        this.Chapters = Chapters;
    }

    @Override
    public int getLevel() {
        return ChaptersAdapter2.TYPE_LEVEL_CLASS;
    }

    @Override
    public int getItemType() {
        return ChaptersAdapter2.TYPE_LEVEL_CLASS;
    }

    public static class ChaptersBean extends AbstractExpandableItem<ChaptersBean.SectionsBean> implements MultiItemEntity{
        /**
         * ChapterID : 5d60e26d1d41c8d5b14ee52d
         * ChapterName : 第一章
         * Sections : [{"SectionID":"5d60e26d1d41c8d5b14ee52e","SectionName":"第一节","TeachingMaterialID":"5d60e26d1d41c8d5b14ee52f"},{"SectionID":"5d60e26d1d41c8d5b14ee530","SectionName":"第二节","TeachingMaterialID":"5d60e26d1d41c8d5b14ee531"},{"SectionID":"5d60e26d1d41c8d5b14ee532","SectionName":"第三节","TeachingMaterialID":"5d60e26d1d41c8d5b14ee533"},{"SectionID":"5d60e26d1d41c8d5b14ee534","SectionName":"第四节","TeachingMaterialID":"5d60e26d1d41c8d5b14ee535"},{"SectionID":"5d60e26d1d41c8d5b14ee536","SectionName":"第五节","TeachingMaterialID":"5d60e26d1d41c8d5b14ee537"}]
         */

        private boolean isSelect;
        private String ChapterID;
        private String ChapterName;
        private List<SectionsBean> Sections;

        public boolean isSelect() {
            return isSelect;
        }

        public void setSelect(boolean select) {
            isSelect = select;
        }

        public String getChapterID() {
            return ChapterID;
        }

        public void setChapterID(String ChapterID) {
            this.ChapterID = ChapterID;
        }

        public String getChapterName() {
            return ChapterName;
        }

        public void setChapterName(String ChapterName) {
            this.ChapterName = ChapterName;
        }

        public List<SectionsBean> getSections() {
            return Sections;
        }

        public void setSections(List<SectionsBean> Sections) {
            this.Sections = Sections;
        }

        @Override
        public int getLevel() {
            return ChaptersAdapter2.TYPE_LEVEL_CHAPTER;
        }

        @Override
        public int getItemType() {
            return ChaptersAdapter2.TYPE_LEVEL_CHAPTER;
        }

        public static class SectionsBean implements MultiItemEntity{
            /**
             * SectionID : 5d60e26d1d41c8d5b14ee52e
             * SectionName : 第一节
             * TeachingMaterialID : 5d60e26d1d41c8d5b14ee52f
             */
            private boolean isSelect;
            private String SectionID;
            private String SectionName;
            private String TeachingMaterialID;
            private KnowledgeModel knowledgeModel;

            public KnowledgeModel getKnowledgeModel() {
                return knowledgeModel;
            }

            public void setKnowledgeModel(KnowledgeModel knowledgeModel) {
                this.knowledgeModel = knowledgeModel;
            }

            public boolean isSelect() {
                return isSelect;
            }

            public void setSelect(boolean select) {
                isSelect = select;
            }

            public String getSectionID() {
                return SectionID;
            }

            public void setSectionID(String SectionID) {
                this.SectionID = SectionID;
            }

            public String getSectionName() {
                return SectionName;
            }

            public void setSectionName(String SectionName) {
                this.SectionName = SectionName;
            }

            public String getTeachingMaterialID() {
                return TeachingMaterialID;
            }

            public void setTeachingMaterialID(String TeachingMaterialID) {
                this.TeachingMaterialID = TeachingMaterialID;
            }

            @Override
            public int getItemType() {
                return ChaptersAdapter2.TYPE_LEVEL_SECTION;
            }
        }
    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }
}
