package com.tanhd.rollingclass.fragments.pages;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.tanhd.rollingclass.R;

public class LearnCasesContainerFragment extends Fragment implements OnClickListener {

    private ImageView mIvSetting1;
    private ImageView mIvSetting2;

    public static LearnCasesContainerFragment newInstance(int typeId) {
        Bundle args = new Bundle();
        args.putInt("typeId", typeId);
        LearnCasesContainerFragment page = new LearnCasesContainerFragment();
        page.setArguments(args);
        return page;
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
        mIvSetting2 = view.findViewById(R.id.iv_set1);
        view.findViewById(R.id.iv_set1).setOnClickListener(this);
        view.findViewById(R.id.iv_set2).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_set1:
                break;
            case R.id.iv_set2:
                break;
        }
    }
}
