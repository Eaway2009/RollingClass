package com.tanhd.rollingclass.views;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import com.tanhd.rollingclass.R;
import java.util.ArrayList;
import java.util.List;

public class PointPopupWindow {

    public final static int TYPE_SETTING_1 = 101;
    public final static int TYPE_SETTING_2 = 102;
    private PopupWindow popupWindow;
    private PopupClickCallBack mListener;
    private Context mContext;

    public final static int ITEM_ANSWER = 1001;
    public final static int ITEM_EXRCISE = 1002;
    public final static int ITEM_LOCK = 1003;
    public final static int ITEM_MUTE = 1004;

    public void create(Context context, int type) {
        mContext = context;
        View inflate= LayoutInflater.from(context).inflate(R.layout.popwin_point_view, null, false);
        initView(inflate, type);
        popupWindow = new PopupWindow(inflate, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
//        popupWindow.setBackgroundDrawable(context.getResources().getDrawable(R.mipmap.a));
        popupWindow.setFocusable(false);
        popupWindow.setOutsideTouchable(true);
    }

    public void setmListener(PopupClickCallBack listener) {
        this.mListener = listener;
    }

    public void showPopup(View view) {
        int offsetY = (int) (view.getHeight() * 3.1f);
        int offsetX = view.getWidth();
        Log.e("mmc", "offsetX:" + offsetX + "   offsetY:" + offsetY);
        popupWindow.showAsDropDown(view, -offsetX +30, -offsetY, Gravity.START);
    }

    public void dimissPopup() {
        if (popupWindow != null) {
            popupWindow.dismiss();
        }
    }

    private void initView(View inflate, final int type) {
        LinearLayout content = inflate.findViewById(R.id.ll_popwin_content);

        List<PointType> list = new ArrayList<>();
        if (type == TYPE_SETTING_1) {
            list.add(new PointType(ITEM_EXRCISE, R.drawable.icon_exercise, R.string.test_study));
            list.add(new PointType(ITEM_ANSWER, R.drawable.icon_answer, R.string.answer_question));
        } else if (type == TYPE_SETTING_2) {
            list.add(new PointType(ITEM_LOCK, R.drawable.icon_lock_enable, R.string.lock_enable));
            list.add(new PointType(ITEM_MUTE, R.drawable.icon_mute_enable, R.string.mute_enable));
        }

        if (list != null) {
            content.removeAllViews();
            for (int i = 0; i < list.size(); i++) {
                View linkLay = LayoutInflater.from(mContext).inflate(R.layout.view_popwin_item, null);
                content.addView(linkLay);
                final TextView titleText = (TextView) linkLay.findViewById(R.id.tv_setting);
                final ImageView imageView = (ImageView) linkLay.findViewById(R.id.iv_setting);
                LinearLayout layout = linkLay.findViewById(R.id.ll_item);
                final PointType obj = list.get(i);
                imageView.setBackgroundResource(obj.imageId);
                imageView.setTag(false);
                titleText.setText(mContext.getString(obj.txtId));
                layout.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        boolean isClick = (boolean)imageView.getTag();

                        if (obj.type == ITEM_LOCK) {
                            if (isClick) {
                                imageView.setBackgroundResource(R.drawable.icon_lock_enable);
                                titleText.setText(mContext.getString(R.string.lock_enable));
                            } else {
                                imageView.setBackgroundResource(R.drawable.icon_lock_unable);
                                titleText.setText(mContext.getString(R.string.lock_unable));
                            }
                        } else if (obj.type == ITEM_MUTE) {
                            if ((boolean)imageView.getTag()) {
                                imageView.setBackgroundResource(R.drawable.icon_mute_enable);
                                titleText.setText(mContext.getString(R.string.mute_enable));
                            } else {
                                imageView.setBackgroundResource(R.drawable.icon_mute_unable);
                                titleText.setText(mContext.getString(R.string.mute_unable));
                            }
                        }
                        imageView.setTag(!isClick);
                        if (mListener != null) {
                            mListener.onClick(obj.type, !isClick);
                        }
                    }
                });
            }
        }
    }

    class PointType {
        public int type;
        public int imageId;
        public int txtId;

        public PointType (int type, int imageId, int txtId) {
            this.txtId = txtId;
            this.imageId = imageId;
            this.type = type;
        }
    }

    public interface PopupClickCallBack{
        void onClick(int type, boolean isClick);
    }

}
