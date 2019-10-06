package com.tanhd.rollingclass.views;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.activity.DocumentEditActivity;
import com.tanhd.rollingclass.activity.LearnCasesActivity;
import com.tanhd.rollingclass.db.KeyConstants;
import com.tanhd.rollingclass.server.RequestCallback;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.KnowledgeDetailMessage;
import com.tanhd.rollingclass.server.data.KnowledgeModel;
import com.tanhd.rollingclass.server.data.ResourceModel;
import com.tanhd.rollingclass.server.data.TeacherData;

import java.util.ArrayList;
import java.util.List;

/**
 * 资源适配器
 */
public class ResourceAdapter extends BaseAdapter {

    private static final String TAG = "ResourceAdapter";
    private boolean mCheckItem;
    private Activity mContext;
    private List<ResourceModel> mDataList = new ArrayList<>();

    private static DisplayImageOptions mImageOptions;

    static {
        mImageOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true).build();
    }

    public ResourceAdapter(Activity context, boolean checkItem) {
        mContext = context;
        mCheckItem = checkItem;
    }

    public void setData(List<ResourceModel> datas) {
        mDataList = datas;
        notifyDataSetChanged();
    }

    public List<ResourceModel> getDataList() {
        return mDataList;
    }

    public void clearData() {
        mDataList.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mDataList.size();
    }

    @Override
    public Object getItem(int position) {
        return mDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout view = (LinearLayout) convertView;
        if (view == null) {
            view = (LinearLayout) mContext.getLayoutInflater().inflate(R.layout.adapter_resource, parent, false);
        }

        final ResourceModel data = mDataList.get(position);

        TextView nameView = view.findViewById(R.id.tv_name);
        ImageView thumbView = view.findViewById(R.id.iv_thumb);
        final ImageView likeView = view.findViewById(R.id.iv_like);
        final LinearLayout collect = view.findViewById(R.id.ll_collect);
        if(data.level == KeyConstants.LevelType.PRIVATE_LEVEL){
            likeView.setImageResource(R.drawable.icon_like);
        }else {
            likeView.setImageResource(R.drawable.icon_unlike);
        }
        nameView.setText(data.name);
        if (data.isChecked && mCheckItem) {
            view.setBackgroundColor(mContext.getResources().getColor(R.color.button_blue_item_checked_transparent));
        } else {
            view.setBackgroundColor(mContext.getResources().getColor(R.color.transparent));
        }
        if(data.thumbs!=null&&data.thumbs.size()>0){
            Log.d(TAG, "okhttp: "+ScopeServer.getInstance().getResourceUrl() + data.thumbs.get(0));
            ImageLoader.getInstance().displayImage(ScopeServer.getInstance().getResourceUrl() + data.thumbs.get(0), thumbView, mImageOptions);
        } else if(data.resource_type == KeyConstants.ResourceType.IMAGE_TYPE){
            ImageLoader.getInstance().displayImage(ScopeServer.getInstance().getResourceUrl() + data.url, thumbView, mImageOptions);
        }
        collect.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                TeacherData teacherData = (TeacherData) ExternalParam.getInstance().getUserData().getUserData();
                ScopeServer.getInstance().StoreResource(teacherData.TeacherID, data.resource_id, new RequestCallback() {
                    @Override
                    public void onProgress(boolean b) {

                    }

                    @Override
                    public void onResponse(String body) {
                        if(data.level==KeyConstants.LevelType.PRIVATE_LEVEL){
                            likeView.setImageResource(R.drawable.icon_unlike);
                        }else {
                            likeView.setImageResource(R.drawable.icon_like);
                        }
                    }

                    @Override
                    public void onError(String code, String message) {

                    }
                });
            }
        });
        return view;
    }

    public void checkItem(int position) {
        for (ResourceModel model : mDataList) {
            model.isChecked = false;
        }
        ResourceModel checkItem = mDataList.get(position);
        checkItem.isChecked = true;
        notifyDataSetChanged();
    }
}