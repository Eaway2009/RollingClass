package com.tanhd.rollingclass.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.server.data.KnowledgeDetailMessage;
import com.tanhd.rollingclass.server.data.KnowledgeModel;
import com.tanhd.rollingclass.db.Message;
import com.tanhd.rollingclass.fragments.ChatFragment;
import com.tanhd.rollingclass.fragments.FrameDialog;
import com.tanhd.rollingclass.fragments.InBoxFragment;
import com.tanhd.rollingclass.fragments.kowledge.KnowledgeControllerFragment;
import com.tanhd.rollingclass.views.TopbarView;

public class DocumentEditActivity extends AppCompatActivity implements KnowledgeControllerFragment.Callback {

    private static final String TAG = "DocumentEditActivity";

    public static final String PARAM_EDIT_KNOWLEDGE = "PARAM_EDIT_KNOWLEDGE";
    public static final String PARAM_TEACHING_MATERIAL_DATA = "PARAM_TEACHING_MATERIAL_DATA";
    public static final String PARAM_KNOWLEDGE_DETAIL_DATA = "PARAM_KNOWLEDGE_DETAIL_DATA";
    public static final String PAGE_ID = "PAGE_ID";
    public static final String NEED_CHECK_ITEM = "NEED_CHECK_ITEM";
    public static final int PAGE_ID_ADD_DOCUMENTS = 0;
    public static final int PAGE_ID_EDIT_DOCUMENTS = 1;

    private KnowledgeModel mKnowledgeModel;
    private int mPageId;

    private TopbarView mTopbarView;
    private View mBackButton;
    private KnowledgeControllerFragment mKnowledgeControllerFragment;
    private KnowledgeDetailMessage mKnowledgeDetailMessage;

    public static void startMe(Fragment context, int pageId, KnowledgeModel knowledgeModel) {
        Intent intent = new Intent();
        intent.setClass(context.getActivity(), DocumentEditActivity.class);
        intent.putExtra(PARAM_TEACHING_MATERIAL_DATA, knowledgeModel);
        intent.putExtra(PAGE_ID, pageId);
        context.startActivityForResult(intent, 0);
    }

    public static void startMe(Fragment context, int pageId, KnowledgeModel knowledgeModel, KnowledgeDetailMessage knowledgeDetailMessage) {
        Intent intent = new Intent();
        intent.setClass(context.getActivity(), DocumentEditActivity.class);
        intent.putExtra(PARAM_TEACHING_MATERIAL_DATA, knowledgeModel);
        intent.putExtra(PARAM_KNOWLEDGE_DETAIL_DATA, knowledgeDetailMessage);
        intent.putExtra(PAGE_ID, pageId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initParams();
        initViews();
        initFragment();
    }

    private void initParams() {
        mPageId = getIntent().getIntExtra(PAGE_ID, PAGE_ID_ADD_DOCUMENTS);
        mKnowledgeModel = (KnowledgeModel) getIntent().getSerializableExtra(PARAM_TEACHING_MATERIAL_DATA);
        if (mPageId == PAGE_ID_EDIT_DOCUMENTS) {
            mKnowledgeDetailMessage = (KnowledgeDetailMessage) getIntent().getSerializableExtra(PARAM_KNOWLEDGE_DETAIL_DATA);
        }
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
        if (mKnowledgeDetailMessage != null) {
            mKnowledgeControllerFragment = KnowledgeControllerFragment.newInstance(mKnowledgeModel, mKnowledgeDetailMessage, this);
        } else {
            mKnowledgeControllerFragment = KnowledgeControllerFragment.newInstance(mKnowledgeModel, this);
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.framelayout, mKnowledgeControllerFragment).commit();
    }

    private void openMessage(Message message) {
        FrameDialog.show(getSupportFragmentManager(), ChatFragment.newInstance(message.fromId));
    }

    @Override
    public void onBack() {
        backToList();
    }

    @Override
    public void onBackPressed() {
        backToList();
    }

    private void backToList() {
        setResult(RESULT_OK);
        finish();
    }
}
