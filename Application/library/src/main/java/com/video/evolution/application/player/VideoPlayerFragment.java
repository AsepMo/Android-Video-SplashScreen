package com.video.evolution.application.player;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;

import com.video.evolution.R;
import com.video.evolution.application.folders.VideoFolder;
import com.video.evolution.application.player.VideoPlayer;
import com.video.evolution.application.player.VideoPlayer.OnVideoPlayerListener;
import com.video.evolution.application.player.utils.VideoPlayerUtils;
import com.video.evolution.application.library.tasks.VideoTask;
import com.video.evolution.application.library.tasks.VideoPlayerTask;
import java.util.ArrayList;
import com.video.evolution.application.library.models.VideoData;

public class VideoPlayerFragment extends Fragment implements OnVideoPlayerListener {

    private static final String EXTRA_TEXT = "text";
    private VideoPlayer mPlayer;
    public LinearLayout mProgressLayout;
    private LinearLayout mWelcomeLayout;

    public static VideoPlayerFragment createFor(String text) {
        VideoPlayerFragment fragment = new VideoPlayerFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_TEXT, text);
        fragment.setArguments(args);
        return fragment;
    }


    private Handler mHandler = new Handler();
    private Runnable mRunner = new Runnable(){
        @Override
        public void run() {
            setVideoTask();
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_video_player, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        //final String text = getArguments().getString(EXTRA_TEXT);


        mPlayer = (VideoPlayer)view.findViewById(R.id.video_player);     
        mPlayer.setOnVideoPlayerListener(this);
        mProgressLayout = (LinearLayout)view.findViewById(R.id.video_library_progress);       
        mWelcomeLayout = (LinearLayout)view.findViewById(R.id.welcome_layout);
        File file = new File(VideoData.FOLDER + "/" + VideoData.FILENAME);
        if (!file.exists()) {
            setVideoTask();
        } else {
            VideoFolder.initVideoBox(getActivity());
            setVideoTask();
        } 

        //  mPlayer.setVideoURI(Uri.parse("android.resource://" + getActivity().getPackageName() + "/" + R.raw.loading_sound_effects));
    }

    public void setVideoTask() {
        VideoTask task = new VideoTask(getActivity());
        task.setOnVideoTaskListener(new VideoTask.OnVideoTaskListener(){
                @Override
                public void onPreExecute() {
                    mProgressLayout.setVisibility(View.VISIBLE);
                    mPlayer.setVisibility(View.GONE);
                    mWelcomeLayout.setVisibility(View.GONE);
                }
                @Override
                public void onSuccess(ArrayList<VideoData> result) {
                    mProgressLayout.setVisibility(View.GONE);
                    mPlayer.getPlaylist();    
                    mPlayer.setVisibility(View.VISIBLE);
                    mWelcomeLayout.setVisibility(View.GONE);
                }

                @Override
                public void onFailed() {
                    mProgressLayout.setVisibility(View.GONE);
                    mPlayer.setVisibility(View.GONE);
                    mWelcomeLayout.setVisibility(View.VISIBLE);
                } 
                @Override
                public void isEmpty() {
                    mProgressLayout.setVisibility(View.GONE);
                    mPlayer.setVisibility(View.GONE);
                    mWelcomeLayout.setVisibility(View.VISIBLE);
                } 
            });
        task.execute();
    }

    @Override
    public void onNavigationIconClick(View v) {
        getActivity().finish();
    }

    @Override
    public void onVideoPlaying(VideoPlayer mMediaPlayer, String title, String thumbnail, String path) {

    }

    @Override
    public void onVideoPlaylist(View v) {

    }

    @Override
    public void onVideoComplete(VideoPlayer mp) {
        mPlayer.setVideoTask();
    }

    @Override
    public void onVideoOrientation(View v) {
        //mPlayer.getScreenOrientation(getActivity());
    }

    @Override
    public void onVideoError(VideoPlayer player, Exception e) {
    }

    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mRunner);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mRunner);
    }

}

