package com.tanhd.rollingclass.views;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.tanhd.library.mqtthttp.MyMqttService;
import com.tanhd.library.mqtthttp.PushMessage;
import com.tanhd.rollingclass.MainActivity;
import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.db.Database;
import com.tanhd.rollingclass.fragments.FrameDialog;
import com.tanhd.rollingclass.fragments.NetWorkTestFragment;
import com.tanhd.rollingclass.fragments.ServerTesterFragment;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.UserData;
import com.tanhd.rollingclass.utils.PopMenu;
import com.tanhd.rollingclass.utils.ToastUtil;
import com.tanhd.rollingclass.utils.langeuage.LanguageType;
import com.tanhd.rollingclass.utils.langeuage.MultiLanguageUtil;
import com.tanhd.rollingclass.views.popmenu.MenuItem;

import java.util.Calendar;
import java.util.List;


public class TopbarView extends CardView {
    private TextView mDateView;
    private TextView mWeekView;
    private CornerImageView mMessageView;
    private PopMenu mPopMenu;
    private Callback mCallback;
    private TextView mUserNameView;
    private int selectedLanguage = LanguageType.LANGUAGE_CHINESE_SIMPLIFIED;
    private AlertDialog mLogoutDialog;

    public TopbarView(Context context) {
        super(context);
    }

    public TopbarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TopbarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mDateView = findViewById(R.id.datetext);
        mWeekView = findViewById(R.id.weektext);
        mMessageView = findViewById(R.id.count_text);
        mUserNameView = findViewById(R.id.username);

        //中英文切换
        findViewById(R.id.iv_en).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( MultiLanguageUtil.getInstance().getLanguageType() == LanguageType.LANGUAGE_EN){
                    return;
                }

