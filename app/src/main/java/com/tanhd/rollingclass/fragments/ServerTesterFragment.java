package com.tanhd.rollingclass.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.tanhd.library.mqtthttp.MQTT;
import com.tanhd.library.mqtthttp.MqttListener;
import com.tanhd.library.mqtthttp.MyMqttService;
import com.tanhd.library.mqtthttp.PushMessage;
import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.server.data.SchoolData;
import com.tanhd.rollingclass.server.data.UserData;

import java.util.ArrayList;
import java.util.List;

public class ServerTesterFragment extends Fragment {
    private class ItemData {
        boolean result;
        long time;
    }
    private ListView mListView;
    private ItemAdapter mAdapter;
    private ArrayList<ItemData> mItemDataList = new ArrayList<>();

    public static ServerTesterFragment newInstance() {
        ServerTesterFragment fragment = new ServerTesterFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_server_tester, container, false);
        mListView = view.findViewById(R.id.list);
        mAdapter = new ItemAdapter();
        mListView.setAdapter(mAdapter);

        new PingTask().execute();
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        MQTT.register(mqttListener);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        MQTT.unregister(mqttListener);
        if (ExternalParam.getInstance().getUserData().isTeacher()) {
            MyMqttService.publishMessage(PushMessage.COMMAND.SERVER_PING_STOP, (List<String>) null, null);
        }
    }

    private class PingTask extends AsyncTask<Void, Void, Long> {
        boolean mResult = false;

        @Override
        protected Long doInBackground(Void... voids) {
            long start = System.currentTimeMillis();
            SchoolData schoolData = ScopeServer.getInstance().QureySchool();
            mResult = (schoolData != null);
            return System.currentTimeMillis() - start;
        }

        @Override
        protected void onPostExecute(Long aLong) {
            ItemData itemData = new ItemData();
            itemData.result = mResult;
            itemData.time = aLong.longValue();
            mItemDataList.add(itemData);
            mAdapter.notifyDataSetChanged();

            if (isAdded()) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        new PingTask().execute();
                    }
                }, 1000);
            }
        }
    }

    private class ItemAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mItemDataList.size();
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
                view = getLayoutInflater().inflate(R.layout.item_server_tester, parent, false);
            }

            ItemData itemData = mItemDataList.get(position);
            TextView noView = view.findViewById(R.id.no);
            noView.setText(position + "");

            TextView timeView = view.findViewById(R.id.time);
            TextView statusView = view.findViewById(R.id.status);
            if (itemData.result) {
                statusView.setText("成功");
                statusView.setTextColor(0xFF12FF12);
                timeView.setText(itemData.time + "ms");
            } else {
                statusView.setText("失败");
                statusView.setTextColor(0xFFFF1212);
                timeView.setText("超时");
            }

            return view;
        }
    }

    private MqttListener mqttListener = new MqttListener() {
        @Override
        public void messageArrived(PushMessage message) {
            if (message.command == PushMessage.COMMAND.SERVER_PING_STOP) {
                if (getParentFragment() instanceof FrameDialog) {
                    FrameDialog dialog = (FrameDialog) getParentFragment();
                    dialog.dismiss();
                }
            }
        }

        @Override
        public void networkTimeout(boolean flag) {

        }
    };
}
