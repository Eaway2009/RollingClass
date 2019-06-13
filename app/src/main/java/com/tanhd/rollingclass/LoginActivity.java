package com.tanhd.rollingclass;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tanhd.library.mqtthttp.MQTT;
import com.tanhd.rollingclass.db.Database;
import com.tanhd.rollingclass.server.RequestCallback;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.StudentData;
import com.tanhd.rollingclass.server.data.TeacherData;
import com.tanhd.rollingclass.server.data.UserData;
import com.tanhd.rollingclass.utils.AppUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSION = 1;
    private static final String TAG = "LoginActivity";
    private EditText mUserView;
    private EditText mPasswordView;
    private View mSignButtonView;
    private ProgressBar mProgressBar;
    private CheckBox  mCheckBox;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mUserView = findViewById(R.id.acc);
        mPasswordView = findViewById(R.id.password);
        mProgressBar = findViewById(R.id.progressbar);
        TextView versionView = findViewById(R.id.version);
        try {
            String title = getString(R.string.login_title);
            versionView.setText(String.format("%s(v%s)", title, getPackageManager().getPackageInfo(getPackageName(), 0).versionName));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        mCheckBox = findViewById(R.id.save_pwd);

        mSignButtonView = findViewById(R.id.sign);
        mSignButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = mUserView.getText().toString();
                String password = mPasswordView.getText().toString();
                if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "用户名和密码不能为空!", Toast.LENGTH_LONG).show();
                    return;
                }

                new LoginTask(username, password, mCheckBox.isChecked()).execute();
            }
        });

        ExternalParam.getInstance().empty();
        ScopeServer.getInstance();
        requestPermission();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSION:
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(getApplicationContext(), "请点击<允许>", Toast.LENGTH_LONG).show();
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
        //findViewById(R.id.sign).callOnClick();
    }

    private class LoginTask extends AsyncTask<Void, Void, Integer> {
        private final String mUserName;
        private final String mPassword;
        private final boolean mChecked;

        public LoginTask(String userName, String password, boolean checked) {
            mUserName = userName;
            mPassword = password;
            mChecked = checked;
        }

        @Override
        protected void onPreExecute() {
            Log.i(TAG, "onPreExecute: 开始时间");
            changeViewsStatus(true);
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            Log.i(TAG, "doInBackground: 准备调用接口");
            String response = ScopeServer.getInstance().loginToServer(mUserName, mPassword);
            Log.i(TAG, "doInBackground: 接口返回");

            if (response != null) {
                JSONObject json;
                try {
                    json = new JSONObject(response);
                    String errorCode = json.optString("errorCode");
                    if (!TextUtils.isEmpty(errorCode) && errorCode.equals("0")) {
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
                    }
                } catch (JSONException e) {
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
                UserData userData = ExternalParam.getInstance().getUserData();
                String ownerID = userData.getOwnerID();
                Log.i(TAG, "onPostExecute: 初始化MQ");
                //初始化MQ
                if (MQTT.getInstance(ownerID, 8080) == null) {
                    Toast.makeText(getApplicationContext(), "连接消息服务器失败, 请重试!", Toast.LENGTH_LONG).show();
                    changeViewsStatus(false);
                    return;
                }
                Log.i(TAG, "onPostExecute: 初始化MQ完成");

                boolean flag = MQTT.getInstance().connect();
                if (!flag) {
                    Toast.makeText(getApplicationContext(), "连接消息服务器失败, 请重试!", Toast.LENGTH_LONG).show();
                    changeViewsStatus(false);
                    return;
                }
                Log.i(TAG, "onPostExecute: 连接MQ");

//                if (!userData.isTeacher()) {
//                    StudentData studentData = (StudentData) userData.getUserData();
//                    new RefreshTask(studentData.Token).execute();
//
//                } else {
                Intent in = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(in);
                finish();
//                }
            } else {
                changeViewsStatus(false);
                if (result == -3)
                    Toast.makeText(getApplicationContext(), "连接超时，请检查服务器是否工作!", Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(getApplicationContext(), "登陆失败，用户名或密码错误!", Toast.LENGTH_LONG).show();
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

}
