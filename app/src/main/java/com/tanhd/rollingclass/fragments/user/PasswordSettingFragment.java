package com.tanhd.rollingclass.fragments.user;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.fragments.UserInfoFragment;
import com.tanhd.rollingclass.server.RequestCallback;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.UserData;
import com.tanhd.rollingclass.utils.AppUtils;
import com.tanhd.rollingclass.utils.ToastUtil;

public class PasswordSettingFragment extends Fragment implements View.OnClickListener {
    private EditText mOriPwdView;
    private EditText mNewPwdView;
    private EditText mRecheckPwdView;
    private View mSaveButton;
    private String mPassword;
    private View mOriPasswordWarning;
    private Callback mCallback;
    private View mNewPasswordWarning;

    public static PasswordSettingFragment newInstance(Callback callback) {
        PasswordSettingFragment fragment = new PasswordSettingFragment();
        fragment.setCallback(callback);
        return fragment;
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_setting_psd, container, false);
        mPassword = AppUtils.readLoginPassword(getContext());
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        mOriPwdView = view.findViewById(R.id.ori_password);
        mNewPwdView = view.findViewById(R.id.new_password);
        mRecheckPwdView = view.findViewById(R.id.recheck_password);
        mSaveButton = view.findViewById(R.id.save_button);
        mOriPasswordWarning = view.findViewById(R.id.ori_password_warning);
        mNewPasswordWarning = view.findViewById(R.id.new_password_wrong);

        mSaveButton.setOnClickListener(this);
        mOriPwdView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String password = mOriPwdView.getText().toString();
                if (!TextUtils.isEmpty(password) && !password.equals(mPassword)) {
                    mOriPasswordWarning.setVisibility(View.VISIBLE);
                } else {
                    mOriPasswordWarning.setVisibility(View.INVISIBLE);
                }
            }
        });
        mRecheckPwdView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String newPassword = mOriPwdView.getText().toString();
                String recheckPassword = mRecheckPwdView.getText().toString();
                if (!TextUtils.isEmpty(newPassword) && !TextUtils.isEmpty(recheckPassword) && !newPassword.equals(recheckPassword)) {
                    mNewPasswordWarning.setVisibility(View.VISIBLE);
                } else {
                    mNewPasswordWarning.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.save_button:
                updatePassword();
                break;
        }
    }


    private void updatePassword() {
        String oldPassword = mOriPwdView.getText().toString();
        String newPassword1 = mNewPwdView.getText().toString();
        String newPassword2 = mRecheckPwdView.getText().toString();

        if (!newPassword1.equals(newPassword2)) {
            ToastUtil.show(R.string.toast_pwd_no_fit);
            return;
        }

        if (TextUtils.isEmpty(newPassword1)) {
            ToastUtil.show(R.string.toast_pwd_empty);
            return;
        }

        UserData userData = ExternalParam.getInstance().getUserData();
        String ownerID = userData.getOwnerID();

        RequestCallback callback = new RequestCallback() {
            @Override
            public void onProgress(boolean b) {

            }

            @Override
            public void onResponse(String body) {
                ToastUtil.show(getResources().getString(R.string.toast_pwd_edit_ok));
                if (mCallback != null) {
                    mCallback.onBack();
                }
            }

            @Override
            public void onError(String code, String message) {
                ToastUtil.show(getString(R.string.toast_edit_pwd_fail) + message);
            }
        };

        if (userData.isTeacher()) {
            ScopeServer.getInstance().UpdataTeacherPasswd(ownerID, oldPassword, newPassword1, callback);
        } else {
            ScopeServer.getInstance().UpdataStudentPasswd(ownerID, oldPassword, newPassword1, callback);
        }
    }

    public interface Callback {
        void onBack();
    }
}