package com.example.yifeihappy.babymonitor;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private ShowFragment showFragment = null;
    private NoticeFragment noticeFragment = null;
    private SetFragment setFragment = null;

    static volatile MediaPlayer mediaPlayer = null;
    static volatile boolean SEND_FALG = true;//是否继续发送
    static volatile boolean WARN_STATE = false;//是否正在报警
    static volatile boolean IS_OVERTIME = false;//是否在超时状态
    static volatile  boolean IS_STOP = false;//是否停止

    static volatile int INTERVAL_TIME = 3;//发送访问命令间隔,单位sc c
    static volatile String IP = "192.168.0.3";
    static volatile int PORT = 502;
    static volatile int STOP_TIME = 30;
    static volatile int WET_THREHOLD = 80;
    static volatile int NOTICE_TIME = 25;


    private SharedPreferences preferences = null;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();

            switch (item.getItemId()) {
                case R.id.navigation_show:
                    fragmentTransaction.replace(R.id.content, showFragment, "ShowFragment");
                    break;
                case R.id.navigation_notifications:
                    fragmentTransaction.replace(R.id.content, noticeFragment, "NoticeFragment");
                    break;
                case R.id.navigation_set:
                    fragmentTransaction.replace(R.id.content, setFragment, "SetFragment");
                    break;
            }
            fragmentTransaction.commit();
            return true;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        showFragment = new ShowFragment();
        noticeFragment = new NoticeFragment();
        setFragment = new SetFragment();

        navigation.setSelectedItemId(R.id.navigation_show);

        mediaPlayer = MediaPlayer.create(this, R.raw.beep);

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mediaPlayer.start();
                mediaPlayer.setLooping(true);
            }
        });

        preferences = getSharedPreferences("WetMonitor", MODE_PRIVATE);
        STOP_TIME = preferences.getInt("STOP_TIME", STOP_TIME);//停止时间默认60秒
        INTERVAL_TIME = preferences.getInt("INTERVAL_TIME", INTERVAL_TIME);//获取温度湿度数据间隔
        IP = preferences.getString("IP", IP);//IP地址
        PORT = preferences.getInt("PORT", PORT);
        WET_THREHOLD = preferences.getInt("WET_THREHOLD", WET_THREHOLD);//湿度报警阈值
        NOTICE_TIME = preferences.getInt("NOTICE_TIME", NOTICE_TIME);//持续报警时间
    }

    static void stopMediaPlayer()
    {
        if(mediaPlayer.isPlaying()){
            mediaPlayer.stop();
        }
        try {
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(null != mediaPlayer && mediaPlayer.isPlaying()){
            mediaPlayer.stop();
        }

        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
