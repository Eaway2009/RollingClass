package com.tanhd.rollingclass;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.tanhd.rollingclass.base.BaseActivity;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.StudentData;
import com.tanhd.rollingclass.server.data.TeacherData;
import com.tanhd.rollingclass.server.data.UserData;
import com.tanhd.rollingclass.utils.AppUtils;
import com.tanhd.rollingclass.utils.ToastUtil;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LoginActivity extends BaseActivity {
    private static final int REQUEST_PERMISSION = 1;
    private static final int REQUEST_TIME = 1;
    private static final String TAG = "LoginActivity";
    private EditText mUserView;
    private EditText mPasswordView;
    private View mSignButtonView;
    private EditText mIpEditView;
    private View mIpLayout;
    private View mIpButton;
    private ProgressBar mProgressBar;
    private CheckBox mSavePwdCheckBox;
    private CheckBox mCheckBox;
    private boolean mLongClicked = false;


    GestureDetector gestureDetector;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mUserView = findViewById(R.id.acc);
        mPasswordView = findViewById(R.id.password);
        mProgressBar = findViewById(R.id.progressbar);
        mSavePwdCheckBox = findViewById(R.id.save_pwd);
        mCheckBox = findViewById(R.id.checkbox);

        mSignButtonView = findViewById(R.id.sign_button);
        mIpEditView = findViewById(R.id.ip_edittext);
        mIpButton = findViewById(R.id.ip_button);
        mIpLayout =this.findViewById(R.id.ip_layout);
        mSignButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = mUserView.getText().toString();
                String password = mPasswordView.getText().toString();
                if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                    ToastUtil.show(R.string.toast_name_pwd_empty);
                    return;
                }

                new LoginTask(username, password, mSavePwdCheckBox.isChecked(), mCheckBox.isChecked()).execute();
            }
        });

        ExternalParam.getInstance().empty();
        ScopeServer.getInstance();
        requestPermission();
        gestureDetector = new GestureDetector(LoginActivity.this, new GestureDetector.SimpleOnGestureListener() {

            /**
             * 发生确定的单击时执行
             * @param e
             * @return
             */
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                return super.onSingleTapConfirmed(e);
            }

            /**
             * 双击发生时的通知
             * @param e
             * @return
             */
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                mIpLayout.setVisibility(View.VISIBLE);
                mIpEditView.setText(ScopeServer.getInstance().getHost());
                return super.onDoubleTap(e);
            }

            /**
             * 双击手势过程中发生的事件，包括按下、移动和抬起事件
             * @param e
             * @return
             */
            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                return super.onDoubleTapEvent(e);
            }
        });
