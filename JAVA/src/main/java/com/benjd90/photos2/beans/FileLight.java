package com.benjd90.photos2.beans;

import java.util.Date;

/**
 * Bean that represent a file without data
 * Created by Benjamin on 11/02/2016.
 */
public class FileLight {

    private String path;
    private Date dateEdit;
    private long size;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Date getDateEdit() {
        return dateEdit;
    }

    public void setDateEdit(Date dateEdit) {
        this.dateEdit = dateEdit;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
