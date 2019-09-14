package com.tanhd.rollingclass.views;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.ResourceModel;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

public class ThumbAdapter  extends BaseAdapter {

    private static final String TAG = "ThumbAdapter";
    private Activity mContext;
    private List<String> mDataList = new ArrayList<>();

    private static DisplayImageOptions mImageOptions;

    static {
        mImageOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true).build();
    }

    public ThumbAdapter(Activity context) {
        mContext = context;
    }

    public void setData(ArrayList<String> thumbs){
        mDataList = thumbs;
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
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView = (ImageView) convertView;
        if (imageView == null) {
            imageView = (ImageView) mContext.getLayoutInflater().inflate(R.layout.imageview, parent, false);
        }

        String thumbUrl = mDataList.get(position);
        ImageLoader.getInstance().displayImage(ScopeServer.getInstance().getResourceUrl() + thumbUrl, imageView, mImageOptions);

        return imageView;
    }
}