//
//        findViewById(R.id.tab_layout).setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                mLongClicked = true;
//                mHandler.sendEmptyMessageDelayed(REQUEST_TIME, 5000);
//                return false;
//            }
//        });
//        findViewById(R.id.tab_layout).setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                if(mLongClicked) {
//                    return gestureDetector.onTouchEvent(event);
//                }else{
//                    return LoginActivity.super.onTouchEvent(event);
//                }
//            }
//        });
        findViewById(R.id.close_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIpLayout.setVisibility(View.GONE);
                mLongClicked = false;
            }
        });

        mIpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newIpUrl = mIpEditView.getText().toString();
                ScopeServer.getInstance().setHost(newIpUrl);
                mIpLayout.setVisibility(View.GONE);
                mLongClicked = false;

            }
        });

        mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                AppUtils.changeTeacherOrStudent(LoginActivity.this, isChecked);
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSION:
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        ToastUtil.show(getResources().getString(R.string.toast_allow));
                        finish();
                        return;
                    }
                }

                check();
                break;
        }
    }

    private void check() {
        autoLogin();
    }

    private void requestPermission() {
        final List<String> permissionsList = new ArrayList<String>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
            permissionsList.add(Manifest.permission.READ_EXTERNAL_STORAGE);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
            permissionsList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED)
            permissionsList.add(Manifest.permission.READ_PHONE_STATE);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WAKE_LOCK)
                != PackageManager.PERMISSION_GRANTED)
            permissionsList.add(Manifest.permission.WAKE_LOCK);

        if (permissionsList.size() == 0) {
            check();

        } else {
            ActivityCompat.requestPermissions(this, permissionsList.toArray(new String[permissionsList.size()]),
                    REQUEST_PERMISSION);
        }
    }

    private void autoLogin() {
        String username = AppUtils.readLoginUserName(getApplicationContext());
        if (TextUtils.isEmpty(username)) {
            return;
        }

        CheckBox checkBox = findViewById(R.id.save_pwd);
        checkBox.setChecked(true);
        mUserView.setText(username);
        mPasswordView.setText(AppUtils.readLoginPassword(getApplicationContext()));
        mSignButtonView.performClick();
    }

    private class LoginTask extends AsyncTask<Void, Void, Integer> {
        private String mErrorMessage;
        private final String mUserName;
        private final String mPassword;
        private final boolean mChecked;
        private final boolean mIsTeacher;

        public LoginTask(String userName, String password, boolean checked, boolean isTeacher) {
            mUserName = userName;
            mPassword = password;
            mChecked = checked;
            mIsTeacher = isTeacher;
        }

        @Override
        protected void onPreExecute() {
            Log.i(TAG, "onPreExecute: 开始时间");
            changeViewsStatus(true);
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            Log.i(TAG, "doInBackground: 准备调用接口");
            String response;
            if (mIsTeacher) {
                response = ScopeServer.getInstance().teacherLoginToServer(mUserName, mPassword);
            } else {
                response = ScopeServer.getInstance().studentLoginToServer(mUserName, mPassword);
            }
            Log.i(TAG, "doInBackground: 接口返回");

            if (response != null) {
                JSONObject json;
                try {
                    json = new JSONObject(response);
                    String errorCode = json.optString("errorCode");
                    mErrorMessage = json.optString("errorMessage");
                    if (!TextUtils.isEmpty(errorCode)) {
                        if (errorCode.equals("0")) {
                            JSONObject result = json.getJSONObject("result");
                            UserData userData = new UserData();
                            int role = json.optInt("role");
                            if (role == 1) {
                                TeacherData teacherData = new TeacherData();
                                teacherData.parse(teacherData, result);
                                userData.setData(UserData.ROLE.TEACHER, teacherData);
                            } else {
                                StudentData studentData = new StudentData();
                                studentData.parse(studentData, result);
                                userData.setData(UserData.ROLE.STUDENT, studentData);
                            }
                            ExternalParam.getInstance().setUserData(userData);
                            ScopeServer.getInstance().initToken(userData);
                            if (mChecked) {
                                AppUtils.saveLoginInfo(getApplicationContext(), mUserName, mPassword);
                            } else {
                                AppUtils.clearLoginInfo(getApplicationContext());
                            }
                            Log.i(TAG, "doInBackground: 返回解析完成");
                            return 0;
                        } else {
                            return Integer.valueOf(errorCode);
                        }
                    }
                } catch (Exception e) {
                    return -1;
                }
                return -2;
            }

            return -3;
        }

        @Override
        protected void onPostExecute(Integer result) {
            Log.i(TAG, "onPostExecute: 处理结果");
            if (result == 0) {
                Intent in = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(in);
                finish();
            } else {
                changeViewsStatus(false);
                if (result < 0) {
                    ToastUtil.show(R.string.toast_net_timeout);
                } else {
                    ToastUtil.show(mErrorMessage);
                }
            }
        }
    }


    private class RefreshTask extends AsyncTask<Void, Void, Map<String, String>> {
        private String mToken;

        public RefreshTask(String token) {
            mToken = token;
        }

        @Override
        protected Map<String, String> doInBackground(Void... voids) {
            return ScopeServer.getInstance().refreshExpiration(mToken);
        }

        @Override
        protected void onPostExecute(Map<String, String> result) {
            changeViewsStatus(false);
            if (result.containsKey("errorCode")) {
                String code = result.get("errorCode");
                if ("0".equals(code)) {
                    Intent in = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(in);
                    finish();
                } else {
                    String errorMessage = result.get("errorMessage");
                    Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
                }
            }
        }

    }

    private void changeViewsStatus(boolean loading) {
        if (loading) {
            mProgressBar.setVisibility(View.VISIBLE);
            mUserView.setEnabled(false);
            mPasswordView.setEnabled(false);
            mSignButtonView.setEnabled(false);
        } else {
            mProgressBar.setVisibility(View.GONE);
            mUserView.setEnabled(true);
            mPasswordView.setEnabled(true);
            mSignButtonView.setEnabled(true);

        }
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case REQUEST_TIME:
                    mLongClicked = false;
                    break;
            }
        }
    };

}
