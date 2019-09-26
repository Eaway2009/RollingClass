package com.tanhd.rollingclass.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.tanhd.library.mqtthttp.MQTT;
import com.tanhd.library.mqtthttp.MyMqttService;
import com.tanhd.library.mqtthttp.PushMessage;
import com.tanhd.rollingclass.LoginActivity;
import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.db.Database;
import com.tanhd.rollingclass.fragments.FrameDialog;
import com.tanhd.rollingclass.fragments.NetWorkTestFragment;
import com.tanhd.rollingclass.fragments.ServerTesterFragment;
import com.tanhd.rollingclass.fragments.UserInfoFragment;
import com.tanhd.rollingclass.server.UpdateHelper;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.UserData;
import com.tanhd.rollingclass.utils.PopMenu;
import com.tanhd.rollingclass.views.popmenu.MenuItem;

import java.security.acl.LastOwnerException;
import java.util.Calendar;
import java.util.List;


public class TopbarView extends CardView {
    private TextView mDateView;
    private TextView mWeekView;
    private CornerImageView mMessageView;
    private PopMenu mPopMenu;
    private Callback mCallback;
    private TextView mUserNameView;

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

        Calendar calendar = Calendar.getInstance();
        String text = String.format("%04d.%02d.%02d", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
        mDateView.setText(text);

        String[] week = new String[] {"日", "一", "二", "三", "四", "五", "六"};
        text = String.format("星期%s", week[calendar.get(Calendar.DAY_OF_WEEK ) - 1]);
        mWeekView.setText(text);

        refreshMessageCount();
        UserData userData = ExternalParam.getInstance().getUserData();
        if (userData!=null){
            mUserNameView.setText(userData.getOwnerName());
        }

        findViewById(R.id.userinfo).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                UserData userData = ExternalParam.getInstance().getUserData();
                if (!userData.isTeacher()) {
                    if (ExternalParam.getInstance().getStatus() != 0) {
                        Toast.makeText(getContext().getApplicationContext(), "正在上课中...", Toast.LENGTH_LONG).show();
                        return;
                    }
                }

                AppCompatActivity activity = (AppCompatActivity) getContext();
                FrameDialog.show(activity.getSupportFragmentManager(), new UserInfoFragment());
            }
        });

        mPopMenu = new PopMenu(findViewById(R.id.more));
        mPopMenu.addItem(R.drawable.menu_save_icon, R.id.server_test, getResources().getString(R.string.menu_server_test));
        mPopMenu.addItem(R.drawable.menu_update_icon, R.id.network_ping, "网络测试");
        mPopMenu.addItem(R.drawable.menu_update_icon, R.id.app_update, "版本更新");
        mPopMenu.addItem(R.drawable.menu_save_icon, R.id.logout, getResources().getString(R.string.menu_logout));

        mPopMenu.setOnItemClickListener(new PopMenu.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id, MenuItem menuItem) {
                if (!ExternalParam.getInstance().getUserData().isTeacher() && ExternalParam.getInstance().getStatus() == 2) {
                    Toast.makeText(getContext().getApplicationContext(), "正在上课中...", Toast.LENGTH_LONG).show();
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
                            Toast.makeText(getContext().getApplicationContext(), "请先选择班级", Toast.LENGTH_LONG).show();
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

    public void setCallback(Callback callback){
        mCallback = callback;
    }

    public interface Callback{
        public void connect_again();
    }

}
