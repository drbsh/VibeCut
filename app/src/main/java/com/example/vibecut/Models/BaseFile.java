package com.example.vibecut.Models;

import android.net.Uri;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalTime;
import java.util.UUID;

public abstract class BaseFile implements Serializable {
    private String idFile;
    private String nameFile;
    private String pathToFile;
    private Duration duration;
    private String typeMedia;
    private int widthOnTimeline;
    private Duration maxDuration;
    // Конструктор для общих полей
    public BaseFile(String nameFile, Uri pathToFile, Duration duration, String typeMedia, int widthOnTimeline) {
        this.idFile = UUID.randomUUID().toString(); // Рандомный ID
        this.nameFile = nameFile;
        this.pathToFile = pathToFile.toString(); // Сериализация Uri в строку
        this.duration = duration;
        this.typeMedia = typeMedia;
        this.widthOnTimeline = widthOnTimeline;
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

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
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

    public Duration getMaxDuration() {
        return maxDuration;
    }

    public void setMaxDuration(Duration maxDuration) {
        this.maxDuration = maxDuration;
    }
}