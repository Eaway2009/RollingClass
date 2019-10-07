package com.tanhd.rollingclass.fragments;

import android.os.AsyncTask;
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
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.QuestionSetData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class QuestionSetListFragment extends Fragment {
    private ArrayList<QuestionSetData> mSetList;
    private ListView mListView;
    private SetAdapter mAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_questionset_list, container, false);
        mListView = view.findViewById(R.id.list);
        mAdapter = new SetAdapter();
        mListView.setAdapter(mAdapter);
        new LoadDataTask().execute();
        return view;
    }

    private class LoadDataTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            String teacherID = ExternalParam.getInstance().getUserData().getOwnerID();
            mSetList = (ArrayList<QuestionSetData>) ScopeServer.getInstance().QureyQuestionSetByTeacherID(teacherID);
            if (mSetList == null)
                return null;

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mAdapter.notifyDataSetChanged();
        }
    }

    private class SetAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if (mSetList == null)
                return 0;

            return mSetList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.item_questionset_list, parent, false);
                view.setBackgroundResource(R.drawable.list_item_selector);
            }

            final QuestionSetData setData = mSetList.get(position);
            TextView noView = view.findViewById(R.id.no);
            TextView timeView = view.findViewById(R.id.time);
            noView.setText(String.format(getResources().getString(R.string.lbl_tw_no), mSetList.size() - position));

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            timeView.setText(getResources().getString(R.string.lbl_tw_time) + simpleDateFormat.format(new Date(setData.CreateTime)));

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getParentFragment() instanceof FrameDialog) {
                        FrameDialog dialog = (FrameDialog) getParentFragment();
                        dialog.replaceFragment(WaitAnswerFragment.newInstance(setData.QuestionSetID));
                    }
                }
            });
            return view;
        }
    }
}
