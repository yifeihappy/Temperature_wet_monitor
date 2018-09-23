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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

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
        if(noticeBtnState) {
            if(null != noticeBtn) {
                noticeBtn.setImageResource(R.drawable.light);
            }
            NOTICE_BTN_CLICKABLE = true;
        } else {
            if(null != noticeBtn){
                noticeBtn.setImageResource(R.drawable.light00);
                noticeBtn.setEnabled(noticeBtnState);
            }
            NOTICE_BTN_CLICKABLE = false;
        }
    }
}
