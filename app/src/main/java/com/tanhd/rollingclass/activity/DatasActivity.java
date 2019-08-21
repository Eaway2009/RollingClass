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
import com.tanhd.rollingclass.fragments.ShowPageFragment;
import com.tanhd.rollingclass.views.TopbarView;

public class DatasActivity extends AppCompatActivity {

    private static final String TAG = "DatasActivity";
    private TopbarView mTopbarView;
    private View mBackButton;
    private ShowPageFragment mShowPageFragment;

    public static final String PAGE_ID = "PAGE_ID";
    public static final int PAGE_ID_DOCUMENTS = 0;
    public static final int PAGE_ID_RESOURCES = 1;
    public static final int PAGE_ID_STATISTICS = 2;
    private int mPageId;

    public static void startMe(Activity context, int pageId){
        Intent intent = new Intent(context, DatasActivity.class);
        intent.putExtra(PAGE_ID, pageId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPageId = getIntent().getIntExtra(PAGE_ID, PAGE_ID_DOCUMENTS);
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

        initFragment();
    }



    private void initFragment() {
        mShowPageFragment = ShowPageFragment.newInstance(PAGE_ID_DOCUMENTS, new ShowPageFragment.PagesListener() {
            @Override
            public void onPageChange(int id) {

            }
        });
        getSupportFragmentManager().beginTransaction().replace(R.id.framelayout, mShowPageFragment).commit();
    }

    private void openMessage(Message message) {
        FrameDialog.show(getSupportFragmentManager(), ChatFragment.newInstance(message.fromId));
    }
}
