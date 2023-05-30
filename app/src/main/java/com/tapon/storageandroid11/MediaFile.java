package com.tapon.storageandroid11;

import android.net.Uri;

public class MediaFile {

    public Uri uri;
    public long id;
    public long time;
    public int size;
    public String name;
    public String mime;
    public String path;

    public MediaFile(long id, Uri uri, String name, long time, int size, String mime) {
        this.id = id;
        this.uri = uri;
        this.name = name;
        this.time = time;
        this.size = size;
        this.mime = mime;
    }
}