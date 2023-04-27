package com.example.downloadtest.callback;

import java.io.File;

import io.reactivex.disposables.Disposable;

public interface DownloadCallback {
    void onStart(Disposable d);

    void onProgress(long totalByte, long currentByte, int progress);

    void onFinish(File file, String message);

    void onError(String msg);
}
