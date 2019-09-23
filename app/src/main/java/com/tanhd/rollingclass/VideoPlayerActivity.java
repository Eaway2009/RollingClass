package com.tanhd.rollingclass;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.Toast;

import com.tanhd.rollingclass.base.BaseActivity;
import com.tanhd.rollingclass.server.ScopeServer;
import com.tanhd.rollingclass.server.data.ExternalParam;
import com.tanhd.rollingclass.views.VideoViewEx;

import org.json.JSONException;
import org.json.JSONObject;


public class VideoPlayerActivity extends BaseActivity {
    private String mMicroCourseID;
    private String mResourceAddr;
    private boolean mCloseFlag = false;
    private VideoViewEx videoView;
    private MediaController mediaController;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams. FLAG_FULLSCREEN);
        setContentView(R.layout.activity_video_player);
        mMicroCourseID = getIntent().getStringExtra("MicroCourseID");
        mResourceAddr = getIntent().getStringExtra("ResourceAddr");

        videoView = (VideoViewEx) findViewById(R.id.videoview);

        //加载指定的视频文件
        videoView.setVideoURI(Uri.parse(mResourceAddr));

        //创建MediaController对象
        mediaController = new MediaController(this);

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
    }

    @Override
    public void onBackPressed() {
        if (videoView.isPlaying()) {
            mCloseFlag = true;
            videoView.pause();
        } else {
            super.onBackPressed();
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
            if (mCloseFlag)
                VideoPlayerActivity.super.onBackPressed();
        }
    }

}
