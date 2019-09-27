package com.tanhd.rollingclass.utils;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.views.popmenu.DropPopMenu;
import com.tanhd.rollingclass.views.popmenu.MenuItem;

import java.util.ArrayList;

public class PopMenu {
    public static interface OnItemClickListener {
        void onItemClick(AdapterView<?> adapterView, View view, int position, long id, MenuItem menuItem);
    }

    private final View mView;
    private ArrayList<MenuItem> mMenuItems = new ArrayList<>();
    private OnItemClickListener mListener;

    public PopMenu(View v) {
        mView = v;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public void addItem(int iconResId, int itemId, String itemTitle) {
        mMenuItems.add(new MenuItem(iconResId, itemId, itemTitle));
    }

    public void show() {
        final Context context = mView.getContext();
        DropPopMenu dropPopMenu = new DropPopMenu(context);
        dropPopMenu.setTriangleIndicatorViewColor(Color.parseColor("#535353"));
        dropPopMenu.setBackgroundResource(R.drawable.bg_drop_pop_menu_white_shap);
        dropPopMenu.setItemTextColor(Color.WHITE);
        dropPopMenu.setIsShowIcon(true);

        dropPopMenu.setOnItemClickListener(new DropPopMenu.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id, MenuItem menuItem) {
                if (mListener == null)
                    ToastUtil.show(R.string.toast_ing);
                else
                    mListener.onItemClick(adapterView, view, position, id, menuItem);
            }
        });
        dropPopMenu.setMenuList(mMenuItems);
        dropPopMenu.show(mView);
    }
}
