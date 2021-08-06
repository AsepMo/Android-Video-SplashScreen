package com.video.evolution.application.folders;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import com.video.evolution.application.player.VideoPlayerFragment;

public class VideoFolder {

    public static String EXTERNAL_DIR = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static String VIDEOBOX_FOLDER = "VideoBox";

    public static String ZFOLDER = EXTERNAL_DIR + "/VideoBox";
    public static String ZFOLDER_APK = ZFOLDER + "/1.Apk";
    public static String ZFOLDER_IMG = ZFOLDER + "/2.Image";
    public static String ZFOLDER_AUDIO = ZFOLDER + "/3.Audio";
    public static String ZFOLDER_AUDIO_RECORDER = ZFOLDER_AUDIO + "/Recorder";
    public static String ZFOLDER_AUDIO_DOWNLOAD = ZFOLDER_AUDIO + "/Download";
    public static String ZFOLDER_AUDIO_CONVERT = ZFOLDER_AUDIO + "/Convert";
    public static String ZFOLDER_VIDEO = ZFOLDER + "/4.Video";
    public static String ZFOLDER_VIDEO_RECORDER = ZFOLDER_VIDEO + "/Recorder";
    public static String ZFOLDER_VIDEO_DOWNLOAD = ZFOLDER_VIDEO + "/Download";
    public static String ZFOLDER_VIDEO_CONVERTED = ZFOLDER_VIDEO + "/Trimmer";
    public static String ZFOLDER_YOUTUBE = ZFOLDER_VIDEO + "/Youtube";
    public static String ZFOLDER_YOUTUBE_ANALYTICS = ZFOLDER_YOUTUBE + "/Analytics";
    public static String ZFOLDER_YOUTUBE_DOWNLOAD = ZFOLDER_YOUTUBE + "/Download";
    public static String ZFOLDER_EBOOK = ZFOLDER + "/5.Ebook";
    public static String ZFOLDER_SCRIPTME = ZFOLDER + "/6.ScriptMe";
    public static String ZFOLDER_ARCHIVE = ZFOLDER + "/7.ZArchive";

    private static VideoFolder mInstance;
    public static void initFolder(VideoFolder engine) {
        mInstance = engine;
    }

    public static void initVideoBox(Context context) {
        initFolder(new VideoFolder.Builder(context)
                   .setDefaultFolder(VideoFolder.ZFOLDER)
                   .setFolderApk(true) 
                   .setFolderImage(true)
                   .setFolderAudio(true)                                        
                   .setFolderArchive(true)
                   .setFolderEbook(true)
                   .setFolderImage(true)
                   .setFolderVideo(true)                                       
                   .setFolderScriptMe(true)
                   .setExternalFileDir(context, VideoFolder.VIDEOBOX_FOLDER)
                   .build()); 
        
    }
    
    public static void initVideoPlayerFragment(VideoPlayerFragment videoPlayer) {
        initVideoBox(videoPlayer.getActivity().getApplicationContext());
        videoPlayer.setVideoTask();
    }
    
    public static VideoFolder get() {
        if (mInstance == null) {
            mInstance = new VideoFolder(new Builder(getApplicationContext()));
        }
        return mInstance;
    }

    private final String mFolder;
    private final boolean isFolder;
    public static Context getApplicationContext() {
        return getApplicationContext();
    }

    public static String setExternalFileDir(Context c, String folder) {
        return c.getExternalFilesDir(folder).getParentFile().mkdirs() + "/.nomedia";
    }

    public static String getExternalCacheDir(Context c) {
        return c.getExternalCacheDir().getAbsolutePath();
    }

    public static String getInternalCacheDir(Context c) {
        return c.getCacheDir().getAbsolutePath();
    }

    public static String getExternalFileDir(Context c, String folder) {
        return c.getExternalFilesDir(folder).getAbsolutePath();
    }

    public static String getInternalFileDir(Context c) {
        return c.getFilesDir().getAbsolutePath();
    }

    public static String getHomeDir(Context c) {
        return c.getFilesDir().getAbsolutePath() + "/home";
    }

    public static String getUserDir(Context c) {
        return c.getFilesDir().getAbsolutePath() + "/user";
    }

    protected VideoFolder(Builder builder) {
        mFolder = builder.mFolder;
        isFolder = builder.isFolder;
    }

    public String getFolder() {
        return mFolder;
    }

    public boolean getPath() {
        return isFolder;
    }

    public static class Builder {
        private boolean isFolder = false;
        private String mFolder = null;
        private Context mContext;

        public Builder(Context context) {
            this.mContext = context;
        }

        public Builder setDefaultFolder(String Folder) {
            File files = new File(Folder);
            if (!files.exists()) {
                files.mkdirs();
            }

            File nomedia = new File(files.getParent(), ".nomedia");
            nomedia.getParentFile().mkdirs();
            if (nomedia.exists() && nomedia.isFile()) {
                try {
                    nomedia.createNewFile();
                } catch (IOException io) {
                    io.getMessage();
                }
            }
            setInternalFileDir(mContext);

            String home = VideoFolder.getHomeDir(mContext);
            File mHome = new File(home);
            if (!mHome.exists()) {
                mHome.mkdirs();
            }

            String user = VideoFolder.getUserDir(mContext);
            File mUser = new File(user);
            if (!mUser.exists()) {
                mUser.mkdirs();
            }
            return this;
        }

