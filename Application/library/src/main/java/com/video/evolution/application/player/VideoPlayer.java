package com.video.evolution.application.player;

import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.IntRange;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.AttributeSet;
import android.util.Log;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.widget.VideoView;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.ArrayList;

import com.video.evolution.R;
import com.video.evolution.application.library.VideoInfo;
import com.video.evolution.application.library.models.VideoData;
import com.video.evolution.application.player.models.ActionItem;
import com.video.evolution.application.player.utils.VideoPlayerUtils;
import com.video.evolution.application.library.tasks.VideoPlayerTask;

public class VideoPlayer extends RelativeLayout implements MediaPlayer.OnErrorListener, View.OnClickListener {

    private static final int MESSAGE_HIDE_CENTER_BOX = 4;
    private static final int MESSAGE_SEEK_NEW_POSITION = 3;
    private static final int HIDE_TIME = 5000;

    private static final int ID_VIDEO_INFO = 1;
    private static final int ID_CHANNEL_TV = 2;
    private static final int ID_VIDEO_LIBRARY = 3;
    private static final int ID_VIDEO_FOLDER = 4;
    private static final int ID_VIDEO_STREAM = 5;
    private static final int ID_VIDEO_PANORAMA = 6;

    private Activity mActivity;
    private Context mContext;
    private View mVideoFrame;
    private VideoView mVideo;
    private VideoData mVideoData;
    private View mTopView;
    private View mCenterView;
    private View mBottomView;
    private View mProgressView;
    private SeekBar mSeekBar;

    private MenuAction mMenuAction;

    private ImageButton mPrevButton;
    private ImageButton mPlayButton;
    private ImageButton mNextButton;
    private ImageView mProgress;

    private TextView mPlayTime;
    private TextView mDurationTime;
    private AudioManager mAudioManager;
    private RelativeLayout app_video_player_controll;
    private LinearLayout app_video_volume_box, app_video_brightness_box, app_video_fastForward_box;
    private ImageView app_video_volume_icon;
    private TextView app_video_volume, app_video_brightness, app_video_fastForward, app_video_fastForward_target, app_video_fastForward_all;
    private int mMaxVolume;
    private int screenWidthPixels;
    private int volume = -1;
    private long newPosition = -1;
    private float brightness = -1;
    private String video_title;
    private String video_thumbnail;
    private String video_url;
    private String video_duration;
    private Uri video_uri;
    private Integer video_index = 0;
    private Integer shortAnimDuration;
    private boolean isOrientation = true;

    public static ArrayList<VideoData> mPlaylist = null;
    private OnVideoPlayerListener mOnVideoPlayerListener;
    private OnMenuClickListener mOnMenuClickListener;

    public static final String VIDEO_CACHE_NAME = "X4gPz_110559_60765d771b815d6faadf2f978fb8fcfe_ori.mp4";
    public static final String VIDEO_URL = "http://stream-1.vdomax.com:1935/vod/__definst__/mp4:youlove/youlove_xxx_7043.mp4/playlist.m3u8";
    public static final String VIDEO_DRIVE_URL = "https://drive.google.com/uc?export=view&id=1LFcPJLWFyeGQdkR6z-nf1OzYGmSdw5d7";
    public VideoPlayer(Context context) {
        super(context);
        init(context, null);
    }

