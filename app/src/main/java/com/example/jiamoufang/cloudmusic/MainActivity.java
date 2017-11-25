package com.example.jiamoufang.cloudmusic;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.security.Permission;
import java.text.SimpleDateFormat;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private IBinder mBinder;
    private ServiceConnection mConnection;
    /*控件*/
    private Button playButton;
    private Button stopButton;
    private Button quitButton;
    private ImageView image;
    private TextView currentTime;
    private TextView endTime;
    private TextView txt_state;
    private SeekBar seekBar;

    /*服务实例*/
    private MusicService musicService;
    private SimpleDateFormat time =  new SimpleDateFormat("mm:ss");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        musicService = new MusicService();

        /*绑定服务，开启服务*/
        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mBinder = service;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mConnection = null;
            }
        };
        Intent intent = new Intent(this,MusicService.class);
        startService(intent);
        bindService(intent,mConnection, Context.BIND_AUTO_CREATE);

        /*初始化*/
        initViews();
        /*事件*/
        initEvents();




    }

    private void initEvents() {
        playButton.setOnClickListener(this);
        stopButton.setOnClickListener(this);
        quitButton.setOnClickListener(this);
    }

    private void initViews() {
        image = (ImageView)findViewById(R.id.image);

        /*动画绑定*/
        musicService.animator = ObjectAnimator.ofFloat(image,"rotation",0,350);

        txt_state = (TextView)findViewById(R.id.txt_state);
        currentTime = (TextView)findViewById(R.id.start_time);
        endTime = (TextView)findViewById(R.id.end_time);
        seekBar = (SeekBar)findViewById(R.id.seekbar);
        playButton = (Button)findViewById(R.id.isPlayButton);
        stopButton = (Button)findViewById(R.id.stopButton);
        quitButton = (Button) findViewById(R.id.quitButton);
    }

    public Handler handler = new Handler();
    public Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (musicService.mediaPlayer.isPlaying()) {
                txt_state.setText("Playing");
            } else {
                if (musicService.which.equals("stop")) {
                    txt_state.setText("stop");
                } else if (musicService.which.equals("pause")) {
                    txt_state.setText("pause");
                }
            }
            currentTime.setText(time.format(musicService.mediaPlayer.getCurrentPosition()));
            endTime.setText(time.format(musicService.mediaPlayer.getDuration()));
            seekBar.setProgress(musicService.mediaPlayer.getCurrentPosition());
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        musicService.mediaPlayer.seekTo(seekBar.getProgress());
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            handler.postDelayed(runnable,100);
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
        mConnection = null;
        try {
            MainActivity.this.finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        if (musicService != null)
            musicService.setAnimattion();
        checkStoragePerssion(this);

        if (musicService.mediaPlayer.isPlaying()) {
            txt_state.setText("playing");
        } else {
            if (musicService.which.equals("stop")) {
                txt_state.setText("stop");
            } else if(musicService.which.equals("pause")) {
                txt_state.setText("pause");
            }
        }
        seekBar.setProgress(musicService.mediaPlayer.getCurrentPosition());
        seekBar.setMax(musicService.mediaPlayer.getDuration());
        handler.post(runnable);
        super.onResume();
    }

    public static void checkStoragePerssion(MainActivity mainActivity) {
        String[] PERMISSIONS_STORAGE = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        final int REQUEST_EXTERNAL_STORAGE = 1;
        try {
            int permission = ActivityCompat.checkSelfPermission(mainActivity, "android.permission.READ_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(mainActivity,PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(MainActivity.this, "您已授予应用权限", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, "用户权限不足，启动失败", Toast.LENGTH_SHORT).show();
            System.exit(0);
        }
        return;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.isPlayButton:
                playButtonHandler();
                break;
            case R.id.stopButton:
                stopButtonHandler();
                break;
            case R.id.quitButton:
                quitButtonHnadler();
                break;
        }

    }

    /*退出播放器处理*/
    private void quitButtonHnadler() {
        try {
            int code = 103;
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            mBinder.transact(code,data,reply,0);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        handler.removeCallbacks(runnable);
        unbindService(mConnection);
        try {
            finish();
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*停止播放处理*/
    private void stopButtonHandler() {
        try {
            int code = 102;
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            mBinder.transact(code,data,reply,0);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /*暂停 or 开始播放处理*/
    private void playButtonHandler() {
        if (musicService.mediaPlayer.isPlaying()) {
            txt_state.setText("Pause");
            playButton.setText("play");
        } else {
            txt_state.setText("Playing");
            playButton.setText("Pause");
        }
        try {
            int code = 101;
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            mBinder.transact(code,data,reply,0);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        musicService.returnFlag = 1;
    }

}
