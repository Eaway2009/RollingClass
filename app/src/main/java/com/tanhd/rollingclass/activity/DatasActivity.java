package com.tanhd.rollingclass.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.tanhd.rollingclass.MainActivity;
import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.base.BaseActivity;
import com.tanhd.rollingclass.db.Message;
import com.tanhd.rollingclass.fragments.ChatFragment;
import com.tanhd.rollingclass.fragments.FrameDialog;
import com.tanhd.rollingclass.fragments.InBoxFragment;
import com.tanhd.rollingclass.fragments.ShowPageFragment;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.views.TopbarView;

/**
 * 数据主要页
 */
public class DatasActivity extends BaseActivity {

    private static final String TAG = "DatasActivity";
    private TopbarView mTopbarView;
    private View mBackButton;
    private ShowPageFragment mShowPageFragment;

    public static final String PAGE_ID = "PAGE_ID";
    public static final String PAGE_TYPE = "PAGE_TYPE";
    public static final int PAGE_ID_DOCUMENTS = 0;
    public static final int PAGE_ID_RESOURCES = 1;
    public static final int PAGE_ID_STATISTICS = 2;
    private int mPageId;
    private boolean mIsStudentPage;

    public static void startMe(Activity context, int pageId) {
        Intent intent = new Intent(context, DatasActivity.class);
        intent.putExtra(PAGE_ID, pageId);
        context.startActivity(intent);
    }

    public static void startMe(Activity context, int pageId, boolean studentPage) {
        Intent intent = new Intent(context, DatasActivity.class);
        intent.putExtra(PAGE_ID, pageId);
        intent.putExtra(PAGE_TYPE, studentPage);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_datas);
        initParams();
        initViews();
        initFragment();
    }

    private void initParams() {
        mPageId = getIntent().getIntExtra(PAGE_ID, PAGE_ID_DOCUMENTS);
        mIsStudentPage = getIntent().getBooleanExtra(PAGE_TYPE, false);
    }

    private void initViews() {

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
        mTopbarView.setCallback(new TopbarView.Callback() {
            @Override
            public void connect_again() {
                if (ExternalParam.getInstance().getUserData() != null) {
//                    new ConnectMqttTask(ExternalParam.getInstance().getUserData()).execute();
                }
            }

            @Override
            public void showPage(int modulePageId) {
                setResult(RESULT_OK);
                finish();
                MainActivity.startMe(DatasActivity.this);
            }
        });
    }

    private void initFragment() {
        mShowPageFragment = ShowPageFragment.newInstance(mPageId, mIsStudentPage, new ShowPageFragment.PagesListener() {
            @Override
            public void onPageChange(int id) {

            }

            @Override
            public void onBack() {
                finish();
            }
        });
        getSupportFragmentManager().beginTransaction().replace(R.id.framelayout, mShowPageFragment).commit();
    }

    private void openMessage(Message message) {
        FrameDialog.show(getSupportFragmentManager(), ChatFragment.newInstance(message.fromId));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            mShowPageFragment.resetDocumentsData();
        }
    }
}
