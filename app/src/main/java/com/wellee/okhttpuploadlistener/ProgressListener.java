package com.wellee.okhttpuploadlistener;

public interface ProgressListener {

    void progress(long total, long current);
}
