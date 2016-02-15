package com.benjd90.photos2.beans;

import java.util.Date;

/**
 * Bean that represent a file without data
 * Created by Benjamin on 11/02/2016.
 */
public class FileLight {

    private String path;
    private Date date;
    private long size;
    private boolean isDirectory;


    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public boolean getIsDirectory() {
        return isDirectory;
    }

    public void setIsDirectory(boolean isDirectory) {
        this.isDirectory = isDirectory;
    }
}
