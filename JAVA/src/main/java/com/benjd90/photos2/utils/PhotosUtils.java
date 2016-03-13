package com.benjd90.photos2.utils;

import com.benjd90.photos2.beans.PhotoLight;
import com.benjd90.photos2.scheduler.utils.PhotosExplorer;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.*;
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


  /**
   * @param width
   * @param height
   * @param photoOriginal
   * @param thumbnailCacheFile if null, image is not stored
   * @return
   * @throws IOException
   */
  public static synchronized
  @Nullable
  byte[] createCacheThumbnail(Integer width, Integer height, File photoOriginal, @Nullable File thumbnailCacheFile) throws IOException {
    if (thumbnailCacheFile != null && !thumbnailCacheFile.getParentFile().exists()) {
      if (!thumbnailCacheFile.getParentFile().mkdirs()) {
        throw new IOException("Can't create cache directories" + thumbnailCacheFile.getParentFile());
      }
    }
    BufferedImage originalImage = null;
    try {
      originalImage = PhotosUtils.readPhoto(photoOriginal);
    } catch (IOException e) {
      LOG.error("Can't read input file " + photoOriginal.getAbsolutePath(), e);
      return null;
    }
    int widthOriginal = originalImage.getWidth();
    int heightOriginal = originalImage.getHeight();
    float ratio = ((float) widthOriginal) / heightOriginal;

    if (height == null) {
      height = (int) (width * ratio);
    } else if (width == null) {
      width = (int) (height * ratio);
    }

    BufferedImage resizedImage = resizeImage(originalImage, width, height);

    if (thumbnailCacheFile != null) {
      storeBufferedImage(resizedImage, thumbnailCacheFile);
      return Files.readAllBytes(thumbnailCacheFile.toPath());
    } else {
      return getBufferedImageBytes(resizedImage);
    }
  }


  public static BufferedImage readPhoto(File file) throws IOException {
    BufferedImage originalImage = ImageIO.read(file);
    try {
      Metadata metadata = ImageMetadataReader.readMetadata(file);
      Directory exifIFD0Directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
      if (exifIFD0Directory != null && exifIFD0Directory.containsTag(ExifIFD0Directory.TAG_ORIENTATION)) {
        int orientation;
        orientation = exifIFD0Directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);

        AffineTransform tx = new AffineTransform();
        double angleRotation = 0;
        switch (orientation) {
          case 1: // [Exif IFD0] Orientation - Top, left side (Horizontal / normal)
            return originalImage;
          case 6: // [Exif IFD0] Orientation - Right side, top (Rotate 90 CW)
            LOG.debug("Rotate 90");
            angleRotation = Math.toRadians(90);
            break;
          case 3: // [Exif IFD0] Orientation - Bottom, right side (Rotate 180)
            LOG.debug("Rotate 180");
            angleRotation = Math.toRadians(180);
            break;
          case 8: // [Exif IFD0] Orientation - Left side, bottom (Rotate 270 CW)
            LOG.debug("Rotate 270");
            angleRotation = Math.toRadians(270);
            break;
          default:
            return originalImage;
        }
        return rotate(originalImage, angleRotation);
      } else {
        return originalImage;
      }
    } catch (ImageProcessingException e) {
      LOG.error("Can't read photo exif", e);
    } catch (MetadataException e) {
      LOG.error("Can't read photo Metadata", e);
    }
    return originalImage;
  }


  private static BufferedImage rotate(BufferedImage image, double angle) {
    double sin = Math.abs(Math.sin(angle));
    double cos = Math.abs(Math.cos(angle));
    int width = image.getWidth();
    int height = image.getHeight();
    int newWidth = (int) Math.floor(width * cos + height * sin);
    int newHeight = (int) Math.floor(height * cos + width * sin);
    BufferedImage result = new BufferedImage(newWidth, newHeight, image.getType());
    Graphics2D g = result.createGraphics();
    g.translate((newWidth - width) / 2, (newHeight - height) / 2);
    g.rotate(angle, width / 2, height / 2);
    g.drawRenderedImage(image, null);
    g.dispose();
    return result;
  }

  private static void storeBufferedImage(BufferedImage resizedImageHintJpg, File thumbnailCacheFile) throws IOException {
    FileOutputStream outputStream = new FileOutputStream(thumbnailCacheFile);
    wirteImgaeToOutputStream(resizedImageHintJpg, outputStream);
    outputStream.close();
  }

  private static OutputStream wirteImgaeToOutputStream(BufferedImage resizedImageHintJpg, OutputStream outputStream) throws IOException {
    final ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
    writer.setOutput(ImageIO.createImageOutputStream(outputStream));
    writer.write(null, new IIOImage(resizedImageHintJpg, null, null), getJpegWriteParam());
    return outputStream;
  }

  private static byte[] getBufferedImageBytes(BufferedImage resizedImageHintJpg) throws IOException {
    JPEGImageWriteParam jpegParams = getJpegWriteParam();

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    wirteImgaeToOutputStream(resizedImageHintJpg, outputStream);

    byte[] imageInByte = outputStream.toByteArray();
    outputStream.close();
    return imageInByte;
  }

  private static JPEGImageWriteParam getJpegWriteParam() {
    JPEGImageWriteParam jpegParams = new JPEGImageWriteParam(null);
    jpegParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
    jpegParams.setCompressionQuality(0.4f);
    return jpegParams;
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