    public VideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public VideoPlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @SuppressWarnings("unused")
    public VideoPlayer(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mContext = context;
        setBackgroundColor(Color.TRANSPARENT);  
        mActivity = (Activity)context;   
        shortAnimDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        setKeepScreenOn(true);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        mVideoFrame = inflater.inflate(R.layout.video_player_layout, this, false);
        mVideoFrame.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        addView(mVideoFrame);

        mMenuAction = (MenuAction) mVideoFrame.findViewById(R.id.menu_action);
        mMenuAction.setOnMenuClickListener(new MenuAction.OnMenuButtonClickListener(){
                @Override
                public void onNavigationIcon(View v) {
                    if (mOnVideoPlayerListener != null) {
                        mOnVideoPlayerListener.onNavigationIconClick(v);
                    }    
                }

                @Override
                public void onMenuLibraryClick(View v) {
                    if (mOnVideoPlayerListener != null) {
                        mOnVideoPlayerListener.onVideoPlaylist(v);
                    }    
                }
                @Override
                public void onMenuScreenOrientation(View v) {
                    if (mOnVideoPlayerListener != null) {
                        mOnVideoPlayerListener.onVideoOrientation(v);
                    }    
                }        
            });
        ActionItem videoInfoItem      = new ActionItem(ID_VIDEO_INFO, "Info", getResources().getDrawable(R.drawable.ic_information_outline));
        ActionItem channelTvItem   = new ActionItem(ID_CHANNEL_TV, "ChannelTv", getResources().getDrawable(R.drawable.ic_youtube_tv));
        ActionItem videoLibraryItem   = new ActionItem(ID_VIDEO_LIBRARY, "Library", getResources().getDrawable(R.drawable.ic_library_video));
        ActionItem videoFolderItem   = new ActionItem(ID_VIDEO_FOLDER, "Folder", getResources().getDrawable(R.drawable.ic_folder));
        ActionItem videoStreamItem   = new ActionItem(ID_VIDEO_STREAM, "Streaming", getResources().getDrawable(R.drawable.ic_access_point));
        ActionItem videoPanoramaItem   = new ActionItem(ID_VIDEO_PANORAMA, "Panorama", getResources().getDrawable(R.drawable.ic_video_panorama));

        mMenuAction.addActionItem(videoInfoItem);
        mMenuAction.addActionItem(channelTvItem);
        mMenuAction.addActionItem(videoLibraryItem);
        mMenuAction.addActionItem(videoFolderItem);
        mMenuAction.addActionItem(videoStreamItem);
        mMenuAction.addActionItem(videoPanoramaItem);

        mMenuAction.setOnActionItemClickListener(new MenuAction.OnActionItemClickListener() {
                @Override
                public void onItemClick(MenuAction quickAction, int pos, int actionId) {
                    ActionItem actionItem = quickAction.getActionItem(pos);

                    if (actionId == ID_VIDEO_INFO) {
                        if (mOnMenuClickListener != null) {
                            mOnMenuClickListener.onVideoInfo();
                        }
                    } else if (actionId == ID_CHANNEL_TV) {
                        if (mOnMenuClickListener != null) {
                            mOnMenuClickListener.onChannel();
                        }
                    } else if (actionId == ID_VIDEO_LIBRARY) {
                        if (mOnMenuClickListener != null) {
                            mOnMenuClickListener.onVideoLibrary();
                        }
                    } else if (actionId == ID_VIDEO_FOLDER) {
                        if (mOnMenuClickListener != null) {
                            mOnMenuClickListener.onVideoFolder();
                        }
                    } else if (actionId == ID_VIDEO_STREAM) {
                        if (mOnMenuClickListener != null) {
                            mOnMenuClickListener.onVideoStreamming();
                        }
                    } 

                    Toast.makeText(mContext, actionItem.getTitle() + " selected", Toast.LENGTH_SHORT).show();
                }
            });

        mVideo = (VideoView) mVideoFrame.findViewById(R.id.videoview);
        //mVideoView.setOnPreparedListener(this);
        mVideo.setOnErrorListener(this);
        mProgress = (ImageView) mVideoFrame.findViewById(R.id.hive_progress);
        VideoPlayerUtils.setProgressVisibility(mProgress, true);
        
        mPlayTime = (TextView) mVideoFrame.findViewById(R.id.play_time);
        mDurationTime = (TextView) mVideoFrame.findViewById(R.id.total_time);
        mPrevButton = (ImageButton) mVideoFrame.findViewById(R.id.btnPrev);
        mPlayButton = (ImageButton) mVideoFrame.findViewById(R.id.btnPlayandPause);
        mNextButton = (ImageButton) mVideoFrame.findViewById(R.id.btnNext);
        mSeekBar = (SeekBar) mVideoFrame.findViewById(R.id.seekbar);
        mTopView = mVideoFrame.findViewById(R.id.app_video_top_box);
        mCenterView = mVideoFrame.findViewById(R.id.app_video_player_control);
        mBottomView = mVideoFrame.findViewById(R.id.app_video_bottom_box);
        mProgressView = mVideoFrame.findViewById(R.id.app_video_progress_bar);
        crossFade(mCenterView, mProgressView);
        app_video_player_controll = (RelativeLayout) mVideoFrame.findViewById(R.id.app_video_player_control);
        app_video_volume_box = (LinearLayout) mVideoFrame.findViewById(R.id.app_video_volume_box);
        app_video_brightness_box = (LinearLayout) mVideoFrame.findViewById(R.id.app_video_brightness_box);
        app_video_fastForward_box = (LinearLayout) mVideoFrame.findViewById(R.id.app_video_fastForward_box);
        app_video_volume_icon = (ImageView) mVideoFrame.findViewById(R.id.app_video_volume_icon);
        app_video_volume = (TextView) mVideoFrame.findViewById(R.id.app_video_volume);
        app_video_brightness = (TextView) mVideoFrame.findViewById(R.id.app_video_brightness);
        app_video_fastForward = (TextView) mVideoFrame.findViewById(R.id.app_video_fastForward);
        app_video_fastForward_target = (TextView) mVideoFrame.findViewById(R.id.app_video_fastForward_target);
        app_video_fastForward_all = (TextView) mVideoFrame.findViewById(R.id.app_video_fastForward_all);

        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mPrevButton.setOnClickListener(this);
        mPlayButton.setOnClickListener(this);
        mNextButton.setOnClickListener(this);

        mSeekBar.setOnSeekBarChangeListener(mSeekBarChangeListener);
        screenWidthPixels = getResources().getDisplayMetrics().widthPixels;

        mVideoData = new VideoData(mContext, VideoData.FILENAME);       
        mPlaylist = getLocallyStoredData();

        setWindowInsets();
    }