                selectedLanguage = LanguageType.LANGUAGE_EN;
                MultiLanguageUtil.getInstance().updateLanguage(selectedLanguage);
                Intent intent = new Intent(getContext(),MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                getContext().startActivity(intent);
                ToastUtil.show(R.string.toast_change_ok);
            }
        });
        findViewById(R.id.iv_cn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( MultiLanguageUtil.getInstance().getLanguageType() == LanguageType.LANGUAGE_CHINESE_SIMPLIFIED){
                    return;
                }
                selectedLanguage = LanguageType.LANGUAGE_CHINESE_SIMPLIFIED;
                MultiLanguageUtil.getInstance().updateLanguage(selectedLanguage);
                Intent intent = new Intent(getContext(),MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                getContext().startActivity(intent);
                ToastUtil.show(R.string.toast_change_ok);
            }
        });

        Calendar calendar = Calendar.getInstance();
        String text = String.format("%04d.%02d.%02d", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
        mDateView.setText(text);

        String[] week = new String[] {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
        if (MultiLanguageUtil.getInstance().getLanguageType() == LanguageType.LANGUAGE_EN){ //英文
            week = new String[]{"Sunday","Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        }

        int weekIndex = calendar.get(Calendar.DAY_OF_WEEK ) - 1;
        text = week[weekIndex];
        mWeekView.setText(text);

        refreshMessageCount();
        UserData userData = ExternalParam.getInstance().getUserData();
        if (userData!=null){
            mUserNameView.setText(userData.getOwnerName());
        }

        findViewById(R.id.username).setOnClickListener(onClickListener);
        findViewById(R.id.profile_image).setOnClickListener(onClickListener);
        findViewById(R.id.home_icon).setOnClickListener(onClickListener);
        findViewById(R.id.setting_icon).setOnClickListener(onClickListener);
        findViewById(R.id.power_icon).setOnClickListener(onClickListener);

        mPopMenu = new PopMenu(findViewById(R.id.more));
        mPopMenu.addItem(R.drawable.menu_save_icon, R.id.server_test, getResources().getString(R.string.menu_server_test));
        mPopMenu.addItem(R.drawable.menu_update_icon, R.id.network_ping, getContext().getResources().getString(R.string.lbl_net_test));
        mPopMenu.addItem(R.drawable.menu_update_icon, R.id.app_update, getContext().getResources().getString(R.string.lbl_brank_update));
        mPopMenu.addItem(R.drawable.menu_save_icon, R.id.logout, getResources().getString(R.string.menu_logout));

        mPopMenu.setOnItemClickListener(new PopMenu.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id, MenuItem menuItem) {
                if (!ExternalParam.getInstance().getUserData().isTeacher() && ExternalParam.getInstance().getStatus() == 2) {
                    ToastUtil.show(getResources().getString(R.string.toast_class_ing));
                    return;
                }

                if (menuItem.itemId == R.id.logout) {
                    Activity activity = (Activity) getContext();
                    activity.finish();
                } else if (menuItem.itemId == R.id.connect_again) {
                    if(mCallback!=null){
                        mCallback.connect_again();
                    }
                } else if (menuItem.itemId == R.id.network_ping) {
                    if (ExternalParam.getInstance().getUserData().isTeacher()) {
                        if (ExternalParam.getInstance().getClassData() == null) {
                            ToastUtil.show(R.string.toast_class_no);
                            return;
                        }
                    }
                    AppCompatActivity activity = (AppCompatActivity) getContext();
                    FrameDialog.show(activity.getSupportFragmentManager(), new NetWorkTestFragment());
                } else if (menuItem.itemId == R.id.app_update) {
                    Uri uri = Uri.parse("https://github.com/Eaway2009/GitTest/blob/master/flip-v1.10_20190623.apk?raw=true");
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    Activity activity = (Activity) getContext();
                    activity.startActivity(intent);
//                    UpdateHelper.getInstance().update(getContext(), false);
                } else if (menuItem.itemId == R.id.server_test) {
                    AppCompatActivity activity = (AppCompatActivity) getContext();
                    FrameDialog.show(activity.getSupportFragmentManager(), ServerTesterFragment.newInstance());
                    if (ExternalParam.getInstance().getUserData().isTeacher())
                        MyMqttService.publishMessage(PushMessage.COMMAND.SERVER_PING, (List<String>) null, null);
                }
            }
        });
        findViewById(R.id.more).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopMenu.show();
            }
        });
    }

    public void refreshMessageCount() {
        mUpdateHandler.sendEmptyMessage(0);
    }

    private Handler mUpdateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            try {
                int count = Database.getInstance().newMessageCount();
                if (count > 0) {
                    mMessageView.setCornerText(String.valueOf(count));
                } else {
                    mMessageView.hiddenCorner();
                }
                removeMessages(0);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };
    OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.profile_image:
                case R.id.username:
                    mCallback.showPage(MainActivity.MODULE_ID_USER_PAGE);
//                    UserData userData = ExternalParam.getInstance().getUserData();
//                    if (!userData.isTeacher()) {
//                        if (ExternalParam.getInstance().getStatus() != 0) {
//                            ToastUtil.show(R.string.toast_class_ing);
//                            return;
//                        }
//                    }
//
//                    AppCompatActivity activity = (AppCompatActivity) getContext();
//                    FrameDialog.show(activity.getSupportFragmentManager(), new UserInfoFragment());
                    break;
                case R.id.home_icon:
                    MainActivity.startMe(getContext());
                    break;
                case R.id.setting_icon:
                    mCallback.showPage(MainActivity.MODULE_ID_SETTING_PAGE);
                    break;
                case R.id.power_icon:
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                            .setTitle(getResources().getString(R.string.dialog_warning))
                            .setMessage(getResources().getString(R.string.dialog_logout_warning))
                            .setPositiveButton(getResources().getString(R.string.sure), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Activity activity = (Activity) getContext();
                                    activity.finish();
                                }
                            })
                            .setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mLogoutDialog.dismiss();
                                }
                            })
                            .setCancelable(false)
                            .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    mLogoutDialog = null;
                                }
                            });
                    mLogoutDialog = builder.create();
                    mLogoutDialog.show();
                    break;
            }

        }
    };

    public void setCallback(Callback callback){
        mCallback = callback;
    }

    public interface Callback{
        public void connect_again();
        public void showPage(int modulePageId);
    }

}
