package com.example.yifeihappy.babymonitor;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by yifeihappy on 2018/9/22.
 */

public class ShowFragment extends Fragment {

    private Button connectBtn = null;
    private Button disconnectBtn = null;

    private TextView temTxt = null;
    private TextView wetTxt = null;

    volatile private Socket socket = null;
    volatile private OutputStream os = null;
    volatile private BufferedInputStream in = null;

    private Thread sendThread = null;
    private Thread receiveThread = null;
    private Thread noticeThread = null;

    volatile static boolean CONNECT_BTN_CLICKABLE = true;
    volatile static boolean DISCONNECT_BTN_CLICKABLE = false;

    final int ERROE_WHAT = 0x000;
    final int UPDATE_WHAT = 0x001;
    final int CONNECT_WHAT = 0X002;
    final int DISCONNECT_WHAT = 0x003;

    private String last_temperature = null;
    private String last_wet = null;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case ERROE_WHAT:
                    String errorStr = msg.obj.toString();
                    Toast.makeText(ShowFragment.this.getActivity(), errorStr, Toast.LENGTH_SHORT).show();
                    break;
                case UPDATE_WHAT:
                    Bundle bundle = (Bundle)msg.obj;
                    temTxt.setText(bundle.getString("Temperature"));
                    wetTxt.setText(bundle.getString("Wet"));
                    break;
                case CONNECT_WHAT:
                    setBtnState_con_disconnect(false, true);
                    Toast.makeText(ShowFragment.this.getActivity(), "连接成功！", Toast.LENGTH_SHORT).show();
                    break;
                case DISCONNECT_WHAT:
                    setBtnState_con_disconnect(true, false);
                    Toast.makeText(ShowFragment.this.getActivity(), "已断开连接", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
//        return super.onCreateView(inflater, container, savedInstanceState);
        View showView = inflater.inflate(R.layout.show_fragment, container, false);

        connectBtn = (Button)showView.findViewById(R.id.connectBtn);
        disconnectBtn = (Button)showView.findViewById(R.id.disconnectBtn);
        temTxt = (TextView)showView.findViewById(R.id.tempTxt);
        wetTxt = (TextView)showView.findViewById(R.id.wetTxt);

        if(null!=last_temperature) {
            temTxt.setText(last_temperature);
        }
        if(null!=last_wet) {
            wetTxt.setText(last_wet);
        }

        //根据状态，使能按钮
        connectBtn.setEnabled(CONNECT_BTN_CLICKABLE);
        disconnectBtn.setEnabled(DISCONNECT_BTN_CLICKABLE);

        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(){
                    @Override
                    public void run() {
                        super.run();

                        socket = new Socket();
                        Message msg = new Message();
                        try {
                            socket.connect(new InetSocketAddress(MainActivity.IP, MainActivity.PORT), 10000);
                            in = new BufferedInputStream(socket.getInputStream());
                            os = socket.getOutputStream();
                            receiveThread = new ReceiveThread();
                            sendThread = new SendThread();

                            receiveThread.start();
                            sendThread.start();

                            msg.what = CONNECT_WHAT;
                        } catch (IOException e) {
                            e.printStackTrace();
                            msg.what = ERROE_WHAT;
                            msg.obj = "网络异常!";
                        }

                        handler.sendMessage(msg);
                    }
                }.start();
            }
        });

        disconnectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(null != receiveThread){
                    receiveThread.interrupt();
                }
                if(null != sendThread){
                    sendThread.interrupt();
                }

                if(null != in){
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if(null != os){
                    try {
                        os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if(null != socket) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Message msg = new Message();
                msg.what = DISCONNECT_WHAT;
                handler.sendMessage(msg);
            }
        });

        return showView;
    }

    class SendThread extends Thread{
        @Override
        public void run() {
            super.run();
            byte[] temperatureCommand = new byte[12];
            temperatureCommand[0] = (byte)0x00;
            temperatureCommand[1] = (byte)0x00;
            temperatureCommand[2] = (byte)0x00;
            temperatureCommand[3] = (byte)0x00;
            temperatureCommand[4] = (byte)0x00;
            temperatureCommand[5] = (byte)0x00;
            temperatureCommand[6] = (byte)0x01;
            temperatureCommand[7] = (byte)0x03;
            temperatureCommand[8] = (byte)0x00;
            temperatureCommand[9] = (byte)0x00;
            temperatureCommand[10] = (byte)0x00;
            temperatureCommand[11] = (byte)0x02;
            try{
                while(MainActivity.SEND_FALG){
                    if(null == os){
                        Message msg = new Message();
                        msg.what = ERROE_WHAT;
                        msg.obj = "网络输出异常";
                        handler.sendMessage(msg);
                    } else {
                        os.write(temperatureCommand);
                        try{
                            sleep(MainActivity.INTERVAL_TIME*1000);
                        } catch (InterruptedException e){
                            e.printStackTrace();
                        }
                    }
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    //接收数据线程
    class ReceiveThread extends Thread{
        @Override
        public void run() {
            super.run();
            byte[] readByte = new byte[13];
            try{
                int r;
                while((r = in.read(readByte))!=-1){
                    Message msg = new Message();
                    if(13 == r) {
                        int[] T_W = new int[4];
                        Log.d("asd", ShowFragment.BinaryToHexString(readByte));
                        //计算温湿度
                        for(int i=0;i<4;i++){
                            if(readByte[9+i]<0) {
                                T_W[i] = 256 + readByte[9+i];
                            } else {
                                T_W[i] = readByte[9+i];
                            }
                        }
                        double temperature = (T_W[0]*256+T_W[1])/10.0;
                        double wet = (T_W[2]*256+T_W[3])/10.0;
                        //湿度大于阈值
                        //不是在响铃状态
                        //不是在超时等待状态
                        last_temperature = ""+temperature;
                        last_wet = ""+wet;
                        if(wet>MainActivity.WET_THREHOLD && !MainActivity.WARN_STATE && !MainActivity.IS_OVERTIME){
                            MainActivity.WARN_STATE = true;
                            noticeThread = new noticeThread();
                            noticeThread.start();
                        }
                        msg.what = UPDATE_WHAT;
                        Bundle bundle = new Bundle();
                        bundle.putString("Temperature",""+temperature);
                        bundle.putString("Wet",""+wet);
                        msg.obj = bundle;
                    } else {
                        msg.what = ERROE_WHAT;
                        msg.obj = "接收数据错误";
                    }
                    handler.sendMessage(msg);
                }
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    //响铃倒计时
    class noticeThread extends Thread {
        @Override
        public void run() {
            super.run();
            MainActivity.mediaPlayer.seekTo(0);
            MainActivity.mediaPlayer.start();//开始播放

            NoticeFragment.setNoticeBtnClickable(true);

            int count = 0;
            while(count++ < MainActivity.NOTICE_TIME && !MainActivity.IS_STOP){
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.d("ring","ring:"+count);
            }

            MainActivity.WARN_STATE = false;
            NoticeFragment.setNoticeBtnClickable(false);

            MainActivity.stopMediaPlayer();

            //如果由于报警超时造成的
//            if(count>MainActivity.NOTICE_TIME){
                MainActivity.IS_OVERTIME = true;
                int l_count = 0;
                while(l_count++<MainActivity.STOP_TIME){
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Log.d("stop", "stop:"+l_count);
                }
                MainActivity.IS_OVERTIME = false;
                MainActivity.IS_STOP = false;
//            }

        }
    }

    private void setBtnState_con_disconnect(boolean connectB, boolean disconnectb){
        connectBtn.setEnabled(connectB);
        disconnectBtn.setEnabled(disconnectb);
        recordBtnState(connectB, disconnectb);
    }

    public static void recordBtnState(boolean connectB, boolean disconnectB){
        CONNECT_BTN_CLICKABLE = connectB;
        DISCONNECT_BTN_CLICKABLE = disconnectB;
    }

    //将字节数组转换为16进制字符串
    public static String BinaryToHexString(byte[] bytes) {
        String hexStr = "0123456789ABCDEF";
        String result = "";
        String hex = "";
        for (byte b : bytes) {
            hex = String.valueOf(hexStr.charAt((b & 0xF0) >> 4));
            hex += String.valueOf(hexStr.charAt(b & 0x0F));
            result += hex + " ";
        }
        return result;
    }
}
