package com.benjd90.photos2.beans.comparator;

import com.benjd90.photos2.beans.PhotoLight;

import java.util.Comparator;

/**
 * Created by Benjamin on 08/03/2016.
 */
public class PhotoLightDefaultComparator implements Comparator<PhotoLight> {
  @Override
  public int compare(PhotoLight p1, PhotoLight p2) {
    return p2.getDateLastModified().compareTo(p1.getDateLastModified());
  }
}
