package com.example.vibecut;

import android.net.Uri;

import java.io.Serializable;
import java.time.LocalTime;
import java.util.UUID;

public class MediaFile implements Serializable {
    private String idFile;
    private String nameFile;
    private String previewMedia; // Изменено на String
    private String pathToFile; // Изменено на String
    private LocalTime duration;
    private String typeMedia;
    public MediaFile() {}

    public MediaFile(String nameFile, Uri previewMedia, Uri pathToFile, LocalTime duration, String typeMedia) {
        this.idFile = UUID.randomUUID().toString(); //рандом id
        this.nameFile = nameFile;
        this.previewMedia = previewMedia.toString(); // Сериализация Uri в строку
        this.pathToFile = pathToFile.toString(); // Сериализация Uri в строку
        this.duration = duration;
        this.typeMedia = typeMedia;
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

    public LocalTime getDuration() {
        return duration;
    }

    public void setDuration(LocalTime duration) {
        this.duration = duration;
    }

    public String getTypeMedia() {
        return typeMedia;
    }

    public void setTypeMedia(String typeMedia) {
        this.typeMedia = typeMedia;
    }

    public String getIdFile() {
        return idFile;
    }

    public void setIdFile(String idFile) {
        this.idFile = idFile;
    }
}
