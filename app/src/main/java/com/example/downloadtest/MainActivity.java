package com.example.downloadtest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.downloadtest.callback.DownloadCallback;
import com.example.downloadtest.core.RetrofitFactory;
import com.example.downloadtest.utils.CommonUtils;
import com.example.downloadtest.utils.LogUtils;
import com.example.downloadtest.utils.ZipUtils;

import java.io.File;

import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity {

    private ProgressBar progressBar;

    private TextView tvTotalM;

    private TextView tvDownloadM;

    private TextView tvProgress;

    private Disposable mDownloadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = findViewById(R.id.pb_progress);
        tvTotalM = findViewById(R.id.tv_total_m);
        tvDownloadM = findViewById(R.id.tv_download_m);
        tvProgress = findViewById(R.id.tv_progress);

        findViewById(R.id.btn_download).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = ((EditText) findViewById(R.id.et_source)).getText().toString();

                if (!TextUtils.isEmpty(url)) {
                    downloadFile(url);
                }
            }
        });

        findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RetrofitFactory.cancel(mDownloadTask);
                Toast.makeText(MainActivity.this, "暂停下载", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btn_unzip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String url = ((EditText) findViewById(R.id.et_source)).getText().toString();
                        String path = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()
                                + File.separator + CommonUtils.getLast(url);

                        ZipUtils.decompressFile(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath(), path);

//                        ZipUtils.UnZipFolder(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath(), path);
                    }
                }).start();
            }
        });

    }

    private void downloadFile(String url) {
        String path = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()
                + File.separator + CommonUtils.getLast(url);
        RxNet.download(url, path, new DownloadCallback() {
            @Override
            public void onStart(Disposable d) {
                mDownloadTask = d;
                LogUtils.d("onStart " + d);
                Toast.makeText(MainActivity.this, "开始下载", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProgress(long totalByte, long currentByte, int progress) {
                LogUtils.d("onProgress " + progress);
                progressBar.setProgress(progress);
                tvProgress.setText(progress + "%");
                tvTotalM.setText(CommonUtils.byteFormat(totalByte));
                tvDownloadM.setText(CommonUtils.byteFormat(currentByte));
            }

            @Override
            public void onFinish(File file, String message) {
                LogUtils.d(message + " onFinish " + file.getAbsolutePath());
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String msg) {
                LogUtils.d("onError " + msg
                );
            }
        });
    }
}