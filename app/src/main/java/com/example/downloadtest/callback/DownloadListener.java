package com.example.downloadtest.callback;

import okhttp3.ResponseBody;

public interface DownloadListener {
    void onStart(ResponseBody responseBody);
}
