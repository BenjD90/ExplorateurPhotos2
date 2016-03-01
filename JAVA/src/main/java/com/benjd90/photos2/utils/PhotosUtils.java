package com.benjd90.photos2.utils;

import com.benjd90.photos2.beans.PhotoLight;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Created by Benjamin on 15/02/2016.
 */
public class PhotosUtils {
  private static final Logger LOG = LoggerFactory.getLogger(PhotosUtils.class);


  public static boolean isPhoto(File fileToTest) {
    return ConfigReader.getPhotosExtensions().contains(FilenameUtils.getExtension(fileToTest.getAbsolutePath()).toLowerCase());
  }

  public static boolean isPhoto(PhotoLight photoToTest) {
    return ConfigReader.getPhotosExtensions().contains(FilenameUtils.getExtension(photoToTest.getPath()).toLowerCase());
  }

  public static BufferedImage resizeImage(BufferedImage originalImage, int width, int height) {
    int type = originalImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : originalImage.getType();

    BufferedImage resizedImage = new BufferedImage(width, height, type);
    Graphics2D g = resizedImage.createGraphics();
    g.drawImage(originalImage, 0, 0, width, height, null);
    g.dispose();

    return resizedImage;
  }

}
