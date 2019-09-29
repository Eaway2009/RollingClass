package com.tanhd.rollingclass.fragments.pages;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.db.model.Course;
import com.tanhd.rollingclass.server.data.ChaptersResponse;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.KnowledgeModel;
import com.tanhd.rollingclass.server.data.StudentData;
import com.tanhd.rollingclass.server.data.TeacherData;
import com.tanhd.rollingclass.server.data.UserData;
import com.tanhd.rollingclass.utils.ToastUtil;
import com.tanhd.rollingclass.views.BaseMultiAdapter;
import com.tanhd.rollingclass.views.ChaptersAdapter;
import com.tanhd.rollingclass.views.ChaptersAdapter2;
import com.tanhd.rollingclass.views.MultiItemEntity;
import com.tanhd.rollingclass.views.interfaces.SelectListener;

import java.util.ArrayList;
import java.util.List;

/**
 * 左侧-年级类别等
 */
public class ChaptersFragment extends Fragment implements SelectListener {
    //private ExpandableListView mExpandableListView;
    private RecyclerView recyclerView;
    //白色背景
    private ChaptersAdapter whiteAdapter;
    //黑色背景
    private ChaptersAdapter2 blackAdapter;

    private BaseMultiAdapter mAdapter;
    private ChapterListener mListener;
    private UserData userData;
    private TeacherData teacherData;
    private StudentData studentData;
    private TextView teaching_material_name;
    private LinearLayout root;
    private boolean isWhiteBg; //是否白色背景

    public static ChaptersFragment newInstance(ChaptersFragment.ChapterListener listener) {
        ChaptersFragment page = new ChaptersFragment();
        Bundle args = new Bundle();
        page.setArguments(args);
        page.setListener(listener);
        return page;
    }

    public static ChaptersFragment newInstance(ChaptersFragment.ChapterListener listener,boolean isWhiteBg) {
        ChaptersFragment page = new ChaptersFragment();
        Bundle args = new Bundle();
        args.putBoolean("isWhiteBg",isWhiteBg);
        page.setArguments(args);
        page.setListener(listener);
        return page;
    }

