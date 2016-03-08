package com.benjd90.photos2.beans;

import java.util.Date;

/**
 * Bean that represent a photo without data
 * Created by Benjamin on 11/02/2016.
 */
public class PhotoLight extends FileLight {

  private Date dateLastModified;
  private long height;
  private long width;

  public long getHeight() {
    return height;
  }

  public void setHeight(long height) {
    this.height = height;
  }

  public long getWidth() {
    return width;
  }

  public void setWidth(long width) {
    this.width = width;
  }

  public Date getDateLastModified() {
    return dateLastModified;
  }

  public void setDateLastModified(Date dateLastModified) {
    this.dateLastModified = dateLastModified;
  }
}
