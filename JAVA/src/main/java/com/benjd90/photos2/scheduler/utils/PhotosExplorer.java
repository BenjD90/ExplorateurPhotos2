package com.benjd90.photos2.scheduler.utils;

import com.benjd90.photos2.beans.PhotoLight;
import com.benjd90.photos2.utils.Constants;
import com.benjd90.photos2.utils.PhotosUtils;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
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

  public static List<PhotoLight> filterOnlyPhotos(List<PhotoLight> listToFilter) {
    List<PhotoLight> ret = new ArrayList<>(listToFilter.size());
    for (PhotoLight p : listToFilter) {
      // TODO : Add use of exif
      if (PhotosUtils.isPhoto(p)) {
        ret.add(p);
      }
    }
    return ret;
  }


  public static Date getPhotoDate(File file) throws IOException {
    Date editDate = new Date(file.lastModified());
    Date creationDate = null;
    Date takenDate = null;
    if (file.isFile() && PhotosUtils.isPhoto(file)) {
      try {
        Metadata metadata = ImageMetadataReader.readMetadata(file);
        Directory directoryMetadata = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
        if (directoryMetadata != null) {
          Date date = directoryMetadata.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL, TimeZone.getDefault());
          if (date != null && date.getTime() > 0) {
            takenDate = date;
          }
        }
      } catch (ImageProcessingException e) {
        LOG.error("Can't read photo " + file.getAbsolutePath(), e);
        PhotosUtils.addError(Constants.METADATA, file.getAbsolutePath(), e);
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


  public static Dimension getDimensions(File file) throws IOException {
    if (file.isFile() && PhotosUtils.isPhoto(file)) {
      try {
        Metadata metadata = ImageMetadataReader.readMetadata(file);
        Directory exifDirectory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
        if (exifDirectory != null && exifDirectory.containsTag(ExifSubIFDDirectory.TAG_EXIF_IMAGE_WIDTH)
                && exifDirectory.containsTag(ExifSubIFDDirectory.TAG_EXIF_IMAGE_HEIGHT)) {
          int width, height;
          try {
            Directory exifIFD0Directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            if (exifIFD0Directory != null && exifIFD0Directory.containsTag(ExifIFD0Directory.TAG_ORIENTATION)) {
              int orientation = exifIFD0Directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
              switch (orientation) {
                case 6: // [Exif IFD0] Orientation - Right side, top (Rotate 90 CW)
                case 8: // [Exif IFD0] Orientation - Left side, bottom (Rotate 270 CW)
                  //swap
                  width = exifDirectory.getInt(ExifSubIFDDirectory.TAG_EXIF_IMAGE_HEIGHT);
                  height = exifDirectory.getInt(ExifSubIFDDirectory.TAG_EXIF_IMAGE_WIDTH);
                  break;
                case 1: // [Exif IFD0] Orientation - Top, left side (Horizontal / normal)
                case 3: // [Exif IFD0] Orientation - Bottom, right side (Rotate 180)
                default:
                  width = exifDirectory.getInt(ExifSubIFDDirectory.TAG_EXIF_IMAGE_WIDTH);
                  height = exifDirectory.getInt(ExifSubIFDDirectory.TAG_EXIF_IMAGE_HEIGHT);
              }
            } else {
              width = exifDirectory.getInt(ExifSubIFDDirectory.TAG_EXIF_IMAGE_WIDTH);
              height = exifDirectory.getInt(ExifSubIFDDirectory.TAG_EXIF_IMAGE_HEIGHT);
            }
          } catch (MetadataException e) {
            //try to read height and width java buffered images
            BufferedImage img = PhotosUtils.readPhoto(file);
            if (img != null) {
              width = img.getWidth();
              height = img.getHeight();
            } else {
              return new Dimension();
            }
          }
          return new Dimension(width, height);
        } else {
          BufferedImage img = PhotosUtils.readPhoto(file);
          if (img == null) {
            return new Dimension();
          }
          return new Dimension(img.getWidth(), img.getHeight());
        }
      } catch (ImageProcessingException e) {
        LOG.error("Can't read photo EXIF", e);
        PhotosUtils.addError(Constants.METADATA, file.getAbsolutePath(), e);
        return new Dimension();
      }
    } else {
      return new Dimension();
    }
  }
}
