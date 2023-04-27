package com.example.downloadtest.utils;

import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class ZipUtils {
    //过滤在mac上压缩时自动生成的__MACOSX文件夹
    private static final String MAC_IGNORE = "__MACOSX/";

    public static void decompressFile(String target, String source) {
        if(TextUtils.isEmpty(target)){
            return;
        }
        try {
            File file = new File(source);
            if(!file.exists()) {
                LogUtils.d("原压缩文件不存在");
                return;
            }
            ZipFile zipFile = new ZipFile(file);
            ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(file));
            ZipEntry zipEntry = null;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                String fileName = zipEntry.getName();

                File temp = new File(target + File.separator + fileName);
                if(zipEntry.isDirectory()) {
                    File dir = new File(target + File.separator + fileName);
                    dir.mkdirs();
                    continue;
                }
                if (temp.getParentFile() != null && !temp.getParentFile().exists()) {
                    temp.getParentFile().mkdirs();
                }
                byte[] buffer = new byte[1024];
                OutputStream os = new FileOutputStream(temp);
                // 通过ZipFile的getInputStream方法拿到具体的ZipEntry的输入流
                InputStream is = zipFile.getInputStream(zipEntry);
                int len = 0;
                while ((len = is.read(buffer)) != -1) {
                    os.write(buffer, 0, len);
                }
                os.close();
                is.close();
            }
            zipInputStream.close();

            //删掉原先的zip
            file.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
