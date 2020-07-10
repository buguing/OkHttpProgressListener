package com.wellee.okhttpuploadlistener;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

public class DownloadResponseBody extends ResponseBody {

    private final ResponseBody mResponseBody;
    private BufferedSource mBufferedSource;
    private ProgressListener mProgressListener;

    public DownloadResponseBody(ResponseBody responseBody, ProgressListener listener) {
        this.mResponseBody = responseBody;
        this.mProgressListener = listener;
    }

    @Override
    public MediaType contentType() {
        return mResponseBody.contentType();
    }

    @Override
    public long contentLength() {
        return mResponseBody.contentLength();
    }

    @Override
    public BufferedSource source() {
        if (mBufferedSource == null) {
            mBufferedSource = Okio.buffer(source(mResponseBody.source()));
        }
        return mBufferedSource;
    }

    private Source source(Source source) {
        final long contentLength = contentLength();
        return new ForwardingSource(source) {
            private long mProgress = 0;

            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                long bytesRead = super.read(sink, byteCount);
                mProgress += bytesRead == -1 ? 0 : bytesRead;
                if (mProgressListener != null) {
                    mProgressListener.progress(contentLength, mProgress);
                }
                return bytesRead;
            }
        };
    }
}
