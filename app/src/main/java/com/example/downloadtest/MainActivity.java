package com.example.downloadtest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.downloadtest.callback.DownloadCallback;
import com.example.downloadtest.core.RetrofitFactory;
import com.example.downloadtest.other.AntZipUtils;
import com.example.downloadtest.utils.CommonUtils;
import com.example.downloadtest.utils.LogUtils;
import com.example.downloadtest.utils.ZipUtils;
import com.hzy.lib7z.IExtractCallback;
import com.hzy.lib7z.Z7Extractor;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.RequestCallback;

import java.io.File;
import java.util.List;

import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity {

    private ProgressBar progressBar;

    private TextView tvTotalM;

    private TextView tvDownloadM;

    private TextView tvProgress;

    private Disposable mDownloadTask;


    private Button btn_makeZip;
    private Button btn_unZip;
    private TextView tv_show;
    private Button btnApply;

    private MakeZipTask makeZipTask;//生成zip文件的异步请求类
    private UnZipTask unZipTask;//解压zip文件的异步请求类

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = findViewById(R.id.pb_progress);
        tvTotalM = findViewById(R.id.tv_total_m);
        tvDownloadM = findViewById(R.id.tv_download_m);
        tvProgress = findViewById(R.id.tv_progress);

        initViews();
        initEvents();

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

        findViewById(R.id.btn_other).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = ((EditText) findViewById(R.id.et_source)).getText().toString();
                String source = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()
                        + File.separator + CommonUtils.getLast(url);
                String target = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Z7Extractor.extractFile(source, target, new IExtractCallback() {
                            @Override
                            public void onStart() {
                                LogUtils.d("回调了onStart");
                            }

                            @Override
                            public void onGetFileNum(int fileNum) {
                                LogUtils.d("回调了onGetFileNum：" + fileNum);
                            }

                            @Override
                            public void onProgress(String name, long size) {
                                LogUtils.d( "回调了onProgress：" + name + " | " + size);
                            }

                            @Override
                            public void onError(int errorCode, String message) {
                                LogUtils.d( "回调了onError：" + errorCode + " | " + message);
                            }

                            @Override
                            public void onSucceed() {
                                LogUtils.d("回调了onSucceed");
                            }
                        });
                    }
                }).start();

            }
        });

        findViewById(R.id.btn_storage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyForRight();
            }
        });

        findViewById(R.id.btn_storage_other).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testAllFiles();
            }
        });


    }

    private void initViews() {
        btn_makeZip = (Button) findViewById(R.id.btn_makeZip);
        btn_unZip = (Button) findViewById(R.id.btn_unZip);

        tv_show = (TextView) findViewById(R.id.tv_show);
    }

    private void initEvents() {
        btn_makeZip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //生成ZIP压缩包【建议异步执行】
                makeZipTask = new MakeZipTask();
                makeZipTask.execute();
            }
        });

        btn_unZip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //解压ZIP包【建议异步执行】
                unZipTask = new UnZipTask();
                unZipTask.execute();

            }
        });
    }

    /**
     * 压缩文件的异步请求任务
     *
     */
    public class MakeZipTask extends AsyncTask<String, Void, String>{

        @Override
        protected void onPreExecute() {
            //显示进度对话框
            //showProgressDialog("");
            tv_show.setText("正在压缩...");
        }

        @Override
        protected String doInBackground(String... params) {
            String data = "";
            if(! isCancelled()){
                try {
                    String[] srcFilePaths = new String[1];
                    srcFilePaths[0] = Environment.getExternalStorageDirectory() + "/why";
                    String zipPath = Environment.getExternalStorageDirectory() + "/why.zip";
                    AntZipUtils.makeZip(srcFilePaths,zipPath);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return data;
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(isCancelled()){
                return;
            }
            try {
                Log.w("MainActivity","result="+result);
            }catch (Exception e) {
                if(! isCancelled()){
                    //showShortToast("文件压缩失败");
                    tv_show.setText("文件压缩失败");
                }
            } finally {
                if(! isCancelled()){
                    //隐藏对话框
                    //dismissProgressDialog();
                    tv_show.setText("压缩完成");
                }
            }
        }
    }

    /**
     * 解压文件的异步请求任务
     *
     */
    public class UnZipTask extends AsyncTask<String, Void, String>{

        @Override
        protected void onPreExecute() {
            //显示进度对话框
            //showProgressDialog("");
            tv_show.setText("正在解压...");
        }

        @Override
        protected String doInBackground(String... params) {
            String data = "";
            if(! isCancelled()){
                try {
                    String zipPath = Environment.getExternalStorageDirectory() + "/why.zip";
                    String targetDirPath = Environment.getExternalStorageDirectory() + "/why";
                    AntZipUtils.unZip(zipPath,targetDirPath);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return data;
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(isCancelled()){
                return;
            }
            try {
                Log.w("MainActivity","result="+result);
            }catch (Exception e) {
                if(! isCancelled()){
                    //showShortToast("文件解压失败");
                    tv_show.setText("文件解压失败");
                }
            } finally {
                if(! isCancelled()){
                    //隐藏对话框
                    //dismissProgressDialog();
                    tv_show.setText("解压完成");
                }
            }
        }
    }

    private void applyForRight() {
        PermissionX.init(MainActivity.this).permissions(Manifest.permission.WRITE_EXTERNAL_STORAGE).request(new RequestCallback() {
            @Override
            public void onResult(boolean allGranted, @NonNull List<String> grantedList, @NonNull List<String> deniedList) {
                if (allGranted) {
                    Toast.makeText(MainActivity.this, "全部权限已给予", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, "你拒绝了以下权限:" + deniedList, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void testAllFiles() {
        //运行设备>=Android 11.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //检查是否已经有权限
            if (!Environment.isExternalStorageManager()) {
                //跳转新页面申请权限
                startActivityForResult(new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION), 101);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //申请权限结果
        if (requestCode == 101) {
            if (Environment.isExternalStorageManager()) {
                Toast.makeText(MainActivity.this, "访问所有文件权限申请成功", Toast.LENGTH_SHORT).show();
            }
        }
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