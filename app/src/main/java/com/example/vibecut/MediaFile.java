package com.example.vibecut;

import android.net.Uri;

import java.io.Serializable;

public class MediaFile implements Serializable {
    private String nameFile;
    private String previewMedia; // Изменено на String
    private String pathToFile; // Изменено на String

    public MediaFile() {}

    public MediaFile(String nameFile, Uri previewMedia, Uri pathToFile) {
        this.nameFile = nameFile;
        this.previewMedia = previewMedia.toString(); // Сериализация Uri в строку
        this.pathToFile = pathToFile.toString(); // Сериализация Uri в строку
    }

    public String getNameFile() {
        return nameFile;
    }

    public void setNameFile(String nameFile) {
        this.nameFile = nameFile;
    }

    public Uri getPreviewUri() {
        return Uri.parse(previewMedia); // Десериализация строки в Uri
    }

    public void setPreviewUri(Uri previewMedia) {
        this.previewMedia = previewMedia.toString(); // Сериализация Uri в строку
    }

    public Uri getPathToFile() {
        return Uri.parse(pathToFile); // Десериализация строки в Uri
    }

    public void setPathToFile(Uri pathToFile) {
        this.pathToFile = pathToFile.toString(); // Сериализация Uri в строку
    }
}