    private SeekBar.OnSeekBarChangeListener mSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mHandler.postDelayed(hideRunnable, HIDE_TIME);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            mHandler.removeCallbacks(hideRunnable);
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                int time = progress * mVideo.getDuration() / 100;
                mVideo.seekTo(time);
            }
        }
    };

    private void onVolumeSlide(float percent) {
        if (volume == -1) {
            volume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            if (volume < 0)
                volume = 0;
        }
        int index = (int) (percent * mMaxVolume) + volume;
        if (index > mMaxVolume)
            index = mMaxVolume;
        else if (index < 0)
            index = 0;
        // 变更声音
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);
        // 变更进度条
        int i = (int) (index * 1.0 / mMaxVolume * 100);
        String s = i + "%";
        if (i == 0) {
            s = "off";
        }
        // 显示
        if (i == 0) app_video_volume_icon.setImageResource(R.drawable.ic_volume_off_white_36dp);
        else app_video_volume_icon.setImageResource(R.drawable.ic_volume_up_white_36dp);
        app_video_player_controll.setVisibility(View.GONE);
        app_video_brightness_box.setVisibility(View.GONE);
        app_video_volume_box.setVisibility(View.VISIBLE);
        app_video_volume.setText(s);
    }

    public void setVideoTitle(String title) {
        video_title = title;
    }

    public String getVideoTitle() {
        return video_title;
    }

    public void setVideoThumbnail(String thumb) {
        video_thumbnail = thumb;
    }

    public String getVideoThumbnail() {
        return video_thumbnail;
    }

    public void setVideoDuration(String duration) {
        video_duration = duration;
    }

    public String getVideoDuration() {
        return video_duration;
    }

    public int getCurrentPosition() {
        if (mVideo == null) return -1;
        return mVideo.getCurrentPosition();
    }


    public int getDuration() {
        if (mVideo == null) return -1;
        return mVideo.getDuration();
    }

    public void seekTo(@IntRange(from = 0, to = Integer.MAX_VALUE) int pos) {
        if (mVideo == null) {
            return;
        }
        mVideo.seekTo(pos);
    }

    public void setVideoPath(String path) {  
        video_url = path;
        video_uri = Uri.parse(path);
        playVideo(video_uri);
    }

    public String getVideoPath() {
        return video_url;
    }

    public void setVideoUrl(String url) {
        this.video_url = url;
    }

    public void setVideoURI(Uri uri) {    
        video_uri = uri;
        playVideo(video_uri);
    }

    private void playVideo(Uri uri) {
        File file = new File(uri.getPath());
        if (!file.exists()) {
            Toast.makeText(mActivity, "File Tersedia", Toast.LENGTH_LONG).show();
            return;
        }
        try { 
            mMenuAction.setVideoTitle(file.getName());
            mVideo.setVideoURI(uri);
            mPlayButton.setImageResource(R.drawable.ic_pause_circle_white_36dp);
            mVideo.requestFocus();
            mVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        //mVideo.setVideoWidth(mp.getVideoWidth());
                        //mVideo.setVideoHeight(mp.getVideoHeight());
                        crossFade(mProgressView, mCenterView);
                        VideoPlayerUtils.setProgressVisibility(mProgress, true);
                        
                        mHandler.removeCallbacks(hideRunnable);
                        mHandler.postDelayed(hideRunnable, HIDE_TIME);
                        mDurationTime.setText(formatTime(mVideo.getDuration()));
                        Timer timer = new Timer();
                        timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    mHandler.sendEmptyMessage(1);
                                }
                            }, 0, 1000);
                        if (mOnVideoPlayerListener != null) {
                            mOnVideoPlayerListener.onVideoPlaying(VideoPlayer.this, getVideoTitle(), getVideoThumbnail(), getVideoPath());
                        }      
                    }
                });
            mVideo.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {                  
                        if (mOnVideoPlayerListener != null) {
                            mOnVideoPlayerListener.onVideoComplete(VideoPlayer.this);
                        }    
                    }
                });

            final GestureDetector gestureDetector = new GestureDetector(mActivity, new PlayerGestureListener());
            View liveBox = findViewById(R.id.app_video_box);
            liveBox.setClickable(true);
            liveBox.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        if (gestureDetector.onTouchEvent(motionEvent))
                            return true;
                        // 处理手势结束
                        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                            case MotionEvent.ACTION_UP:
                                endGesture();
                                break;
                        }
                        return false;
                    }
                });
            mVideo.start();     
            mPlayTime.setTextColor(Color.WHITE);
            mDurationTime.setTextColor(Color.WHITE);

            mPlayButton.setImageResource(R.drawable.ic_pause_circle_white_36dp);
            mPrevButton.setVisibility(View.GONE);
            mNextButton.setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // setVideo file
    public void setPlaylist(ArrayList<VideoData> mPlaylist, final Integer video_index) {

        this.mPlaylist = mPlaylist;
        this.video_index = video_index;
        playVideo(video_index);   
    }

    public void getPlaylist() {
        
        setVideoTask();
    }
    
    public void getVideoPlaylist(){
        File file = new File(VideoData.getPath());
        if (!file.exists()) {
            Toast.makeText(mActivity, "File Tersedia", Toast.LENGTH_LONG).show();
            return;
        }
        try { 
            String videoName = FilenameUtils.getName(file.getAbsolutePath());
            VideoInfo.setVideoPath(file.getAbsolutePath());
            mMenuAction.setVideoTitle(videoName);  
            mVideo.setVideoURI(Uri.fromFile(file));
            mPlayButton.setImageResource(R.drawable.ic_pause_circle_white_36dp);
            mVideo.requestFocus();
            mVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        //mVideo.setVideoWidth(mp.getVideoWidth());
                        //mVideo.setVideoHeight(mp.getVideoHeight());
                        crossFade(mProgressView, mCenterView);
                        mHandler.removeCallbacks(hideRunnable);
                        mHandler.postDelayed(hideRunnable, HIDE_TIME);
                        mDurationTime.setText(formatTime(mVideo.getDuration()));
                        Timer timer = new Timer();
                        timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    mHandler.sendEmptyMessage(1);
                                }
                            }, 0, 1000);
                        if (mOnVideoPlayerListener != null) {
                            mOnVideoPlayerListener.onVideoPlaying(VideoPlayer.this, getVideoTitle(), getVideoThumbnail(), getVideoPath());
                        }      
                    }
                });
            mVideo.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        video_index++;
                        if (video_index < (mPlaylist.size())) {
                            playVideo(video_index);
                        } else {
                            video_index = 0;
                            playVideo(video_index);
                        }
                    }
                });

            final GestureDetector gestureDetector = new GestureDetector(mActivity, new PlayerGestureListener());
            View liveBox = findViewById(R.id.app_video_box);
            liveBox.setClickable(true);
            liveBox.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        if (gestureDetector.onTouchEvent(motionEvent))
                            return true;
                        // 处理手势结束
                        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                            case MotionEvent.ACTION_UP:
                                endGesture();
                                break;
                        }
                        return false;
                    }
                });
            mVideo.start();     
            mPlayTime.setTextColor(Color.WHITE);
            mDurationTime.setTextColor(Color.WHITE);

            mPlayButton.setImageResource(R.drawable.ic_pause_circle_white_36dp);
            mPrevButton.setVisibility(View.VISIBLE);
            mNextButton.setVisibility(View.VISIBLE);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // play video file
    public void playVideo(int pos) {
        video_title = mPlaylist.get(pos).getVideoTitle();
        video_url = mPlaylist.get(pos).getVideoPath();
        video_thumbnail = mPlaylist.get(pos).getVideoThumbnail();
        video_duration = mPlaylist.get(pos).getVideoDuration();
        setVideoTitle(video_title);
        setVideoThumbnail(video_thumbnail);
        setVideoDuration(video_duration);
        setVideoUrl(video_url);

        //VideoPlayerUtils.setOrientation(mActivity, getVideoPath());   

        try  {
            VideoInfo.setVideoPath(getVideoPath());
            mMenuAction.setVideoTitle(getVideoTitle());  
            mVideo.setVideoURI(Uri.parse(getVideoPath()));
            mVideo.requestFocus();
            mVideo.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        //mVideo.setVideoWidth(mp.getVideoWidth());
                        //mVideo.setVideoHeight(mp.getVideoHeight());
                        crossFade(mProgressView, mCenterView);
                        mHandler.removeCallbacks(hideRunnable);
                        mHandler.postDelayed(hideRunnable, HIDE_TIME);
                        mDurationTime.setText(formatTime(mVideo.getDuration()));
                        Timer timer = new Timer();
                        timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    mHandler.sendEmptyMessage(1);
                                }
                            }, 0, 1000);
                        if (mOnVideoPlayerListener != null) {
                            mOnVideoPlayerListener.onVideoPlaying(VideoPlayer.this, getVideoTitle(), getVideoThumbnail(), getVideoPath());
                        }    
                    }
                });
            mVideo.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        video_index++;
                        if (video_index < (mPlaylist.size())) {
                            playVideo(video_index);
                        } else {
                            video_index = 0;
                            playVideo(video_index);
                        }
                    }
                });
            final GestureDetector gestureDetector = new GestureDetector(mActivity, new PlayerGestureListener());
            View liveBox = findViewById(R.id.app_video_box);
            liveBox.setClickable(true);
            liveBox.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        if (gestureDetector.onTouchEvent(motionEvent))
                            return true;
                        // 处理手势结束
                        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                            case MotionEvent.ACTION_UP:
                                endGesture();
                                break;
                        }
                        return false;
                    }
                });

            mVideo.start();     
            mPlayTime.setTextColor(Color.WHITE);
            mDurationTime.setTextColor(Color.WHITE);

            mPlayButton.setImageResource(R.drawable.ic_pause_circle_white_36dp);
            mPrevButton.setVisibility(View.VISIBLE);
            mNextButton.setVisibility(View.VISIBLE);

            video_index = pos;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setVideoTask() {
        VideoPlayerTask task = new VideoPlayerTask(mContext, mPlaylist);
        task.setOnVideoTaskListener(new VideoPlayerTask.OnVideoTaskListener(){
                @Override
                public void onPreExecute() {
                    crossFade(mCenterView, mProgressView);
                }
                @Override
                public void onSuccess(ArrayList<VideoData> result) {
                    crossFade(mProgressView, mCenterView);                    
                    setPlaylist(mPlaylist, video_index);
                }

                @Override
                public void onFailed() {
                    crossFade(mProgressView, mCenterView);
                } 
                @Override
                public void isEmpty() {
                    crossFade(mProgressView, mCenterView);
                } 
            });
        task.execute();  
    }

    public void setOrientation(boolean isOrientation) {
        this.isOrientation = isOrientation;
    }

    // play video file
    public void pauseVideo() {
        mHandler.removeCallbacks(hideRunnable);
        mVideo.start();
        mVideo.pause();
        mVideo.seekTo(0);
        mPlayTime.setText("00:00");
        mSeekBar.setProgress(0);
        mSeekBar.setSecondaryProgress(0);
        mPlayButton.setImageResource(R.drawable.ic_play_circle_white_36dp);   
        crossFade(mCenterView, mProgressView);
        mTopView.setVisibility(View.VISIBLE);
        mCenterView.setVisibility(View.VISIBLE);
        mBottomView.setVisibility(View.VISIBLE);
        mHandler.postDelayed(hideRunnable, HIDE_TIME);
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {

        if (what == -38) {
            // Error code -38 happens on some Samsung devices
            // Just ignore it
            return false;
        }
        String errorMsg = "Preparation/playback error (" + what + "): ";
        switch (what) {
            default:
                errorMsg += "Unknown error";
                break;
            case MediaPlayer.MEDIA_ERROR_IO:
                errorMsg += "I/O error";
                break;
            case MediaPlayer.MEDIA_ERROR_MALFORMED:
                errorMsg += "Malformed";
                break;
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                errorMsg += "Not valid for progressive playback";
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                errorMsg += "Server died";
                break;
            case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
                errorMsg += "Timed out";
                break;
            case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                errorMsg += "Unsupported";
                break;
        }

        throwError(new Exception(errorMsg));
        return false;
    }

    public ArrayList<VideoData> getLocallyStoredData() {
        ArrayList<VideoData> items = null;
        try {
            items = mVideoData.loadFromFile();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        if (items == null) {
            items = new ArrayList<>();
        }
        return items;
    }

    private void onBrightnessSlide(float percent) {
        if (brightness < 0) {
            brightness = mActivity.getWindow().getAttributes().screenBrightness;
            if (brightness <= 0.00f) {
                brightness = 0.50f;
            } else if (brightness < 0.01f) {
                brightness = 0.01f;
            }
        }
        Log.d(this.getClass().getSimpleName(), "brightness:" + brightness + ",percent:" + percent);
        app_video_brightness_box.setVisibility(View.VISIBLE);
        app_video_player_controll.setVisibility(View.GONE);
        WindowManager.LayoutParams lpa = mActivity.getWindow().getAttributes();
        lpa.screenBrightness = brightness + percent;
        if (lpa.screenBrightness > 1.0f) {
            lpa.screenBrightness = 1.0f;
        } else if (lpa.screenBrightness < 0.01f) {
            lpa.screenBrightness = 0.01f;
        }
        Log.i("Sunmeng", "lpa.screenBrightness : " + lpa.screenBrightness * 100);
        app_video_brightness.setText(((int) (lpa.screenBrightness * 100)) + "%");
        mActivity.getWindow().setAttributes(lpa);
    }

    private class PlayerGestureListener extends GestureDetector.SimpleOnGestureListener {
        private boolean firstTouch;
        private boolean volumeControl;
        private boolean toSeek;

        /**
         * 双击
         */
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            firstTouch = true;
            return super.onDown(e);
        }

        /**
         * 滑动
         */
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            float mOldX = e1.getX(), mOldY = e1.getY();
            float deltaY = mOldY - e2.getY();
            float deltaX = mOldX - e2.getX();
            if (firstTouch) {
                toSeek = Math.abs(distanceX) >= Math.abs(distanceY);
                volumeControl = mOldX > screenWidthPixels * 0.5f;
                firstTouch = false;
            }
            if (toSeek) {
                onProgressSlide(-deltaX / mVideo.getWidth());
            } else {
                float percent = deltaY / mVideo.getHeight();
                if (volumeControl) {
                    onVolumeSlide(percent);
                } else {
                    onBrightnessSlide(percent);
                }
            }
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            showOrHide();
            return true;
        }
    }

    private void endGesture() {
        volume = -1;
        brightness = -1f;
        if (newPosition >= 0) {
            mHandler.removeMessages(MESSAGE_SEEK_NEW_POSITION);
            mHandler.sendEmptyMessage(MESSAGE_SEEK_NEW_POSITION);
        }
        mHandler.removeMessages(MESSAGE_HIDE_CENTER_BOX);
        mHandler.sendEmptyMessageDelayed(MESSAGE_HIDE_CENTER_BOX, 500);
    }

    private void onProgressSlide(float percent) {
        long position = mVideo.getCurrentPosition();
        long duration = mVideo.getDuration();
        long deltaMax = Math.min(100 * 1000, duration - position);
        long delta = (long) (deltaMax * percent);
        newPosition = delta + position;
        if (newPosition > duration) {
            newPosition = duration;
        } else if (newPosition <= 0) {
            newPosition = 0;
            delta = -position;
        }
        int showDelta = (int) delta / 1000;
        if (showDelta != 0) {
            app_video_player_controll.setVisibility(View.GONE);
            app_video_fastForward_box.setVisibility(View.VISIBLE);
            String text = showDelta > 0 ? ("+" + showDelta) : "" + showDelta;
            app_video_fastForward.setText(text + "s");
            app_video_fastForward_target.setText(generateTime(newPosition) + "/");
            app_video_fastForward_all.setText(generateTime(duration));
        }
    }

    private String generateTime(long time) {
        int totalSeconds = (int) (time / 1000);
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        return hours > 0 ? String.format("%02d:%02d:%02d", hours, minutes, seconds) : String.format("%02d:%02d", minutes, seconds);
    }

    private Runnable hideRunnable = new Runnable() {

        @Override
        public void run() {
            showOrHide();
        }
    };

    @SuppressLint("SimpleDateFormat")
    private String formatTime(long time) {
        DateFormat formatter = new SimpleDateFormat("mm:ss");
        return formatter.format(new Date(time));
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.btnPrev) {
            if (video_index > 0) {
                video_index--;
                playVideo(video_index);              
            } else {
                video_index = mPlaylist.size() - 1;
                playVideo(video_index);
            }

        }
        if (v.getId() == R.id.btnPlayandPause) {
            if (mVideo.isPlaying()) {
                mVideo.pause();
                mPlayButton.setImageResource(R.drawable.ic_play_circle_white_36dp);
            } else {
                mVideo.start();
                mPlayButton.setImageResource(R.drawable.ic_pause_circle_white_36dp);
            }
        }  
        if (v.getId() == R.id.btnNext) {
            if (video_index < (mPlaylist.size() - 1)) {
                video_index++;
                playVideo(video_index);
            } else {
                video_index = 0;
                playVideo(video_index);
            }
        }  
    }

    private void showOrHide() {
        if (mTopView.getVisibility() == View.VISIBLE) {
            mTopView.clearAnimation();
            Animation animation = AnimationUtils.loadAnimation(mActivity, R.anim.option_leave_from_top);
            animation.setAnimationListener(new AnimationImp() {
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        super.onAnimationEnd(animation);
                        mTopView.setVisibility(View.GONE);
                    }
                });
            mTopView.startAnimation(animation);
            mCenterView.setVisibility(View.GONE);
            mBottomView.clearAnimation();
            Animation animation1 = AnimationUtils.loadAnimation(mActivity, R.anim.option_leave_from_bottom);
            animation1.setAnimationListener(new AnimationImp() {
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        super.onAnimationEnd(animation);
                        mBottomView.setVisibility(View.GONE);
                    }
                });
            mBottomView.startAnimation(animation1);
        } else {
            mTopView.setVisibility(View.VISIBLE);
            mTopView.clearAnimation();
            Animation animation = AnimationUtils.loadAnimation(mActivity, R.anim.option_entry_from_top);
            mTopView.startAnimation(animation);
            mCenterView.setVisibility(View.VISIBLE);
            mBottomView.setVisibility(View.VISIBLE);
            mBottomView.clearAnimation();
            Animation animation1 = AnimationUtils.loadAnimation(mActivity, R.anim.option_entry_from_bottom);
            mBottomView.startAnimation(animation1);
            mHandler.removeCallbacks(hideRunnable);
            mHandler.postDelayed(hideRunnable, HIDE_TIME);
        }
    }

    private class AnimationImp implements Animation.AnimationListener {

        @Override
        public void onAnimationEnd(Animation animation) {

        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationStart(Animation animation) {
        }
    }

    public void getScreenOrientation(final Activity activity) {
        boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;   
        if (isPortrait) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);    

            isPortrait = false;
        } else {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);  

            isPortrait = true;
        }
    }

    private int getScreenOrientation() {
        int rotation = mActivity.getWindowManager().getDefaultDisplay().getRotation();
        DisplayMetrics dm = new DisplayMetrics();
        mActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        int orientation;
        // if the device's natural orientation is portrait:
        if ((rotation == Surface.ROTATION_0
            || rotation == Surface.ROTATION_180) && height > width ||
            (rotation == Surface.ROTATION_90
            || rotation == Surface.ROTATION_270) && width > height) {
            switch (rotation) {
                case Surface.ROTATION_0:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
                case Surface.ROTATION_90:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
                case Surface.ROTATION_180:
                    orientation =
                        ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    break;
                case Surface.ROTATION_270:
                    orientation =
                        ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
                default:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
            }
        } else {
            switch (rotation) {
                case Surface.ROTATION_0:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
                case Surface.ROTATION_90:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
                case Surface.ROTATION_180:
                    orientation =
                        ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
                case Surface.ROTATION_270:
                    orientation =
                        ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    break;
                default:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
            }
        }

        return orientation;
    }

    public void toggleFullScreen() {
        if (getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        updateFullScreenButton();
    }

    private void updateFullScreenButton() {
        if (getScreenOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            //app_video_fullscreen.setImageResource(R.drawable.ic_fullscreen_exit);
        } else {
            //app_video_fullscreen.setImageResource(R.drawable.ic_fullscreen);
        }
    }

    private void throwError(Exception e) {
        if (mOnVideoPlayerListener != null)
            mOnVideoPlayerListener.onVideoError(this, e);
        else
            throw new RuntimeException(e);
    }

    public void onDestroy() {       
        mHandler.removeMessages(0);
        mHandler.removeCallbacksAndMessages(null);
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    if (mVideo.getCurrentPosition() > 0) {
                        mPlayTime.setText(formatTime(mVideo.getCurrentPosition()));
                        int progress = mVideo.getCurrentPosition() * 100 / mVideo.getDuration();
                        mSeekBar.setProgress(progress);
                        if (mVideo.getCurrentPosition() > mVideo.getDuration() - 100) {
                            mPlayTime.setText("00:00");
                            mSeekBar.setProgress(0);
                        }
                        mSeekBar.setSecondaryProgress(mVideo.getBufferPercentage());
                    } else {
                        mPlayTime.setText("00:00");
                        mSeekBar.setProgress(0);
                    }

                    break;
                case 2:
                    showOrHide();
                    break;
                case MESSAGE_HIDE_CENTER_BOX:
                    //隐藏调节操作悬浮框
                    app_video_volume_box.setVisibility(View.GONE);
                    app_video_brightness_box.setVisibility(View.GONE);
                    app_video_fastForward_box.setVisibility(View.GONE);
                    break;
                case MESSAGE_SEEK_NEW_POSITION:
                    if (newPosition >= 0) {
                        mVideo.seekTo((int) newPosition);
                        newPosition = -1;
                    }
                    break;
                default:
                    break;
            }
        }
    };


    public void onConfigurationChanged(Configuration newConfig) {
        setWindowInsets();
    }

    public void setWindowInsets() {
        final View mTopBar = findViewById(R.id.app_video_top_box);
        final View mBottombar= findViewById(R.id.app_video_bottom_box);
        final ViewGroup rootView = findViewById(R.id.app_video_box);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            rootView.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                    @Override
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)
                    public WindowInsets onApplyWindowInsets(View view, WindowInsets insets) {

                        mTopBar.setPadding(insets.getSystemWindowInsetLeft(),
                                           insets.getSystemWindowInsetTop(),
                                           insets.getSystemWindowInsetRight(), 0);

                        mBottombar.setPadding(insets.getSystemWindowInsetLeft(),
                                              0, insets.getSystemWindowInsetRight(),
                                              insets.getSystemWindowInsetBottom());

                        // clear this listener so insets aren't re-applied
                        rootView.setOnApplyWindowInsetsListener(null);
                        return insets.consumeSystemWindowInsets();
                    }
                });
        } else {
            rootView.getViewTreeObserver()
                .addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        //hacky way of getting window insets on pre-Lollipop
                        int[] screenSize = getScreenSize(mActivity);

                        int[] windowInsets = new int[]{
                            Math.abs(screenSize[0] - rootView.getLeft()),
                            Math.abs(screenSize[1] - rootView.getTop()),
                            Math.abs(screenSize[2] - rootView.getRight()),
                            Math.abs(screenSize[3] - rootView.getBottom())};

                        mTopBar.setPadding(windowInsets[0], windowInsets[1],
                                           windowInsets[2], 0);

                        mBottombar.setPadding(windowInsets[0], 0,
                                              windowInsets[2], windowInsets[3]);

                        rootView.getViewTreeObserver()
                            .removeOnGlobalLayoutListener(this);
                    }
                });
        }
    }

    public void showOrHideSystemUi(boolean show) {
        if (show) {
            mActivity.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE);
        } else {
            mActivity.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                | View.SYSTEM_UI_FLAG_IMMERSIVE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    //int[left, top, right, bottom]
    public static int[] getScreenSize(Activity context) {
        Rect displayRect = new Rect();
        context.getWindow().getDecorView().getWindowVisibleDisplayFrame(displayRect);
        return new int[]{
            displayRect.left, displayRect.top,
            displayRect.right, displayRect.bottom};
    }

    public void crossFade(final View toHide, View toShow) {

        toShow.setAlpha(0.0f);
        toShow.setVisibility(View.VISIBLE);

        toShow.animate()
            .alpha(1.0f)
            .setDuration(shortAnimDuration)
            .setListener(null);

        toHide.animate()
            .alpha(0.0f)
            .setDuration(shortAnimDuration)
            .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    toHide.setVisibility(View.GONE);
                }
            });
    }

    public void setOnVideoPlayerListener(OnVideoPlayerListener mOnVideoPlayerListener) {
        this.mOnVideoPlayerListener = mOnVideoPlayerListener;
    }

    public void setOnMenuClickListener(OnMenuClickListener mOnMenuClickListener) {
        this.mOnMenuClickListener = mOnMenuClickListener;
    }

    public interface OnMenuClickListener {
        void onVideoInfo();
        void onChannel();
        void onVideoLibrary();
        void onVideoFolder();
        void onVideoStreamming();
    }

    public interface OnVideoPlayerListener {
        void onNavigationIconClick(View v);
        void onVideoPlaying(VideoPlayer mVideoPlayer, String title, String thumbnail, String path);
        void onVideoOrientation(View v);
        void onVideoPlaylist(View v);
        void onVideoComplete(VideoPlayer mVideoPlayer);
        void onVideoError(VideoPlayer player, Exception e);
    }
}
