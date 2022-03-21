package com.minminaya.exposure.utils;

import androidx.annotation.NonNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Author: minminaya  😊😊😊
 * Email: minminaya@gmail.com
 * Date: 2022/3/21 17:50
 */
public class ThreadHelper {

    private ThreadHelper() {
        //do nothing
    }

    private static ExecutorService sExecutor = null;

    public static void executeExposureSingleTask(@NonNull Runnable runnable) {
        if (sExecutor == null) {
            sExecutor = Executors.newSingleThreadExecutor();
        }
        sExecutor.execute(runnable);
    }
}
