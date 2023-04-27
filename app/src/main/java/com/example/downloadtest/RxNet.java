package com.example.downloadtest;

import android.text.TextUtils;

import com.example.downloadtest.callback.DownloadCallback;
import com.example.downloadtest.callback.DownloadListener;
import com.example.downloadtest.core.RetrofitFactory;
import com.example.downloadtest.utils.CommonUtils;
import com.example.downloadtest.utils.HandlerUtils;
import com.example.downloadtest.utils.LogUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import okhttp3.ResponseBody;

public class RxNet {
    public static void download(final String url, final String filePath, final DownloadCallback callback) {
        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(filePath)) {
            if (null != callback) {
                callback.onError("url or path empty");
            }
            return;
        }

        File oldFile = new File(filePath);
        if (oldFile.exists()) {
            if (null != callback) {
                callback.onFinish(oldFile, "文件已存在");
            }
            return;
        }

        DownloadListener listener = new DownloadListener() {
            @Override
            public void onStart(ResponseBody responseBody) {
                saveFile(responseBody, url, filePath, callback);
            }
        };

        RetrofitFactory.downloadFile(url, CommonUtils.getTempFile(url, filePath).length(), listener, new Observer<ResponseBody>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {
                if (null != callback) {
                    callback.onStart(d);
                }
            }

            @Override
            public void onNext(@NonNull ResponseBody responseBody) {
                LogUtils.d("执行了onNext " + responseBody);
            }

            @Override
            public void onError(@NonNull Throwable e) {
                e.printStackTrace();
                LogUtils.d("onError " + e.getMessage());
                if (null != callback) {
                    callback.onError(e.getMessage());
                }
            }

            @Override
            public void onComplete() {
                LogUtils.i("download onComplete ");
            }
        });
    }

    /**
     * 这里是最终成功/失败的出口 ？
     */
    private static void saveFile(final ResponseBody responseBody, String url, final String filePath, final DownloadCallback callback) {
        boolean downloadSuccess = true;
        final File tempFile = CommonUtils.getTempFile(url, filePath);
        try {
            writeFileToDisk(responseBody, tempFile.getAbsolutePath(), callback);
        } catch (Exception e) {
            e.printStackTrace();
            downloadSuccess = false;
            LogUtils.d("下载失败：" + e);
        }

        LogUtils.d("下载成功？ " + downloadSuccess);

        if (downloadSuccess) {
            final boolean renameSuccess = tempFile.renameTo(new File(filePath));
            LogUtils.d("改名是否成功：" + renameSuccess);
            HandlerUtils.runOnUi(new Runnable() {
                @Override
                public void run() {
                    if (null != callback && renameSuccess) {
                        callback.onFinish(new File(filePath), "下载真的成功了");
                    }
                }
            });
        }
    }

    private static void writeFileToDisk(ResponseBody responseBody, String filePah, final DownloadCallback callback) throws IOException {
        long totalByte = responseBody.contentLength();
        LogUtils.d("本次要下载的内容长度:" + totalByte);

        long downloadByte = 0;
        File file = new File(filePah);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        byte[] buffer = new byte[1024 * 4];
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rwd");
        long tempFileLen = file.length();
        LogUtils.d("临时文件的长度:" + tempFileLen);

        randomAccessFile.seek(tempFileLen);

        LogUtils.d("开始读取相应的输入流");
        while (true) {
            //这里将输入流读到缓冲区去
            int len = responseBody.byteStream().read(buffer);
            if (len == -1) {
                LogUtils.d("输入流读到了尾部");
                break;
            }
            randomAccessFile.write(buffer, 0, len);
            downloadByte += len;

            callbackProgress(tempFileLen + totalByte, tempFileLen + downloadByte, callback);
        }
        randomAccessFile.close();

    }

    private static void callbackProgress(final long totalByte, final long downloadedByte, final DownloadCallback callback) {
        HandlerUtils.runOnUi(new Runnable() {
            @Override
            public void run() {
                if (null != callback) {
                    callback.onProgress(totalByte, downloadedByte, (int)((downloadedByte * 100) / totalByte));
                }
            }
        });
    }
}
