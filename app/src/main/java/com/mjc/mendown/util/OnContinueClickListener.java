package com.mjc.mendown.util;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by mjc on 2016/3/3.
 * 功能：使用这个类替代OnTouchListener，能够获得连续点击的效果
 */
public abstract class OnContinueClickListener implements View.OnTouchListener {
    private boolean isContinue;
    private Thread mThread;
    //单例模式，只创建一个Handler
    private volatile MyHandler mHandler;
    //不同事件，传入不同的what值,因为不同当前对象中，都只有一个实例
    private int what;
    public final static int interval = 20;
    private View view;

    public OnContinueClickListener() {
        //必须在主线程中调用
        if (mThread == null)
            mHandler = new MyHandler();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        this.view = v;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isContinue = true;
                mThread = new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        while (isContinue) {
                            mHandler.sendEmptyMessage(what);
                            Log.v("@msg-what", what + "");
                            try {
                                Thread.sleep(interval);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                };
                mThread.start();
                break;
            case MotionEvent.ACTION_UP:
                isContinue = false;
                mThread = null;
                break;
            case MotionEvent.ACTION_CANCEL:
                isContinue = false;
                mThread = null;
                break;
        }

        return true;
    }


    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            handleClickEvent(view);
        }
    }

    public abstract void handleClickEvent(View view);


}
