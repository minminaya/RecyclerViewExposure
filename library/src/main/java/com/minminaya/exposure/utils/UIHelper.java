package com.minminaya.exposure.utils;

import android.os.Handler;
import android.os.Looper;

/**
 * 在异步线程中,可通过此帮助类直接向主线程发送消息
 *
 * Author: minminaya  😊😊😊
 * Email: minminaya@gmail.com
 * Date: 2022/3/16 20:40
 */
public class UIHelper {

    public static final int MIN_POST_DELAY = 1;
    private volatile static Handler sHandler = null;

    private static Handler getInstance() {
        if (sHandler == null) {
            synchronized (UIHelper.class) {
                if (sHandler == null) {
                    sHandler = new Handler(Looper.getMainLooper());
                }
            }
        }
        return sHandler;
    }

    public static void removeCallback(Runnable runnable) {
        if (null != sHandler) {
            sHandler.removeCallbacks(runnable);
        }
    }

    public static void removeCallbacksAndMessages(Object token) {
        if (null != sHandler) {
            sHandler.removeCallbacksAndMessages(token);
        }
    }

    public static void runOnUiThread(Runnable runnable) {
        if (isRunningMainThread()) {
            runnable.run();
        } else {
            getInstance().post(runnable);
        }
    }

    public static void runOnUiThreadDelay(Runnable runnable, long delayMillis) {
        getInstance().postDelayed(runnable, delayMillis);
    }


    public static void runOnUiThreadDelay(long delayMillis, Runnable runnable) {
        getInstance().postDelayed(runnable, delayMillis);
    }

    public static boolean isRunningMainThread() {
        return Looper.getMainLooper() == Looper.myLooper();
    }
}
