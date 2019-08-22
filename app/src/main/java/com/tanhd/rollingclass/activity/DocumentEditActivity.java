package com.tanhd.rollingclass.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.db.Message;
import com.tanhd.rollingclass.fragments.ChatFragment;
import com.tanhd.rollingclass.fragments.FrameDialog;
import com.tanhd.rollingclass.fragments.InBoxFragment;
import com.tanhd.rollingclass.fragments.kowledge.KnowledgeControllerFragment;
import com.tanhd.rollingclass.views.TopbarView;

public class DocumentEditActivity extends AppCompatActivity implements KnowledgeControllerFragment.Callback {


    private String KnowledgeID;
    private String KnowledgePointName;
    private String ChapterName;
    private String SectionName;
    private String TeachingMaterialID;
    private String Remark;

    public static final String PARAM_KNOWLEDGE_ID = "KnowledgeID";
    public static final String PARAM_KNOWLEDGE_POINT_NAME = "KnowledgePointName";
    public static final String PARAM_CHAPTER_NAME = "ChapterName";
    public static final String PARAM_SECTION_NAME = "SectionName";
    public static final String PARAM_TEACHING_MATERIAL_ID = "TeachingMaterialID";
    public static final String PARAM_REMARK = "Remark";

    private static final String TAG = "DocumentEditActivity";
    private TopbarView mTopbarView;
    private View mBackButton;
    private KnowledgeControllerFragment mKnowledgeControllerFragment;

    public static final String PAGE_ID = "PAGE_ID";
    public static final int PAGE_ID_ADD_DOCUMENTS = 0;
    public static final int PAGE_ID_EDIT_DOCUMENTS = 1;
    private int mPageId;

    public static void startMe(Activity context, int pageId) {
        Intent intent = new Intent(context, DocumentEditActivity.class);
        intent.putExtra(PAGE_ID, pageId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPageId = getIntent().getIntExtra(PAGE_ID, PAGE_ID_ADD_DOCUMENTS);
        initParams();
        initViews();
        initFragment();
    }

    private void initParams() {

        KnowledgeID = getIntent().getStringExtra(DocumentEditActivity.PARAM_KNOWLEDGE_ID);
        ChapterName = getIntent().getStringExtra(DocumentEditActivity.PARAM_CHAPTER_NAME);
        SectionName = getIntent().getStringExtra(DocumentEditActivity.PARAM_SECTION_NAME);
        TeachingMaterialID = getIntent().getStringExtra(DocumentEditActivity.PARAM_TEACHING_MATERIAL_ID);
        Remark = getIntent().getStringExtra(DocumentEditActivity.PARAM_REMARK);
    }

    private void initViews() {

        setContentView(R.layout.activity_datas);
        mTopbarView = findViewById(R.id.topbar);
        mBackButton = findViewById(R.id.back_button);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        findViewById(R.id.inbox).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FrameDialog.show(getSupportFragmentManager(), InBoxFragment.newInstance(new InBoxFragment.SelectorListener() {
                    @Override
                    public void onMessageSelected(Message message) {
                        mTopbarView.refreshMessageCount();
                        if (message != null)
                            openMessage(message);
                    }
                }));
            }
        });
    }

    private void initFragment() {
        mKnowledgeControllerFragment = KnowledgeControllerFragment.newInstance(KnowledgeID, ChapterName, SectionName, TeachingMaterialID, Remark, this);
        getSupportFragmentManager().beginTransaction().replace(R.id.framelayout, mKnowledgeControllerFragment).commit();
    }

    private void openMessage(Message message) {
        FrameDialog.show(getSupportFragmentManager(), ChatFragment.newInstance(message.fromId));
    }

    @Override
    public void onBack() {
        finish();
    }
}
