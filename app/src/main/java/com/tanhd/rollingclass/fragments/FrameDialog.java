package com.tanhd.rollingclass.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.tanhd.rollingclass.R;

import java.io.Serializable;
import java.util.ArrayList;

public class FrameDialog extends DialogFragment {
    private static String mTopName;
    private Fragment mFragment;
    private boolean mIsFullMode;
    private boolean mIsLittleMode;

    private static FrameDialog newInstance(boolean fullMode) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("fullMode", fullMode);

        FrameDialog dialog = new FrameDialog();
        dialog.setArguments(bundle);
        return dialog;
    }

    private static FrameDialog newInstance(boolean fullMode, boolean little) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("fullMode", fullMode);
        bundle.putBoolean("littleMode", little);

        FrameDialog dialog = new FrameDialog();
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setRetainInstance(true);
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.TransparentDialog);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mIsFullMode = getArguments().getBoolean("fullMode");
        mIsLittleMode = getArguments().getBoolean("littleMode");

        View view;
        if (!mIsFullMode) {
            if (!mIsLittleMode) {
                view = inflater.inflate(R.layout.dialog_frame, container, false);
                getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.dialog_background)));
                view.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            dismiss();
                        } catch (Exception e) {

                        }
                    }
                });
            } else {
                view = inflater.inflate(R.layout.little_dialog_frame, container, false);
            }
        } else {
            view = inflater.inflate(R.layout.dialog_frame_full, container, false);
        }

        return view;
    }

    public void setFragment(Fragment fragment) {
        mFragment = fragment;
    }

    public void replaceFragment(Fragment fragment) {
        setFragment(fragment);
        refreshFragment();
    }

    @Override
    public void onStart() {
        super.onStart();
        if(!mIsLittleMode) {
            Dialog dialog = getDialog();
            if (dialog != null) {
                Window window = dialog.getWindow();
                if (window != null) {
                    int width = ViewGroup.LayoutParams.MATCH_PARENT;
                    int height = ViewGroup.LayoutParams.MATCH_PARENT;
                    window.setLayout(width, height);
                }
            }
        }

        refreshFragment();
    }

    private void refreshFragment() {
        String fragmentTag = "dialogTag";
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment, mFragment, fragmentTag);
        transaction.addToBackStack(fragmentTag);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.commit();
    }

    public static void show(FragmentManager manager, Fragment fragment) {
        FrameDialog dialog = FrameDialog.newInstance(false);
        dialog.setFragment(fragment);
        dialog.show(manager, (String) null);
    }

    public static void showLittleDialog(FragmentManager manager, Fragment fragment) {
        FrameDialog dialog = FrameDialog.newInstance(false, true);
        dialog.setFragment(fragment);
        dialog.show(manager, (String) null);
    }

    public static void fullShow(FragmentManager manager, Fragment fragment) {
        FrameDialog dialog = FrameDialog.newInstance(true);
        dialog.setFragment(fragment);
        dialog.show(manager, (String) null);
    }
}
