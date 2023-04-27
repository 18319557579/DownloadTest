package com.example.downloadtest.utils;

import android.os.Handler;
import android.os.Looper;

public class HandlerUtils {
    public static void runOnUi(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }
}
