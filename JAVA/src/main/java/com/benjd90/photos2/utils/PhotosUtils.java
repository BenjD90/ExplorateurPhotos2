package com.benjd90.photos2.utils;

import com.benjd90.photos2.beans.PhotoLight;
import com.benjd90.photos2.scheduler.utils.PhotosExplorer;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

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

  public static synchronized byte[] createCacheThumbnail(Integer width, Integer height, File photoOriginal, File thumbnailCacheFile) throws IOException {
    if (!thumbnailCacheFile.getParentFile().exists()) {
      if (!thumbnailCacheFile.getParentFile().mkdirs()) {
        throw new IOException("Can't create cache directories" + thumbnailCacheFile.getParentFile());
      }
    }

    BufferedImage originalImage = ImageIO.read(photoOriginal);

    int widthOriginal = originalImage.getWidth();
    int heightOriginal = originalImage.getHeight();
    float ratio = ((float) widthOriginal) / heightOriginal;

    if (height == null) {
      height = (int) (width * ratio);
    } else if (width == null) {
      width = (int) (height * ratio);
    }

    BufferedImage resizedImageHintJpg = resizeImage(originalImage, width, height);

    storeBufferedImage(resizedImageHintJpg, thumbnailCacheFile);
    return Files.readAllBytes(thumbnailCacheFile.toPath());
  }

  private static void storeBufferedImage(BufferedImage resizedImageHintJpg, File thumbnailCacheFile) throws IOException {
    JPEGImageWriteParam jpegParams = new JPEGImageWriteParam(null);
    jpegParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
    jpegParams.setCompressionQuality(0.5f);


    final ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
    FileOutputStream outputStream = new FileOutputStream(thumbnailCacheFile);
    writer.setOutput(ImageIO.createImageOutputStream(outputStream));
    writer.write(null, new IIOImage(resizedImageHintJpg, null, null), jpegParams);
    outputStream.close();
  }

  public static Path getThumbnailPath(Integer width, Integer height, File photoOriginal) {
    String pathToPhotoInPhotosLibrary = StringUtils.replaceOnce(photoOriginal.getAbsolutePath(), ConfigReader.getMessage(ConfigReader.KEY_PATH), Constants.EMPTY_STRING);
    return Paths.get(ConfigReader.getMessage(ConfigReader.KEY_APP_DIR), ConfigReader.getMessage(ConfigReader.KEY_CACHE_DIR), width + "x" + height, pathToPhotoInPhotosLibrary);
  }

  public static PhotoLight getPhotoLightFromFile(File file) throws IOException {
    PhotoLight photoLight = new PhotoLight();
    photoLight.setPath(file.getPath());
    photoLight.setSize(file.length());
    photoLight.setIsDirectory(file.isDirectory());
    photoLight.setDate(PhotosExplorer.getPhotoDate(file));
    photoLight.setDateLastModified(new Date(file.lastModified()));
    Dimension photoDimensions = PhotosExplorer.getDimensions(file);
    photoLight.setHeight((long) photoDimensions.getHeight());
    photoLight.setWidth((long) photoDimensions.getWidth());
    return photoLight;
  }
}
