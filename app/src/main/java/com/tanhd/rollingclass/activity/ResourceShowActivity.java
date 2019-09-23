package com.tanhd.rollingclass.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.base.BaseActivity;
import com.tanhd.rollingclass.db.KeyConstants;
import com.tanhd.rollingclass.fragments.ImageShowFragment;
import com.tanhd.rollingclass.fragments.ShowDocumentFragment;
import com.tanhd.rollingclass.fragments.ShowPptFragment;
import com.tanhd.rollingclass.fragments.VideoPlayerFragment;
import com.tanhd.rollingclass.server.data.ResourceModel;

import java.io.Serializable;

public class ResourceShowActivity extends BaseActivity {

    private static final String PAGE_RESOURCE = "PAGE_RESOURCE";
    private ResourceModel mResourceModel;
    private Fragment mFragment;

    public static void startMe(Activity context, ResourceModel resourceModel) {
        Intent intent = new Intent(context, ResourceShowActivity.class);
        intent.putExtra(PAGE_RESOURCE, resourceModel);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resource);
        initParams();
        initFragment();
    }

    private void initFragment() {
        if (mResourceModel != null) {
            switch (mResourceModel.resource_type) {
                case KeyConstants.ResourceType.PPT_TYPE:
                    mFragment = ShowPptFragment.newInstance(ResourceShowActivity.this, mResourceModel.pdf_url, mResourceModel.thumbs, KeyConstants.SYNC_MODE.NONE);
                    break;
                case KeyConstants.ResourceType.WORD_TYPE:
                    mFragment = ShowDocumentFragment.newInstance(ResourceShowActivity.this, mResourceModel.pdf_url, KeyConstants.SYNC_MODE.NONE);
                    break;
                case KeyConstants.ResourceType.IMAGE_TYPE:
                    mFragment = ImageShowFragment.newInstance(mResourceModel.url);
                    break;
                case KeyConstants.ResourceType.VIDEO_TYPE:
                    mFragment = VideoPlayerFragment.newInstance(mResourceModel.resource_id, mResourceModel.url);
                    break;
            }
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.framelayout, mFragment).commit();
    }

    private void initParams() {
        mResourceModel = (ResourceModel) getIntent().getSerializableExtra(PAGE_RESOURCE);
    }
}
