package com.tanhd.rollingclass.fragments;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.MediaController;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.VideoPlayerActivity;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.views.VideoViewEx;

import org.json.JSONException;
import org.json.JSONObject;

public class VideoPlayerFragment extends Fragment {

    private String mMicroCourseID;
    private String mResourceAddr;
    private VideoViewEx videoView;
    private MediaController mediaController;
    private boolean mCloseFlag;

    public static VideoPlayerFragment newInstance(String MicroCourseID, String VideoUrl){
        VideoPlayerFragment videoPlayerFragment = new VideoPlayerFragment();
        Bundle bundle = new Bundle();
        bundle.putString("MicroCourseID", MicroCourseID);
        bundle.putString("ResourceAddr", ScopeServer.getInstance().getResourceUrl() + VideoUrl);
        videoPlayerFragment.setArguments(bundle);
        return videoPlayerFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.activity_video_player, container, false);

        Bundle args = getArguments();
        mMicroCourseID = args.getString("MicroCourseID");
        mResourceAddr = args.getString("ResourceAddr");

        videoView = (VideoViewEx) view.findViewById(R.id.videoview);

        //加载指定的视频文件
        videoView.setVideoURI(Uri.parse(mResourceAddr));

        //创建MediaController对象
        mediaController = new MediaController(getActivity());

        //VideoView与MediaController建立关联
        videoView.setMediaController(mediaController);

        //让VideoView获取焦点
        videoView.requestFocus();

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                videoView.start();
            }
        });

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                JSONObject json = new JSONObject();
                try {
                    json.put("course_id", mMicroCourseID);
                    json.put("end_time", System.currentTimeMillis());
                    json.put("start_time", videoView.getStartTime());
                    json.put("studentID", ExternalParam.getInstance().getUserData().getOwnerID());
                    json.put("video_end_time", (videoView.getCurrentPosition() / 1000));
                    json.put("video_start_time", (videoView.getStartPosition() / 1000));
                    json.put("statistic_id", mMicroCourseID);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                new UploadDataTask().execute(json.toString());
            }
        });
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();

        if (videoView.isPlaying()) {
            mCloseFlag = true;
            videoView.pause();
        }

    }

    private class UploadDataTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected Integer doInBackground(String... strings) {
            String data = strings[0];

            return ScopeServer.getInstance().InsertMicroCourseStatistic(data);
        }

        @Override
        protected void onPostExecute(Integer integer) {

        }
    }
}
