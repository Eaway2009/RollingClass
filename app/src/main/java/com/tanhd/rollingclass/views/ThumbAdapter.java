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
import com.tanhd.rollingclass.utils.ToastUtil;

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

    private int mClickedIndex;

    public ThumbAdapter(Activity context) {
        mContext = context;
    }

    public void setData(ArrayList<String> thumbs){
        mDataList = thumbs;
        notifyDataSetChanged();
    }

    public void setClickedIndex(int clickedIndex){
        mClickedIndex = clickedIndex;
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
        LinearLayout contentView = (LinearLayout) convertView;
        if (contentView == null) {
            contentView = (LinearLayout) mContext.getLayoutInflater().inflate(R.layout.imageview, parent, false);
        }
        ImageView imageView = contentView.findViewById(R.id.image_view);
        String thumbUrl = mDataList.get(position);
        ImageLoader.getInstance().displayImage(ScopeServer.getInstance().getResourceUrl() + thumbUrl, imageView, mImageOptions);
        if(position == mClickedIndex){
            contentView.setBackgroundColor(mContext.getResources().getColor(R.color.button_orange));
        }else{
            contentView.setBackgroundColor(mContext.getResources().getColor(R.color.transparent));
        }
        return contentView;
    }
}
