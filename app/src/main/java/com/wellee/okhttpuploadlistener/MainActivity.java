package com.wellee.okhttpuploadlistener;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.io.IOException;
import java.net.FileNameMap;
import java.net.URLConnection;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity {

    private File dir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dir = getExternalFilesDir("file");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        // /storage/emulated/0/Android/data/com.wellee.okhttpuploadlistener/files/file/test.apk

        RxPermissions permissions = new RxPermissions(this);
        Disposable subscribe = permissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(new Consumer<Boolean>() {


                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        if (aBoolean) {

                        }
                    }
                });
    }

    private void upload() {
        File file = new File(dir, "1.jpg");
        Log.e("MainActivity", "totalSize = " + file.length());
        String url = "http://10.0.2.2:9999/uploadFile";
        OkHttpClient client = new OkHttpClient();
        MultipartBody.Builder builder = new MultipartBody.Builder();

        builder.setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(),
                        RequestBody.create(MediaType.parse(guessMimeType(file.getAbsolutePath())), file));

        ExMultipartBody exMultipartBody = new ExMultipartBody(builder.build(), new ProgressListener() {
            @Override
            public void progress(long total, long current) {
                toast(total, current);
            }
        });

        // 构建一个请求
        final Request request = new Request.Builder()
                .url(url)
                .post(exMultipartBody)
                .build();
        // new RealCall 发起请求
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.e("MainActivity", response.body().string());
            }
        });
    }

    private void toast(final long total, final long current) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 更新UI
                Log.e("MainActivity", "total = " + total + "; current = " + current);
            }
        });
    }

    private String guessMimeType(String path) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String type = fileNameMap.getContentTypeFor(path);
        if (TextUtils.isEmpty(type)) {
            return "application/octet-stream";
        }
        return type;
    }

    public void upload(View view) {
        upload();
    }

    public void download(View view) {
        download();
    }

    private void download() {
        String url = "http://10.0.2.2:9999/downloadFile/1.jpg";
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        OkHttpClient client = builder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Response response = chain.proceed(chain.request());
                DownloadResponseBody downloadResponseBody = new DownloadResponseBody(response.body(), new ProgressListener() {
                    @Override
                    public void progress(long total, long current) {
                        toast(total, current);
                    }
                });
                return response.newBuilder().body(downloadResponseBody).build();
            }
        }).build();
        final Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                ResponseBody body = response.body();
                if (body != null) {
                    byte[] bytes = body.bytes();
                    showImage(bytes);
                }
            }
        });
    }

    private void showImage(final byte[] bytes) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ImageView image = findViewById(R.id.iv);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                image.setImageBitmap(bitmap);
            }
        });
    }
}
