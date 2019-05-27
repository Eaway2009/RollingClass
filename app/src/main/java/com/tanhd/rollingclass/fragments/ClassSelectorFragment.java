package com.tanhd.rollingclass.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.server.data.ClassData;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.TeacherData;
import com.tanhd.rollingclass.server.data.UserData;
import com.tanhd.rollingclass.utils.AppUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class ClassSelectorFragment extends Fragment {
    public static interface OnClassListener {
        void onClassSelected(ClassData classData);
    }

    private class ItemData {
        String title;
        ClassData classData;
    }

    private ListView mListView;
    private ClassAdapter mAdapter;
    private List<ItemData> mItemList = new ArrayList<>();
    private OnClassListener mListener;

    public static ClassSelectorFragment newInstance(OnClassListener listener) {
        ClassSelectorFragment fragment = new ClassSelectorFragment();
        fragment.setListener(listener);
        return fragment;
    }

    public void setListener(OnClassListener listener) {
        this.mListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_class_selector, container, false);
        mListView = view.findViewById(R.id.list);
        mAdapter = new ClassAdapter();
        mListView.setAdapter(mAdapter);
        init();
        return view;
    }

    private void init() {
        UserData userData = ExternalParam.getInstance().getUserData();
        if (userData.isTeacher()) {
            TeacherData teacherData = (TeacherData) userData.getUserData();
            for (ClassData classData: ExternalParam.getInstance().getTeachingClass()) {
                ItemData itemData = new ItemData();
                itemData.title = classData.ClassName;
                itemData.classData = classData;
                mItemList.add(itemData);
            }
        }

        mAdapter.notifyDataSetChanged();
    }

    private class ClassAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mItemList.size();
        }

        @Override
        public Object getItem(int position) {
            return mItemList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = getActivity().getLayoutInflater().inflate(R.layout.item_class_selector, parent, false);
            }

            final ItemData itemData = (ItemData) getItem(position);
            TextView nameView = view.findViewById(R.id.class_name);
            nameView.setText(itemData.title);
            view.setBackgroundResource(R.drawable.list_item_selector);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null)
                        mListener.onClassSelected(itemData.classData);

                    if (getParentFragment() instanceof FrameDialog) {
                        FrameDialog dialog = (FrameDialog) getParentFragment();
                        dialog.dismiss();
                    }
                }
            });

            return view;
        }
    }
}
