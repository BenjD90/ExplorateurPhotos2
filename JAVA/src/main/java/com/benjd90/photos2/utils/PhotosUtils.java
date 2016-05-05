package com.benjd90.photos2.utils;

import com.benjd90.photos2.beans.PhotoError;
import com.benjd90.photos2.beans.PhotoLight;
import com.benjd90.photos2.scheduler.utils.PhotosExplorer;
import com.drew.imaging.FileType;
import com.drew.imaging.FileTypeDetector;
import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.lang.annotations.NotNull;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.imageio.*;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Benjamin on 15/02/2016.
 */
@Component
public class PhotosUtils {
  private static final Logger LOG = LoggerFactory.getLogger(PhotosUtils.class);

  private static List<PhotoError> photosInError = new ArrayList<>();

  private static String photosExtensions;

  @Value("${photosExtensions}")
  public void setPhotosExtensions(String photosExtensions) {
    PhotosUtils.photosExtensions = photosExtensions;
  }

  private static String photosPath;

  @Value("${path}")
  public void setPhotosPath(String photosPath) {
    PhotosUtils.photosPath = photosPath;
  }

  private static String appDir;

  @Value("${appFilesDir}")
  public void setAppDir(String appDir) {
    PhotosUtils.appDir = appDir;
  }

  private static String cacheDir;

  @Value("${cacheDir}")
  public void setCacheDir(String cacheDir) {
    PhotosUtils.cacheDir = cacheDir;
  }

  public static boolean isPhoto(File fileToTest) {
    return photosExtensions.contains(FilenameUtils.getExtension(fileToTest.getAbsolutePath()).toLowerCase());
  }

  public static boolean isPhoto(PhotoLight photoToTest) {
    return photosExtensions.contains(FilenameUtils.getExtension(photoToTest.getPath()).toLowerCase());
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
    BufferedImage originalImage;
    try {
      originalImage = PhotosUtils.readPhoto(photoOriginal);
      if (originalImage == null) {
        return null;
      }
    } catch (IOException e) {
      LOG.error("Can't read input file " + photoOriginal.getAbsolutePath(), e);
      addError(Constants.THUMBNAIL, photoOriginal.getAbsolutePath(), e);
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


  public static
  @Nullable
  BufferedImage readPhoto(File file) throws IOException {
    BufferedImage originalImage = null;
    FileInputStream fis = null;
    BufferedInputStream bis = null;
    try {
      originalImage = ImageIO.read(file);

      fis = new FileInputStream(file);
      bis = new BufferedInputStream(fis);
      if (FileTypeDetector.detectFileType(bis).equals(FileType.Jpeg)) {
        Metadata metadata = JpegMetadataReader.readMetadata(bis);
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
      } else {
        return originalImage;
      }
    } catch (JpegProcessingException e) {
      LOG.error("Can't read photo exif" + file.getAbsolutePath(), e);
      addError(Constants.EXIF, file.getAbsolutePath(), e);
    } catch (MetadataException e) {
      LOG.error("Can't read photo Metadata" + file.getAbsolutePath(), e);
      addError(Constants.METADATA, file.getAbsolutePath(), e);
    } catch (IllegalArgumentException | IIOException e) {
      LOG.error("Can't read photo " + file.getAbsolutePath(), e);
      addError(Constants.ALL, file.getAbsolutePath(), e);
    } finally {
      try {
        if (fis != null) {
          fis.close();
        }
        if (bis != null) {
          bis.close();
        }
      } catch (IOException ex) {
        LOG.error("Can't close input stream " + file.getAbsolutePath(), ex);
      }
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
    String pathToPhotoInPhotosLibrary = StringUtils.replaceOnce(photoOriginal.getAbsolutePath(), photosPath, Constants.EMPTY_STRING);
    return Paths.get(appDir, cacheDir, width + "x" + height, pathToPhotoInPhotosLibrary);
  }

  public static PhotoLight getPhotoLightFromFile(@NotNull File file, @Nullable Date date) throws IOException {
    PhotoLight photoLight = new PhotoLight();
    photoLight.setPath(file.getPath());
    photoLight.setSize(file.length());
    photoLight.setIsDirectory(file.isDirectory());
    photoLight.setDate(PhotosExplorer.getPhotoDate(file));
    photoLight.setDateLastModified(new Date(file.lastModified()));
    Dimension photoDimensions = PhotosExplorer.getDimensions(file);
    photoLight.setHeight((long) photoDimensions.getHeight());
    photoLight.setWidth((long) photoDimensions.getWidth());
    photoLight.setSelected(date);
    return photoLight;
  }

  public static java.util.List getPhotosInError() {
    return photosInError;
  }

  public static void addError(String kind, String absolutePath, Exception e) {
    photosInError.add(new PhotoError(kind, absolutePath, e.getClass() + " " + e.getMessage()));
  }

  public static void resetErrors() {
    photosInError.clear();
  }
}
