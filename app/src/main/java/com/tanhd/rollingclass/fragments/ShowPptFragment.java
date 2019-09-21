package com.tanhd.rollingclass.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageScrollListener;
import com.tanhd.library.mqtthttp.MqttListener;
import com.tanhd.library.mqtthttp.MyMqttService;
import com.tanhd.library.mqtthttp.PushMessage;
import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.server.RequestCallback;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.utils.AppUtils;
import com.tanhd.rollingclass.views.ThumbAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tanhd.rollingclass.db.KeyConstants.SYNC_MODE;

public class ShowPptFragment extends Fragment {
    private Activity mActivity;
    private ListView mThumbsListView;
    private ThumbAdapter mThumbAdapter;
    private SYNC_MODE mSyncMode;
    private PDFView.Configurator mConfigurator;

    private PDFView webView;
    private TextView mLoadFailView;
    private View mProgressBarView;
    private String mUrl;
    private String mPdfFilePath;
    private boolean downLoadFinish = true;
    private static final String TAG = "ShowPptFragment";
    private ArrayList<String> mThumbsList;

    public static ShowPptFragment newInstance(Activity activity, String url, ArrayList<String> thumbs, SYNC_MODE mode) {
        Bundle args = new Bundle();
        args.putString("url", url);
        args.putStringArrayList("thumbs", thumbs);
        args.putInt("mode", mode.ordinal());
        ShowPptFragment fragment = new ShowPptFragment();
        fragment.setArguments(args);
        fragment.setActivity(activity);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mThumbsList = getArguments().getStringArrayList("thumbs");
        mUrl = getArguments().getString("url");
        int mode = getArguments().getInt("mode");
        mSyncMode = SYNC_MODE.values()[mode];
        View view = inflater.inflate(R.layout.fragment_show_ppt, container, false);
        webView = view.findViewById(R.id.webview);
        mLoadFailView = view.findViewById(R.id.load_fail);
        mLoadFailView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                load();
            }
        });
        mProgressBarView = view.findViewById(R.id.progressbar);
        if (mSyncMode == SYNC_MODE.SLAVE) {
            webView.setEnabled(false);
        }
        initListView(view);
        return view;
    }

    private void initListView(View view){
        mThumbsListView = view.findViewById(R.id.thumbs_listview);
        mThumbAdapter = new ThumbAdapter(getActivity());
        mThumbsListView.setAdapter(mThumbAdapter);
        mThumbAdapter.setData(mThumbsList);
        mThumbsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                webView.jumpTo(position);
            }
        });
    }

    public void setActivity(Activity activity){
        mActivity = activity;
    }

    @Override
    public void onStart() {
        super.onStart();
//        if (mSyncMode == SYNC_MODE.SLAVE)
//            MQTT.register(mqttListener);
        downloadPDF();
    }

    public void refreshPpt(String url, ArrayList<String> thumbs){
        if(downLoadFinish || mUrl!=url){
            mUrl = url;
            mThumbsList.clear();
            mThumbsList.addAll(thumbs);
            downloadPDF();
        }
    }

    private void downloadPDF() {
        String fileName = AppUtils.md5(mUrl);
        mPdfFilePath = mActivity.getApplicationContext().getFilesDir().getAbsolutePath()
                + "/" + fileName;
        File file = new File(mPdfFilePath);
        if (file.exists()) {
            load();
            return;
        }
        downLoadFinish = false;
        ScopeServer.getInstance().downloadFile(mUrl, mPdfFilePath, new RequestCallback() {
            @Override
            public void onProgress(boolean b) {
                if (b)
                    mProgressBarView.setVisibility(View.VISIBLE);
                else
                    mProgressBarView.setVisibility(View.GONE);
            }

            @Override
            public void onResponse(String body) {
                load();
                downLoadFinish = true;
            }

            @Override
            public void onError(String code, String message) {
                mProgressBarView.setVisibility(View.GONE);
                mLoadFailView.setVisibility(View.VISIBLE);
                downLoadFinish = true;
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
//临时注释
//        if (mSyncMode == SYNC_MODE.SLAVE)
//            MQTT.unregister(mqttListener);
    }

    private void load() {
        mLoadFailView.setVisibility(View.GONE);
        mProgressBarView.setVisibility(View.VISIBLE);
        mConfigurator = webView.fromFile(new File(mPdfFilePath))
                .enableSwipe(true) // allows to block changing pages using swipe
                .swipeHorizontal(false)
                .enableDoubletap(true)
                .defaultPage(0)
                .enableAnnotationRendering(false) // render annotations (such as comments, colors or forms)
                .password(null)
                .scrollHandle(null)
                .autoSpacing(true)
                .enableAntialiasing(true) // improve rendering a little bit on low-res screens
                .spacing(0)
                .onPageScroll(new OnPageScrollListener() {
                    @Override
                    public void onPageScrolled(int page, float positionOffset) {
                        mThumbsListView.smoothScrollToPosition(page);
                    }
                })
                .onLoad(new OnLoadCompleteListener() {
                    @Override
                    public void loadComplete(int nbPages) {
                        mProgressBarView.setVisibility(View.GONE);
                        publish(PushMessage.COMMAND.SCROLL_CUR, (List<String>) null, null);
                    }
                })
                .onError(new OnErrorListener() {
                    @Override
                    public void onError(Throwable t) {
                        mProgressBarView.setVisibility(View.GONE);
                        mLoadFailView.setVisibility(View.VISIBLE);
                    }
                });

        if (mSyncMode == SYNC_MODE.MASTER) {
            mConfigurator.onPageScroll(mPageScrollListener);
        }

        mConfigurator.load();
    }

    public void publish(PushMessage.COMMAND command, List<String> to, Map<String, String> data) {
        MyMqttService.publishMessage(command, to, data);
    }

    private OnPageScrollListener mPageScrollListener = new OnPageScrollListener() {
        @Override
        public void onPageScrolled(int page, float positionOffset) {
            mThumbsListView.smoothScrollToPosition(page);
            HashMap<String, String> params = new HashMap<>();
            params.put("page", String.valueOf(page));
            params.put("positionOffset", String.valueOf(positionOffset));
            params.put("scale", String.valueOf(webView.getZoom()));
            MyMqttService.publishMessage(PushMessage.COMMAND.SCROLL_TO, (List<String>) null, params);
        }
    };

    private MqttListener mqttListener = new MqttListener() {
        @Override
        public void messageArrived(PushMessage message) {
            if (mSyncMode == SYNC_MODE.NONE)
                return;

            if (mSyncMode == SYNC_MODE.SLAVE) {
                if (message.command == PushMessage.COMMAND.SCROLL_TO) {
                    try {
                        int page = Integer.parseInt(message.parameters.get("page"));
                        float positionOffset = Float.parseFloat(message.parameters.get("positionOffset"));
                        float scale = Float.parseFloat(message.parameters.get("scale"));
                        webView.jumpTo(page);
                        webView.setPositionOffset(positionOffset);
                        webView.zoomWithAnimation(scale);
                    } catch (Exception ex) {
                    }
                }

                return;
            }

            if (message.command == PushMessage.COMMAND.SCROLL_CUR) {
                try {
                    mPageScrollListener.onPageScrolled(webView.getCurrentPage(), webView.getPositionOffset());
                } catch (Exception ex) {
                }
            }
        }

        @Override
        public void networkTimeout(boolean flag) {

        }
    };

}
