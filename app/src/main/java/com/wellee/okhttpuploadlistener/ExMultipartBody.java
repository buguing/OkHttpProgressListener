package com.wellee.okhttpuploadlistener;

import android.util.Log;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;

/**
 * 静态代理 MultipartBody
 */
public class ExMultipartBody extends RequestBody {

    private MultipartBody mMultipartBody;
    private long mCurrentLength;

    private ProgressListener mListener;

    public ExMultipartBody(MultipartBody multipartBody, ProgressListener listener) {
        this.mMultipartBody = multipartBody;
        this.mListener = listener;
    }

    @Override
    public MediaType contentType() {
        return mMultipartBody.contentType();
    }

    @Override
    public long contentLength() throws IOException {
        return mMultipartBody.contentLength();
    }



    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        Log.e("ExMultipartBody", "监听");

        // 这里的contentLength 略大于 file.length()
        final long contentLength = contentLength();

        // sink代理
        ForwardingSink forwardingSink = new ForwardingSink(sink) {
            @Override
            public void write(Buffer source, long byteCount) throws IOException {
                mCurrentLength += byteCount;
                Log.e("ExMultipartBody", contentLength + " ------ " + mCurrentLength);
                if (mListener != null) {
                    mListener.progress(contentLength, mCurrentLength);
                }
                super.write(source, byteCount);
            }
        };

        BufferedSink bufferedSink = Okio.buffer(forwardingSink);
        mMultipartBody.writeTo(bufferedSink);
        // 刷新
        bufferedSink.flush();
    }
}
