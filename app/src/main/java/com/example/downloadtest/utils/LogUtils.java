package com.example.downloadtest.utils;

import android.util.Log;



/**
 * @author LvQiSheng
 * @date 2019/6/14
 */
public class LogUtils {
    private static final String TAG = "Daisy";

    public static void d(String msg) {
            Log.d(TAG, msg);
    }

    public static void i(String msg) {
            Log.i(TAG, msg);
    }

    public static void e(String msg) {
            Log.e(TAG, msg);
    }

}
