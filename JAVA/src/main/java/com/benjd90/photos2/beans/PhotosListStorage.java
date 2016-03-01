package com.benjd90.photos2.beans;

import java.util.Date;
import java.util.List;

/**
 * List of photos and other infos about storage
 * Created by Benjamin on 26/02/2016.
 */
public class PhotosListStorage {

  private List<PhotoLight> photos;
  private Date dateScan;


  public Date getDateScan() {
    return dateScan;
  }

  public void setDateScan(Date dateScan) {
    this.dateScan = dateScan;
  }

  public List<PhotoLight> getPhotos() {
    return photos;
  }

  public void setPhotos(List<PhotoLight> photos) {
    this.photos = photos;
  }
}