    public void setListener(ChapterListener listener) {
        mListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.page_chapters, container, false);
        initParams();
        iniViews(view);
        refreshData();
        return view;
    }

    private void initParams() {
        Bundle args = getArguments();
        if (args != null){
            if (args.containsKey("isWhiteBg")){
                isWhiteBg = args.getBoolean("isWhiteBg");
            }

        }

    }

    private void iniViews(View view) {
        root = view.findViewById(R.id.root);
        recyclerView = view.findViewById(R.id.rv);
        teaching_material_name = view.findViewById(R.id.teaching_material_name);
        teaching_material_name.setSelected(true);

//        mAdapter = new ChaptersAdapter(getActivity());
        if (isWhiteBg){
            root.setBackgroundResource(R.color.transparent);
            teaching_material_name.setTextColor(getResources().getColor(R.color.black));
            whiteAdapter = new ChaptersAdapter(getActivity());
            whiteAdapter.setSelectListener(this);
            mAdapter = whiteAdapter;
        }else{
            root.setBackgroundResource(R.color.chapter_menu_background);
            teaching_material_name.setTextColor(getResources().getColor(R.color.chapter_menu_text));
            blackAdapter = new ChaptersAdapter2(getActivity());
            blackAdapter.setSelectListener(this);
            mAdapter = blackAdapter;
        }
        recyclerView.setAdapter(mAdapter);
    }

    public void refreshData() {
        new InitDataTask().execute();
    }

    private class InitDataTask extends AsyncTask<Void, Void, List<ChaptersResponse>> {

        @Override
        protected List<ChaptersResponse> doInBackground(Void... voids) {
            userData = ExternalParam.getInstance().getUserData();
            List<ChaptersResponse> responseList;
            if (userData.isTeacher()) {
                teacherData = (TeacherData) userData.getUserData();
                responseList = ScopeServer.getInstance().QueryTeachingMaterial(teacherData.SchoolID, teacherData.StudysectionCode, teacherData.SubjectCode);
            } else {
                studentData = (StudentData) userData.getUserData();
                responseList = ScopeServer.getInstance().QueryTeachingMaterialByGradeID(studentData.SchoolID, studentData.GradeID);
            }
            return responseList;
        }

        @Override
        protected void onPostExecute(List<ChaptersResponse> chapterList) {
            if (chapterList == null || chapterList.isEmpty()) {
                try {
                    ToastUtil.show(R.string.toast_no_chapters);
                } catch (Exception e) {
                }
                return;
            }

            //数据请求成功
            List<MultiItemEntity> list = new ArrayList<>();
            for (ChaptersResponse chaptersResponse : chapterList) { //1
                Course course = new Course();
                course.setGradeName(chaptersResponse.GradeName);

                if (chaptersResponse.Chapters != null) { //2
                    for (ChaptersResponse.Chapter chaptersBean : chaptersResponse.Chapters) {
                        Course.ChaptersBean cc = new Course.ChaptersBean();
                        cc.setChapterName(chaptersBean.ChapterName);
                        course.addSubItem(cc);

                        if (chaptersBean.Sections != null) {
                            for (ChaptersResponse.Section sectionsBean : chaptersBean.Sections) {
                                Course.ChaptersBean.SectionsBean ccs = new Course.ChaptersBean.SectionsBean();
                                ccs.setSectionName(sectionsBean.SectionName);

                                String schollId = teacherData != null ? teacherData.SchoolID : studentData.SchoolID;
                                String teacherId = teacherData != null ? teacherData.TeacherID : studentData.ClassID;
                                KnowledgeModel knowledgeModel = new KnowledgeModel(schollId, teacherId, chaptersBean.ChapterID, chaptersBean.ChapterName, sectionsBean.SectionID, sectionsBean.SectionName, chaptersResponse.SubjectCode, chaptersResponse.SubjectName, sectionsBean.TeachingMaterialID, "");
                                ccs.setKnowledgeModel(knowledgeModel);
                                cc.addSubItem(ccs);
                            }
                        }
                    }
                }
                list.add(course);
            }

            mAdapter.setDataList(list);
            mAdapter.expandAllOne(0, false, false);

            // 将第一章设置成默认展开,设置默认选中第一节
            ChaptersResponse chaptersResponse = chapterList.get(0);
            if (chaptersResponse != null && chaptersResponse.Chapters != null && chaptersResponse.Chapters.size() > 0) {
                //mExpandableListView.expandGroup(0);
                teaching_material_name.setText(chaptersResponse.TeachingMaterialName + "——" + chaptersResponse.StudySectionName + "-" + chaptersResponse.SubjectName);

                if (chaptersResponse.Chapters != null && !chaptersResponse.Chapters.isEmpty()) {
                    ChaptersResponse.Chapter chapter = chaptersResponse.Chapters.get(0);
                    ChaptersResponse.Section section = chapter.Sections.get(0);
                    if (mListener != null) {
                        if (teacherData != null) {
                            mListener.onTeacherCheckChapter(teacherData.SchoolID, teacherData.TeacherID, chapter.ChapterID, chapter.ChapterName, section.SectionID, section.SectionName, chaptersResponse.SubjectCode, chaptersResponse.SubjectName, section.TeachingMaterialID);
                        } else if (studentData != null) {
                            mListener.onTeacherCheckChapter(studentData.SchoolID, studentData.ClassID, chapter.ChapterID, chapter.ChapterName, section.SectionID, section.SectionName, chaptersResponse.SubjectCode, chaptersResponse.SubjectName, section.TeachingMaterialID);
                        }
                    }
                }
            }
        }
    }

    private List<ChaptersResponse.Chapter> getChapterDatas(List<ChaptersResponse> responseList) {
        List<ChaptersResponse.Chapter> lastChapters = new ArrayList<>();
        if (responseList != null) {
            for (int i = 0; i < responseList.size(); i++) {
                ChaptersResponse chaptersResponse = responseList.get(i);
                ChaptersResponse.TeachingMaterial teachingMaterial = new ChaptersResponse.TeachingMaterial(chaptersResponse.GradeCode,
                        chaptersResponse.GradeName, chaptersResponse.StudySectionCode, chaptersResponse.StudySectionName, chaptersResponse.SubjectCode,
                        chaptersResponse.SubjectName, chaptersResponse.TeachingMaterialCode, chaptersResponse.TeachingMaterialID, chaptersResponse.TeachingMaterialName);
                if (chaptersResponse.Chapters != null && chaptersResponse.Chapters.size() > 0) {
                    for (int j = 0; j < chaptersResponse.Chapters.size(); j++) {
                        ChaptersResponse.Chapter chapter = chaptersResponse.Chapters.get(j);
                        if (j == 0) {
                            chapter.teachingMaterial = teachingMaterial;
                        }
                        lastChapters.add(chapter);
                    }
                }
            }
        }
        return lastChapters;
    }

    @Override
    public void select(View view, int pos, Course.ChaptersBean.SectionsBean item) {
        KnowledgeModel kd = item.getKnowledgeModel();
        if (mListener != null && kd != null) {
            mListener.onTeacherCheckChapter(kd.school_id, kd.teacher_id, kd.chapter_id, kd.chapter_name, kd.section_id, kd.section_name, kd.subject_code, kd.subject_name, kd.teaching_material_id);
        }
    }

    public interface ChapterListener {
        void onTeacherCheckChapter(String school_id, String teacher_id, String chapter_id, String chapter_name, String section_id, String section_name, int subject_code, String subject_name, String teaching_material_id);

        void onStudentCheckChapter(String school_id, String class_id, String chapter_id, String chapter_name, String section_id, String section_name, int subject_code, String subject_name, String teaching_material_id);
    }

}
