package com.benjd90.photos2.scheduler.utils;

import com.benjd90.photos2.beans.FileLight;
import com.benjd90.photos2.beans.PhotoLight;
import com.benjd90.photos2.utils.PhotosUtils;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Functions to list photos
 * Created by Benjamin on 26/02/2016.
 */
public class PhotosExplorer {
  private static final Logger LOG = LoggerFactory.getLogger(PhotosExplorer.class);

  private static List<PhotoLight> getListOfPhotos(String path) throws IOException {
    File directory = new File(path);
    File[] list = directory.listFiles();
    List<PhotoLight> ret = new ArrayList<>();

    if (list != null) {
      for (File file : list) {
        PhotoLight photoLight = new PhotoLight();
        photoLight.setPath(file.getPath());
        photoLight.setSize(file.length());
        photoLight.setIsDirectory(file.isDirectory());
        photoLight.setDate(getPhotoDate(file));
        ret.add(photoLight);
      }
    }

    return ret;
  }

  public static List<PhotoLight> getListOfPhotosRecursively(String path) throws IOException {
    List<PhotoLight> ret = getListOfPhotos(path);

    for (int i = 0; i < ret.size(); i++) {
      FileLight f = ret.get(i);
      if (f.getIsDirectory()) {
        ret.addAll(getListOfPhotos(f.getPath()));
      }
    }

    ret = filterOnlyPhotos(ret);

    return ret;
  }


  private static List<PhotoLight> filterOnlyPhotos(List<PhotoLight> listToFilter) {
    List<PhotoLight> ret = new ArrayList<>(listToFilter.size());
    for (PhotoLight p : listToFilter) {
      // TODO : Add use of exif
      if (PhotosUtils.isPhoto(p)) {
        ret.add(p);
      }
    }
    return ret;
  }


  //TODO add scan on file change https://docs.oracle.com/javase/tutorial/essential/io/notification.html
  public static Date getPhotoDate(File file) throws IOException {
    Date editDate = new Date(file.lastModified());
    Date creationDate = null;
    Date takenDate = null;
    if (file.isFile() && PhotosUtils.isPhoto(file)) {
      try {
        Metadata metadata = ImageMetadataReader.readMetadata(file);
        Directory directoryMetadata = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
        if (directoryMetadata != null) {
          Date date = directoryMetadata.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL, TimeZone.getTimeZone(ZoneId.systemDefault()));
          if (date != null && date.getTime() > 0) {
            takenDate = date;
          }
        }
      } catch (ImageProcessingException e) {
        LOG.error("Can't read photo", e);
      }

      BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
      creationDate = new Date(attr.creationTime().toMillis());
    }

    if (takenDate != null) {
      return takenDate;
    } else if (creationDate != null && editDate.after(creationDate)) {
      return creationDate;
    } else {
      return editDate;
    }
  }
}
