package com.example.vibecut;

import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ProjectInfo implements Serializable {
    private Uri preview;
    private String name;
    private LocalTime duration = LocalTime.of(0, 0); // 00:00
    private LocalDateTime date;
    private Boolean isFavourite = false;
    private List<MediaFile> projectFiles = new ArrayList<MediaFile>();
    //private File fileProject;
    public ProjectInfo(){
    }

    public ProjectInfo(Uri preview, String name, LocalTime duration, LocalDateTime date, Boolean isFavourite, ArrayList<MediaFile> projectFiles/*,File fileProject*/) {
        this.preview = preview;
        this.name = name;
        this.duration = duration;
        this.date = date;
        this.isFavourite = isFavourite;
        this.projectFiles = projectFiles;
        //this.fileProject = fileProject;
    }

    public Uri getPreview() {
        return preview;
    }

    public void setPreview(Uri preview) {
        this.preview = preview;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalTime getDurarion() {
        return duration;
    }

    public void setDurarion(LocalTime duration) {
        this.duration = duration;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Boolean getFavourite() {
        return isFavourite;
    }

    public void setFavourite(Boolean favourite) {
        isFavourite = favourite;
    }

    public void setProjectFiles(List<MediaFile> projectFiles){
        this.projectFiles = projectFiles;
    }
    public void addMediaFile(MediaFile mediaFile){
        projectFiles.add(mediaFile);
    }
    public List<MediaFile> getProjectFiles(){
        return projectFiles;
    }

    /*public File getFileProject() {
        return fileProject;
    }

    public void setFileProject(File fileProject) {
        this.fileProject = fileProject;
    }

    //метод для установки пути
    public void setFileProjectPath(String filePath) {
        this.fileProject = new File(filePath);
    }

    //метод для получения пути
    public String getFileProjectPath() {
        return fileProject != null ? fileProject.getAbsolutePath() : null;
    }*/
}
