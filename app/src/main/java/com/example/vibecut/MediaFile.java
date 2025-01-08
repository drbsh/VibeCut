package com.example.vibecut;

import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;

import java.io.Serializable;
import java.nio.file.Path;

public class MediaFile implements Serializable {
    private String nameFile;
    private Uri previewMedia;
    private Uri pathToFile;

    public MediaFile(){}

    public MediaFile(String nameFile, Uri previewMedia, Uri pathToFile){
        this.nameFile = nameFile;
        this.pathToFile = pathToFile;
        this.previewMedia = previewMedia;
    }

    public String getNameFile() {
        return nameFile;
    }

    public void setNameFile(String nameFile) {
        this.nameFile = nameFile;
    }

    public Uri getPreviewUri() {
        return previewMedia;
    }

    public void setPreviewUri(Uri previewMedia) {
        this.previewMedia = previewMedia;
    }

    public Uri getPathToFile() {
        return pathToFile;
    }

    public void setPathToFile(Uri pathToFile) {
        this.pathToFile = pathToFile;
    }
}
