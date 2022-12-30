package com.ims.helpers;

import android.os.FileObserver;

public class DirectoryObserver extends FileObserver {

    private String path;

    public DirectoryObserver(String path) {
        super(path);
        this.path = path;
    }

    public DirectoryObserver(String path, int mask) {
        super(path, mask);
        this.path = path;
    }

    @Override
    public void onEvent(int i, String s) {
    }

    public String getPath() {
        return path;
    }
}
