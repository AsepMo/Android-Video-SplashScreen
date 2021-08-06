package com.video.evolution.application.player.utils;

import android.content.Context;
import android.os.Handler;

public class AsyncSleep {
    private Task task = null;
    private Context context;

    public interface Task {
         void onCountDown(int left);
         void onFinish();
    }

    public AsyncSleep task(Task task) {
        this.task = task;
        return this;
    }

    public AsyncSleep(Context context) {
        this.context = context;
    }

    public void start(int second) {
        for (int i = 1; i <= second; i++) {
            final int left = second - i;
            new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run(){
                            task.onCountDown(left);
                            if (left == 0) {
                                task.onFinish();
                            }
                            }
                        });
                    }
                }, i * 1000);
        }
    }

    private void runOnUiThread(Runnable task) {
        new Handler(context.getMainLooper()).post(task);
    }
}