        public Builder setFolder(String folder) {
            this.isFolder = !TextUtils.isEmpty(folder);
            this.mFolder = folder;
            File mFolderMe = new File(folder);
            if (!mFolderMe.exists()) {
                mFolderMe.mkdirs();
            }
            return this;
        }

        public Builder setFolderApk(boolean isFolder) {
            String folder = ZFOLDER_APK;
            this.isFolder = !TextUtils.isEmpty(folder);

            File mFolderMe = new File(folder);
            if (!mFolderMe.exists()) {
                mFolderMe.mkdirs();
            }

            return this;
        }

        public Builder setFolderImage(boolean isFolder) {
            String folder = ZFOLDER_IMG;
            this.isFolder = !TextUtils.isEmpty(folder);

            File mFolderMe = new File(folder);
            if (!mFolderMe.exists()) {
                mFolderMe.mkdirs();
            }
            return this;
        }

        public Builder setFolderScriptMe(boolean isFolder) {
            String folder = ZFOLDER_SCRIPTME;
            this.isFolder = !TextUtils.isEmpty(folder);

            File mFolderMe = new File(folder);
            if (!mFolderMe.exists()) {
                mFolderMe.mkdirs();
            }

            return this;
        }

        public Builder setFolderAudio(boolean isFolder) {
            String folder = ZFOLDER_AUDIO;
            this.isFolder = !TextUtils.isEmpty(folder);

            File mFolderMe = new File(folder);
            if (!mFolderMe.exists()) {
                mFolderMe.mkdirs();
            }

            return this;
        }

        public Builder setFolderAudio_Converted(boolean isFolder) {
            String folder = ZFOLDER_AUDIO_CONVERT;
            this.isFolder = !TextUtils.isEmpty(folder);

            File mFolderMe = new File(folder);
            if (!mFolderMe.exists()) {
                mFolderMe.mkdirs();
            }

            return this;
        }

        public Builder setFolderVideo(boolean isFolder) {
            String folder = ZFOLDER_VIDEO;
            this.isFolder = !TextUtils.isEmpty(folder);

            File mFolderMe = new File(folder);
            if (!mFolderMe.exists()) {
                mFolderMe.mkdirs();
            }

            return this;
        }

        public Builder setFolderVideoConverted(boolean isFolder) {
            String folder = ZFOLDER_VIDEO_CONVERTED;
            this.isFolder = !TextUtils.isEmpty(folder);

            File mFolderMe = new File(folder);
            if (!mFolderMe.exists()) {
                mFolderMe.mkdirs();
            }
            return this;
        }

        public Builder setFolderYoutube(boolean isFolder) {
            String folder = ZFOLDER_YOUTUBE;
            this.isFolder = !TextUtils.isEmpty(folder);

            File mFolderMe = new File(folder);
            if (!mFolderMe.exists()) {
                mFolderMe.mkdirs();
            }

            return this;
        }

        public Builder setFolderYoutube_Analytics(boolean isFolder) {
            String folder = ZFOLDER_YOUTUBE_ANALYTICS;
            this.isFolder = !TextUtils.isEmpty(folder);

            File mFolderMe = new File(folder);
            if (!mFolderMe.exists()) {
                mFolderMe.mkdirs();
            }
            return this;
        }

        public Builder setFolderYoutube_Download(boolean isFolder) {
            String folder = ZFOLDER_YOUTUBE_DOWNLOAD;
            this.isFolder = !TextUtils.isEmpty(folder);

            File mFolderMe = new File(folder);
            if (!mFolderMe.exists()) {
                mFolderMe.mkdirs();
            }
            return this;
        }


        public Builder setFolderEbook(boolean isFolder) {
            String folder = ZFOLDER_EBOOK;
            this.isFolder = !TextUtils.isEmpty(folder);

            File mFolderMe = new File(folder);
            if (!mFolderMe.exists()) {
                mFolderMe.mkdirs();
            }
            return this;
        }

        public Builder setFolderArchive(boolean isFolder) {
            String folder = ZFOLDER_ARCHIVE;
            this.isFolder = !TextUtils.isEmpty(folder);

            File mFolderMe = new File(folder);
            if (!mFolderMe.exists()) {
                mFolderMe.mkdirs();
            }
            return this;
        }

        private void setInternalFileDir(Context c) {
            getInternalFileDir(c);
        }

        public Builder setInternalCacheDir(Context c) {
            getInternalCacheDir(c);
            return this;
        }

        public Builder setExternalFileDir(Context c, String folder) {
            c.getExternalFilesDir(folder).getAbsolutePath();
            return this;
        }

        public VideoFolder build() {
            this.isFolder = !TextUtils.isEmpty(mFolder);
            return new VideoFolder(this);
        }
    }
}

