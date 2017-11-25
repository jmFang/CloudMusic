package com.example.jiamoufang.cloudmusic;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.view.animation.LinearInterpolator;

public class MusicService extends Service {
    public static MediaPlayer mediaPlayer = new MediaPlayer();
    private IBinder mBinder = new MyBinder();
    public static ObjectAnimator animator;

    public static String which= "";
    public static int returnFlag = 0;

    public class MyBinder extends Binder {
        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 101:
                    /*播放按钮，服务处理函数*/
                    playOrPauseHandler();
                    break;
                case 102:
                    /*停止按钮,服务处理函数*/
                    stopHandler();
                    break;
                case 103:
                    /*退出处理*/
                    quitHnadler();
                    break;
                case 104:
                    /*界面刷新，服务返回数据函数*/
                    break;
                case 105:
                    /*拖动进度处理*/
                    break;
                default:
                    break;
            }
            return super.onTransact(code, data, reply, flags);
        }
    }

    /*退出播放处理函数*/
    private void quitHnadler() {
        animator.end();
        mediaPlayer.stop();
        mediaPlayer.release();
    }

    /*停止播放处理函数*/
    private void stopHandler() {
        which = "stop";
        animator.pause();
        if(mediaPlayer != null) {
            mediaPlayer.pause();
            mediaPlayer.stop();
            try {
                mediaPlayer.prepare();
                mediaPlayer.seekTo(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /*暂时或播放处理函数*/
    private int flag = 0;
    private void playOrPauseHandler() {
        flag ++;
        if (flag >= 1000)
            flag = 2;

        which = "pause";
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            animator.pause();
        } else {
            mediaPlayer.start();
            if (flag == 1 || returnFlag == 1) {
                animator.setDuration(5000);
            /*设置匀速转动*/
                animator.setInterpolator(new LinearInterpolator());
            /*设置无限循环*/
                animator.setRepeatCount(ValueAnimator.INFINITE);
                animator.setRepeatMode(ValueAnimator.RESTART);
                animator.start();
            } else {
                animator.start();
            }

        }

    }

    /*
    * 获取文件路径，设置播放属性
    * */
    public MusicService() {
        try {
            mediaPlayer.setDataSource("/data/melt.mp3");
            mediaPlayer.prepare();
            mediaPlayer.setLooping(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /*
    * 设置转动
    * */
    public void setAnimattion() {
        if (mediaPlayer.isPlaying()) {
            animator.setDuration(5000);
            /*设置匀速转动*/
            animator.setInterpolator(new LinearInterpolator());
            /*设置无限循环*/
            animator.setRepeatCount(ValueAnimator.INFINITE);
            animator.setRepeatMode(ValueAnimator.RESTART);
            animator.start();
        }
    }

    @Override
    public void onDestroy() {
        mediaPlayer.stop();
        mediaPlayer.release();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return mBinder;
    }
}
