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
import com.tanhd.rollingclass.fragments.pages.LearnCasesFragment;
import com.tanhd.rollingclass.views.TopbarView;

/**
 * 学案界面
 */
public class LearnCasesActivity extends AppCompatActivity {

    private LearnCasesFragment mLearnCasesActivity;

    private TopbarView mTopbarView;
    private View mBackButton;

    public static void startMe(Activity context) {
        Intent intent = new Intent(context, LearnCasesActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        initParams();
        initViews();
        initFragment();
    }

    private void initFragment() {
        mLearnCasesActivity = LearnCasesFragment.newInstance(new LearnCasesFragment.PagesListener() {
            @Override
            public void onPageChange(int id) {

            }

            @Override
            public void onBack() {
                finish();
            }
        });
        getSupportFragmentManager().beginTransaction().replace(R.id.framelayout, mLearnCasesActivity).commit();
    }

    private void initViews() {

        setContentView(R.layout.activity_learn_cases);
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

    private void openMessage(Message message) {
        FrameDialog.show(getSupportFragmentManager(), ChatFragment.newInstance(message.fromId));
    }
}