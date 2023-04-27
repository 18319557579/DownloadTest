package com.example.downloadtest.core;

import com.example.downloadtest.callback.DownloadListener;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

public class DownloadInterceptor implements Interceptor {
    private final DownloadListener listener;

    public DownloadInterceptor(DownloadListener listener) {
        this.listener = listener;
    }


    @Override
    public Response intercept(Chain chain) throws IOException {
        Response originResponse = chain.proceed(chain.request());

        return originResponse.newBuilder()
                .body(new DownloadResponseBody(originResponse.body(), listener))
                .build();
    }
}
