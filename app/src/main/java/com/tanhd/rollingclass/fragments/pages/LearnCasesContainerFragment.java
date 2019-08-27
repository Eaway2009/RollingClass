package com.tanhd.rollingclass.fragments.pages;

import static com.tanhd.rollingclass.views.PointPopupWindow.ITEM_ANSWER;
import static com.tanhd.rollingclass.views.PointPopupWindow.ITEM_EXRCISE;
import static com.tanhd.rollingclass.views.PointPopupWindow.ITEM_LOCK;
import static com.tanhd.rollingclass.views.PointPopupWindow.ITEM_MUTE;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.views.PointPopupWindow;
import com.tanhd.rollingclass.views.PointPopupWindow.PopupClickCallBack;

public class LearnCasesContainerFragment extends Fragment implements OnClickListener {

    PagesListener mListener;
    private ImageView mIvSetting1;
    private ImageView mIvSetting2;
    private ImageView mIvFullScreen;
    private RelativeLayout mRlSettingLayout;
    private boolean mIsFullScreen;

    private PointPopupWindow mPopupWindow1;
    private PointPopupWindow mPopupWindow2;
    public static LearnCasesContainerFragment newInstance(int typeId, PagesListener listener) {
        Bundle args = new Bundle();
        args.putInt("typeId", typeId);
        LearnCasesContainerFragment page = new LearnCasesContainerFragment();
        page.setArguments(args);
        page.setListener(listener);
        return page;
    }

    private void setListener(PagesListener listener) {
        mListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.page_learncases_container, container, false);
        iniViews(view);
        return view;
    }

    private void iniViews(View view) {
        mIvSetting1 = view.findViewById(R.id.iv_set1);
        mIvSetting2 = view.findViewById(R.id.iv_set2);
        mIvFullScreen = view.findViewById(R.id.iv_full_screen);
        mRlSettingLayout = view.findViewById(R.id.rl_setting);
        mIvSetting1.setOnClickListener(this);
        mIvSetting2.setOnClickListener(this);
        mIvFullScreen.setOnClickListener(this);

        mPopupWindow1 = new PointPopupWindow();
        mPopupWindow1.create(getActivity(), PointPopupWindow.TYPE_SETTING_1);
        mPopupWindow1.setmListener(clickCallBack);
        mPopupWindow2 = new PointPopupWindow();
        mPopupWindow2.create(getActivity(), PointPopupWindow.TYPE_SETTING_2);
        mPopupWindow2.setmListener(clickCallBack);
    }

    PopupClickCallBack clickCallBack = new PopupClickCallBack() {
        @Override
        public void onClick(int type, boolean isClick) {
            switch (type) {
                case ITEM_ANSWER:
                    break;
                case ITEM_EXRCISE:
                    break;
                case ITEM_LOCK:
                    break;
                case ITEM_MUTE:
                    break;
            }
            mPopupWindow1.dimissPopup();
            mPopupWindow2.dimissPopup();
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_set1:
                mPopupWindow2.dimissPopup();
                mPopupWindow1.showPopup(v);
                break;
            case R.id.iv_set2:
                mPopupWindow1.dimissPopup();
                mPopupWindow2.showPopup(v);
                break;
            case R.id.iv_full_screen:
                if (mListener != null) {
                    mIsFullScreen = !mIsFullScreen;
                    mListener.onFullScreen(mIsFullScreen);
                    if (mIsFullScreen) {
                        mIvFullScreen.setBackgroundResource(R.mipmap.icon_fullscreen_unable);
                    } else {
                        mIvFullScreen.setBackgroundResource(R.mipmap.icon_fullscreen_enable);
                    }
                    mRlSettingLayout.setVisibility(mIsFullScreen == true ? View.GONE : View.VISIBLE);
                    mPopupWindow1.dimissPopup();
                    mPopupWindow2.dimissPopup();
                }
                break;
        }
    }

    public interface PagesListener {
        void onFullScreen(boolean isFull);
    }

}
