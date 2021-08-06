package com.video.evolution.application.player.utils;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaMetadataRetriever;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class VideoPlayerUtils {
    public final static int 
    KILOBYTE = 1024,
    MEGABYTE = KILOBYTE * 1024,
    GIGABYTE = MEGABYTE * 1024,
    MAX_BYTE_SIZE = KILOBYTE / 2,
    MAX_KILOBYTE_SIZE = MEGABYTE / 2,
    MAX_MEGABYTE_SIZE = GIGABYTE / 2;

    public static void setOrientation(Activity mActivity, String url) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(url);
        String width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
        String height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
        String resolusi = width + "x" + height;
        if (resolusi.equals("330x480")) {
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);                 
        } else if (resolusi.equals("406x720")) {
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);                 
        } else if (resolusi.equals("576x1024")) {
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);                 
        } else if (resolusi.equals("1920x1080")) {
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);                 
        } else {
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);                 
        }
    }
    
    //time conversion
    public static String timeConversion(long value) {
        String songTime;
        int dur = (int) value;
        int hrs = (dur / 3600000);
        int mns = (dur / 60000) % 60000;
        int scs = dur % 60000 / 1000;

        if (hrs > 0) {
            songTime = String.format("%02d:%02d:%02d", hrs, mns, scs);
        } else {
            songTime = String.format("%02d:%02d", mns, scs);
        }
        return songTime;
    }

    public static String getShowTime(long milliseconds) {
        /*
         // 获取日历函数
         Calendar calendar = Calendar.getInstance();
         calendar.setTimeInMillis(milliseconds);
         SimpleDateFormat dateFormat = null;
         // 判断是否大于60分钟，如果大于就显示小时。设置日期格式
         if (milliseconds / 60000 > 60) {
         dateFormat = new SimpleDateFormat("hh:mm:ss");
         } else {
         dateFormat = new SimpleDateFormat("00:mm:ss");
         }
         return dateFormat.format(calendar.getTime());
         */
        //abhiank209 (pr #10)
        return String.format("%02d:%02d:%02d",
                             TimeUnit.MILLISECONDS.toHours(milliseconds),
                             TimeUnit.MILLISECONDS.toMinutes(milliseconds) -
                             TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(milliseconds)),
                             TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
                             TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds)));
    }
    
    public static String formatFileSize(File file)
    {
        return formatFileSize(file.length());       
    }

    public static String formatFileSize(long size)
    {
        if (size < MAX_BYTE_SIZE)
            return String.format(Locale.ENGLISH, "%d bytes", size);
        else if (size < MAX_KILOBYTE_SIZE)
            return String.format(Locale.ENGLISH, "%.2f kb", (float)size / KILOBYTE);
        else if (size < MAX_MEGABYTE_SIZE)
            return String.format(Locale.ENGLISH, "%.2f mb", (float)size / MEGABYTE);
        else 
            return String.format(Locale.ENGLISH, "%.2f gb", (float)size / GIGABYTE);
    }

    public static String formatFileSize(Collection<File> files)
    {
        return formatFileSize(getFileSize(files));
    }

    public static long getFileSize(File... files)
    {
        if (files == null) return 0l;
        long size=0;
        for (File file : files)
        {
            if (file.isDirectory())
                size += getFileSize(file.listFiles());
            else size += file.length();
        }
        return size;
    }

    public static long getFileSize(Collection<File> files)
    {
        return getFileSize(files.toArray(new File[files.size()]));
    }
    
    public static void setImageBuffer(final ImageView imageView){
        final RotateAnimation GearProgressLeftAnim = new RotateAnimation(0.0f, 360.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        GearProgressLeftAnim.setRepeatCount(Animation.INFINITE);
        GearProgressLeftAnim.setDuration((long) 2 * 1500);
        GearProgressLeftAnim.setInterpolator(new LinearInterpolator());
        imageView.post(new Runnable() {
                @Override
                public void run() {
                    imageView.setAnimation(GearProgressLeftAnim);
                }
            });
    }
    
    public static void setProgressVisibility(ImageView imgBuffer, boolean Visible) {
        if (Visible) {
            imgBuffer.setVisibility(View.VISIBLE);
            setImageBuffer(imgBuffer);
        } else {
            stopImageAnim(imgBuffer);
            imgBuffer.setVisibility(View.GONE);
        }
    }
    
    public static void startImageAnim(ImageView Img, int anim) {
        Img.setVisibility(View.VISIBLE);
        try {
            Img.setImageResource(anim);
            AnimationDrawable animationDrawable = (AnimationDrawable) Img.getDrawable();
            animationDrawable.start();
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    public static void stopImageAnim(ImageView Img) {
        try {
            AnimationDrawable animationDrawable = (AnimationDrawable) Img.getDrawable();
            animationDrawable.stop();
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
        Img.setVisibility(View.GONE);
    }


    //缓冲动画控制
    public static void setBufferVisibility(ImageView imgBuffer, boolean Visible) {
        if (Visible) {
            imgBuffer.setVisibility(View.VISIBLE);
          //  startImageAnim(imgBuffer, R.drawable.loading);
        } else {
            stopImageAnim(imgBuffer);
            imgBuffer.setVisibility(View.GONE);
        }
    }
    
}
