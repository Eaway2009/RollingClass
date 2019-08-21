package com.tanhd.rollingclass.db;

import java.io.Serializable;
import java.util.List;

public class ChaptersResponse implements Serializable {
    /**
     * 资产类别列表
     */
    public List<Category> categories;

    public static class Category extends MultiLevelModel<Chapter> {

        public Category(String categoryName, long categoryId) {
            this.categoryName = categoryName;
            this.categoryId = categoryId;
        }

        /**
         * 章名
         */
        public String categoryName;
        /**
         * 章的id
         */
        public long categoryId;
        /**
         * 节列表
         */
        public List<Chapter> chapterList;

        @Override
        public List<Chapter> getChildren() {
            return chapterList;
        }
    }

    public static class Chapter implements Serializable {
        public Chapter(String chapterName,long chapterId) {
            this.chapterId = chapterId;
            this.chapterName = chapterName;
        }

        /**
         * 节的Id
         */
        public long chapterId;
        /**
         * 节名
         */
        public String chapterName;

        public boolean isChecked;
    }
}
