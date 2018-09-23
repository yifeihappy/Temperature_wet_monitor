package com.example.yifeihappy.babymonitor;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by yifeihappy on 2018/9/22.
 */

public class SetFragment extends Fragment {

    private Button setTimeBtn = null;
    private Button setIpBtn = null;
    private Button setPortBtn = null;
    private Button setWetThreholdBtn = null;
    private Button setNotictTimeBtn = null;
    private Button setStopTimeBtn = null;

    private EditText interTimeEdt = null;
    private EditText ipEdt = null;
    private EditText portEdt = null;
    private EditText wetThreholdEdit = null;
    EditText noticeTimeEdt = null;
    EditText stopTimeEdt = null;

    TextView localIPTxt = null;
    
    final int ERROR_WHAT = 0x000;
    final int UPDATE_WHAT = 0x001;

    private SharedPreferences preferences = null;
    private SharedPreferences.Editor editor = null;
    
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case ERROR_WHAT:
                    Toast.makeText(SetFragment.this.getActivity(), msg.obj.toString(), Toast.LENGTH_SHORT).show();
                    break;
                case UPDATE_WHAT:
                    Toast.makeText(SetFragment.this.getActivity(), msg.obj.toString(), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = getActivity().getSharedPreferences("WetMonitor", MODE_PRIVATE);
        editor = preferences.edit();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
//        return super.onCreateView(inflater, container, savedInstanceState);
        View setView = inflater.inflate(R.layout.set_fragment, container, false);

        setTimeBtn = (Button)setView.findViewById(R.id.setTimeBtn);
        setIpBtn = (Button)setView.findViewById(R.id.setIPBtn);
        setPortBtn = (Button)setView.findViewById(R.id.setPortBtn);
        setWetThreholdBtn = (Button)setView.findViewById(R.id.setWetThreholdBtn);
        setNotictTimeBtn = (Button)setView.findViewById(R.id.setNoticeTimeBtn);
        setStopTimeBtn = (Button)setView.findViewById(R.id.setStopTimeBtn);
        
        interTimeEdt = (EditText)setView.findViewById(R.id.interTimeEdt);
        ipEdt = (EditText)setView.findViewById(R.id.ipEdt);
        portEdt = (EditText)setView.findViewById(R.id.portEdt);
        wetThreholdEdit = (EditText)setView.findViewById(R.id.wetThreholdEdt);
        noticeTimeEdt = (EditText)setView.findViewById(R.id.noticeTimeEdt);
        stopTimeEdt = (EditText)setView.findViewById(R.id.stopTimeEdt);

        stopTimeEdt.setText(""+preferences.getInt("STOP_TIME", MainActivity.STOP_TIME));//停止时间默认60秒
        interTimeEdt.setText( ""+preferences.getInt("INTERVAL_TIME", MainActivity.INTERVAL_TIME));//获取温度湿度数据间隔
        ipEdt.setText( preferences.getString("IP", MainActivity.IP));//IP地址
        portEdt.setText(""+preferences.getInt("PORT", MainActivity.PORT));
        wetThreholdEdit.setText(""+preferences.getInt("WET_THREHOLD", MainActivity.WET_THREHOLD));//湿度报警阈值
        noticeTimeEdt.setText(""+preferences.getInt("NOTICE_TIME", MainActivity.NOTICE_TIME));//持续报警时间

        localIPTxt = (TextView)setView.findViewById(R.id.localIPTxt);
        localIPTxt.setText(getHostIP());
        //设置时间间隔
        setTimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String interTimeStr=interTimeEdt.getText().toString().trim();
                if(isLegalInput(interTimeStr)){
                    MainActivity.INTERVAL_TIME = Integer.parseInt(interTimeStr);
                    editor.putInt("INTERVAL_TIME", Integer.parseInt(interTimeStr));
                    editor.commit();
                    setSuccessMsg("设置时间间隔成功!");
                }
            }
        });
        //设置IP地址
        setIpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String IPStr = ipEdt.getText().toString().trim();
                    if(ipCheck(IPStr)){
                        Message msg = new Message();
                        msg.what = ERROR_WHAT;
                        msg.obj = "请输入正确的IP地址";
                        handler.sendMessage(msg);
                    } else {
                        MainActivity.IP = IPStr;
                        editor.putString("IP", IPStr);
                        editor.commit();
                        setSuccessMsg( "设置IP地址成功！");
                    }
                
            }
        });
        //设置端口号
        setPortBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String portStr = portEdt.getText().toString().trim();
                if(isLegalInput(portStr)){
                    MainActivity.PORT = Integer.parseInt(portStr);
                    editor.putInt("PORT", Integer.parseInt(portStr));
                    editor.commit();
                    setSuccessMsg("设置PORT成功！");
                }
            }
        });

        //设置湿度报警阈值
        setWetThreholdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String wetThreholdStr = wetThreholdEdit.getText().toString().trim();
                if(isLegalInput(wetThreholdStr)) {
                    MainActivity.WET_THREHOLD = Integer.parseInt(wetThreholdStr);
                    editor.putInt("WET_THREHOLD", Integer.parseInt(wetThreholdStr));
                    editor.commit();
                    setSuccessMsg("设置报警阈值成功！");
                }
            }
        });
        //设置响铃时间
        setNotictTimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String noticeTimeStr = noticeTimeEdt.getText().toString().trim();
                if(isLegalInput(noticeTimeStr)) {
                    MainActivity.NOTICE_TIME = Integer.parseInt(noticeTimeStr);
                    editor.putInt("NOTICE_TIME",Integer.parseInt(noticeTimeStr));
                    editor.commit();
                    setSuccessMsg("设置响铃时间成功！");
                }
            }
        });
        //设置停止时间
        setStopTimeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String stopTimeStr = stopTimeEdt.getText().toString().trim();
                if(isLegalInput(stopTimeStr)){
                    MainActivity.STOP_TIME = Integer.parseInt(stopTimeStr);
                    editor.putInt("STOP_TIME", Integer.parseInt(stopTimeStr));
                    editor.commit();
                    setSuccessMsg("设置停止时间成功！");
                }
            }
        });
        
        return setView;
    }

    /**
     * 获取ip地址
     * @return
     */
    public static String getHostIP() {

        String hostIp = null;
        try {
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia = null;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements()) {
                    ia = ias.nextElement();
                    if (ia instanceof Inet6Address) {
                        continue;// skip ipv6
                    }
                    String ip = ia.getHostAddress();
                    if (!"127.0.0.1".equals(ip)) {
                        hostIp = ia.getHostAddress();
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            Log.i("yao", "SocketException");
            e.printStackTrace();
        }
        return hostIp;

    }
    /**
     * 判断IP地址的合法性，这里采用了正则表达式的方法来判断
     * return true，合法
     * */
    public boolean ipCheck(String text) {
        if (text != null && !text.isEmpty()) {
            // 定义正则表达式
            String regex = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\.(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\.(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\.(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";
            // 判断ip地址是否与正则表达式匹配
            if (text.matches(regex)) {
                // 返回判断信息
                return true;
            } else {
                // 返回判断信息
                return false;
            }
        }
        return false;
    }

    private boolean isLegalInput(String inputStr){
        Message msg = new Message();
        msg.what = 0x000;
        if(0 == inputStr.length()){
            msg.obj = "不能为空";
            handler.sendMessage(msg);
            return false;
        } else if(Integer.parseInt(inputStr)<0) {
            msg.obj = "非法输入";
            handler.sendMessage(msg);
            return false;
        }
        return true;
    }

    private void setSuccessMsg(String str){
        Message msg = new Message();
        msg.what = UPDATE_WHAT;
        msg.obj = str;
        handler.sendMessage(msg);
    }
    
}
