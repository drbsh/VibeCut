package com.example.vibecut.Models;

import android.net.Uri;

import java.io.Serializable;
import java.time.LocalTime;
import java.util.UUID;

public abstract class BaseAudioFile implements Serializable {
    private String idFile;
    private String nameFile;
    private String pathToFile;
    private LocalTime duration;
    private String typeMedia;
    private int widthOnTimeline;

    // Конструктор для общих полей
    public BaseAudioFile(String nameFile, Uri pathToFile, LocalTime duration, String typeMedia) {
        this.idFile = UUID.randomUUID().toString(); // Рандомный ID
        this.nameFile = nameFile;
        this.pathToFile = pathToFile.toString(); // Сериализация Uri в строку
        this.duration = duration;
        this.typeMedia = typeMedia;
    }

    // Геттеры и сеттеры
    public String getNameFile() {
        return nameFile;
    }

    public void setNameFile(String nameFile) {
        this.nameFile = nameFile;
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

    public int getWidthOnTimeline() {
        return widthOnTimeline;
    }

    public void setWidthOnTimeline(int widthOnTimeline) {
        this.widthOnTimeline = widthOnTimeline;
    }
}