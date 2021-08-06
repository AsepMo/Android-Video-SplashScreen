package com.media.video;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import java.util.ArrayList;

import com.video.evolution.application.folders.VideoFolder;
import com.video.evolution.application.library.models.VideoData;
import com.video.evolution.application.library.tasks.VideoTask;
import com.video.evolution.application.player.utils.SplashScreen;

public class SplashActivity extends AppCompatActivity {
    private SplashScreen mSplashScreen;
    private View mSplash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evolution);

        Toolbar mToolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        VideoFolder.initVideoBox(this);
        mSplash = findViewById(R.id.splash);
        mSplash.setVisibility(View.VISIBLE);

        mSplashScreen = (SplashScreen)findViewById(R.id.icon);
        mSplashScreen.setVisibility(View.GONE);

        int SPLASH_TIME_OUT = 5000;
        new Handler().postDelayed(new Runnable() {

                /*
                 * Showing splash screen with a timer. This will be useful when you
                 * want to show case your app logo / company
                 */

                @Override
                public void run() {
                    // This method will be executed once the timer is over
                    // Start SplashScreen
                    mSplash.setVisibility(View.GONE);
                    // hide actionbar
                    getSupportActionBar().hide();
                    // hide navigation bar
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);
                    mSplashScreen.setVisibility(View.VISIBLE);
                    mSplashScreen.start();
                    mSplashScreen.setOnSplashScreenListener(new SplashScreen.OnSplashScreenListener(){
                            @Override
                            public void OnStartActivity() {
                                VideoTask task = new VideoTask(SplashActivity.this);
                                task.setOnVideoTaskListener(new VideoTask.OnVideoTaskListener(){
                                        @Override
                                        public void onPreExecute() {

                                        }
                                        @Override
                                        public void onSuccess(ArrayList<VideoData> result) {
                                            Intent mIntent = new Intent(SplashActivity.this, MainActivity.class);
                                            startActivity(mIntent);
                                            mSplashScreen.createShortCut(SplashActivity.this, mIntent);
                                        }

                                        @Override
                                        public void onFailed() {

                                        } 
                                        @Override
                                        public void isEmpty() {

                                        } 
                                    });
                                task.execute();
                            }

                        });

                }
            }, SPLASH_TIME_OUT);   
    }


}
