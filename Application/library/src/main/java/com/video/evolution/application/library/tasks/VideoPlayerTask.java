package com.video.evolution.application.library.tasks;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.Context;
import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.MediaStore;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.io.File;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.text.SimpleDateFormat;

import com.video.evolution.R;
import com.video.evolution.application.library.models.VideoData;
import com.video.evolution.application.player.VideoPlayer;
import com.video.evolution.application.player.utils.VideoPlayerUtils;
import static com.video.evolution.application.player.VideoPlayer.mPlaylist;

public class VideoPlayerTask extends AsyncTask<Void, Void, ArrayList<VideoData>> {

    private Context mContext;
    private VideoData mVideoData;
    private ArrayList<VideoData> videoList;
    private int count = 0; 
    private OnVideoTaskListener mOnVideoTaskListener;
    public VideoPlayerTask(Context context, ArrayList<VideoData> videoList) {
        this.mContext = context; 
        this.videoList = videoList;
        mVideoData = new VideoData(context, VideoData.FILENAME);  
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute(); 
        if(mOnVideoTaskListener != null){
            mOnVideoTaskListener.onPreExecute();
        }
    }

    @Override
    protected void onProgressUpdate(Void[] values) {
        super.onProgressUpdate(values);

    }

    @Override
    protected void onCancelled() {
        super.onCancelled();

    }

    @Override
    protected void onCancelled(ArrayList<VideoData> result) {
        super.onCancelled(result);
    }


    @Override
    protected ArrayList<VideoData> doInBackground(Void[] params) {
        videoList = new ArrayList<VideoData>();
        ContentResolver contentResolver = mContext.getContentResolver();
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

        Cursor cursor = contentResolver.query(uri, null, null, null, null);

        //looping through all rows and adding to list
        if (cursor != null && cursor.moveToFirst()) {
            do {
                count++;
                String id = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media._ID));                
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE));
                String thumbnail = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Thumbnails.DATA));
                //String size = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.SIZE));
                String duration = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DURATION));
                String data = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
                SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy  hh:mm a");
                File file = new File(data);
                String date = format.format(file.lastModified());
                Integer number = (count);
                VideoData videoData  = new VideoData(mContext);
                videoData.setId(number);
                videoData.setVideoId(id);
                videoData.setVideoTitle(title);
                videoData.setVideoUri(Uri.parse(data));
                videoData.setVideoPath(data);
                videoData.setVideoThumbnail(thumbnail);
                videoData.setVideoSize(VideoPlayerUtils.formatFileSize(new File(data)));
                videoData.setVideoDuration(VideoPlayerUtils.timeConversion(Long.parseLong(duration)));
                videoData.setVideoDate(date);
                videoList.add(videoData);

                try {
                    videoData.saveToFile(videoList);

                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                    if(mOnVideoTaskListener != null){
                        mOnVideoTaskListener.onFailed();
                    }
                }
            } while (cursor.moveToNext());
        }
        return videoList;
    }

    @Override
    protected void onPostExecute(ArrayList<VideoData> result) {
        super.onPostExecute(result);
        if (result.size() < 1) {
            if(mOnVideoTaskListener != null){
                mOnVideoTaskListener.isEmpty();
            }
        } else {
            if(mOnVideoTaskListener != null){
                mOnVideoTaskListener.onSuccess(result);
            }
        }
    }

    public void setOnVideoTaskListener(OnVideoTaskListener mOnVideoTaskListener){
        this.mOnVideoTaskListener = mOnVideoTaskListener;
    }

    public interface OnVideoTaskListener{
        void onPreExecute();
        void onSuccess(ArrayList<VideoData> result);
        void onFailed();
        void isEmpty();
    }
}
