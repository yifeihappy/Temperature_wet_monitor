package com.example.yifeihappy.babymonitor;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import java.io.IOException;

/**
 * Created by yifeihappy on 2018/9/22.
 */

public class NoticeFragment extends Fragment {

    volatile static boolean NOTICE_BTN_CLICKABLE = false;
    static ImageButton noticeBtn = null;
    final static int NOTICE_CLICKABLE_WHAT = 0x000;
    final static int NOTICE_DISCLICKABLE_WHAT = 0x001;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    static Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case NOTICE_CLICKABLE_WHAT:
                    if(null != noticeBtn) {
                        noticeBtn.setImageResource(R.drawable.light);
                        noticeBtn.setEnabled(true);
                    }
                    NOTICE_BTN_CLICKABLE = true;
                    break;
                case NOTICE_DISCLICKABLE_WHAT:
                    if(null != noticeBtn){
                        noticeBtn.setImageResource(R.drawable.light00);
                        noticeBtn.setEnabled(false);
                    }
                    NOTICE_BTN_CLICKABLE = false;
                    break;
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
//        return super.onCreateView(inflater, container, savedInstanceState);
        View noticeView = inflater.inflate(R.layout.notice_fragment, container, false);
        noticeBtn = (ImageButton)noticeView.findViewById(R.id.noticeBtn);

        setNoticeBtnClickable(NOTICE_BTN_CLICKABLE);

        noticeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.IS_STOP = true;
                setNoticeBtnClickable(false);
            }
        });
        return noticeView;
    }

    public static void setNoticeBtnClickable(boolean noticeBtnState)
    {
        Message msg = new Message();
        if(noticeBtnState) {
            msg.what = NOTICE_CLICKABLE_WHAT;
            handler.sendMessage(msg);
        } else {
            msg.what = NOTICE_DISCLICKABLE_WHAT;
            handler.sendMessage(msg);
        }
    }
}
