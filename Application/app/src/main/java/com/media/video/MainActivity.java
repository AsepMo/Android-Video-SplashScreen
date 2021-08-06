package com.media.video;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import com.video.evolution.application.player.VideoPlayerFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_application);
		
		Toolbar mToolbar = (Toolbar)findViewById(R.id.toolbar);
		setSupportActionBar(mToolbar);

        
        getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.content_frame, new VideoPlayerFragment())
        .commit(); 
    }


}
