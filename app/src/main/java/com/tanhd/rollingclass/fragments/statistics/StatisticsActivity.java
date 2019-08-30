package com.tanhd.rollingclass.fragments.statistics;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.db.Message;
import com.tanhd.rollingclass.fragments.ChatFragment;
import com.tanhd.rollingclass.fragments.CountClassFragment;
import com.tanhd.rollingclass.fragments.FrameDialog;
import com.tanhd.rollingclass.fragments.InBoxFragment;
import com.tanhd.rollingclass.views.TopbarView;

public class StatisticsActivity extends AppCompatActivity {

    private static final String TAG = "StatisticsActivity";
    private TopbarView mTopbarView;
    private View mBackButton;
    private CountClassFragment mCountClassFragment;

    public static final String PAGE_ID = "PAGE_ID";
    public static final int PAGE_ID_MICRO_COURSE = 0;
    public static final int PAGE_ID_QUESTION = 1;
    private int mPageId;

    public static void startMe(Activity context, int pageId){
        Intent intent = new Intent(context, StatisticsActivity.class);
        intent.putExtra(PAGE_ID, pageId);
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

    private void initParams(){
        mPageId = getIntent().getIntExtra(PAGE_ID, PAGE_ID_MICRO_COURSE);
    }

    private void initViews(){

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
        mCountClassFragment = CountClassFragment.newInstance(mPageId, new CountClassFragment.PagesListener() {
            @Override
            public void onPageChange(int id) {

            }

            @Override
            public void onBack() {
                finish();
            }
        });
        getSupportFragmentManager().beginTransaction().replace(R.id.framelayout, mCountClassFragment).commit();
    }

    private void openMessage(Message message) {
        FrameDialog.show(getSupportFragmentManager(), ChatFragment.newInstance(message.fromId));
    }
}