package com.tanhd.rollingclass.views;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ViewHolder {

    private static final String TAG = ViewHolder.class.getSimpleName();

    private SparseArray<View> mSparse;
    private View mConvertView;

    public View getConvertView() {
        return mConvertView;
    }

    public ViewHolder(View convertView) {
        this.mConvertView = convertView;
        mSparse = new SparseArray<View>();
    }

    public static ViewHolder getViewHolder(Context context, int position, View convertView, ViewGroup parent, int layoutId) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(layoutId, null);
            ViewHolder viewHoloder = new ViewHolder(convertView);
            convertView.setTag(viewHoloder);
            return viewHoloder;
        }
        return (ViewHolder) convertView.getTag();
    }

    public <T extends View> T getView(int viewId) {

        View view = mSparse.get(viewId);
        if (view == null) {
            view = mConvertView.findViewById(viewId);
            mSparse.put(viewId, view);
        }

        return (T) view;
    }

}

