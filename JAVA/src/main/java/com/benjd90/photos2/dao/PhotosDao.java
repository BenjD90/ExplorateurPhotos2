package com.benjd90.photos2.dao;

import com.benjd90.photos2.utils.ConfigReader;
import com.benjd90.photos2.utils.Constants;
import com.benjd90.photos2.utils.PhotosUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Class to read file structure
 * Created by Benjamin on 11/02/2016.
 */
@Repository("PhotosDao")
public class PhotosDao implements IPhotosDao {
  private static final Logger LOG = LoggerFactory.getLogger(PhotosDao.class);

  @Override
  public byte[] getThumbnail(String path, Integer width, Integer height) throws IOException {
    File photoOriginal = new File(path);
    if (photoOriginal.exists() && PhotosUtils.isPhoto(photoOriginal)) {

      if (width == null && height == null) { // return original photo
        return Files.readAllBytes(photoOriginal.toPath());
      }

      String pathToPhotoInPhotosLibrary = StringUtils.replaceOnce(photoOriginal.getAbsolutePath(), ConfigReader.getMessage(ConfigReader.KEY_PATH), Constants.EMPTY_STRING);
      Path thumbnailPath = Paths.get(ConfigReader.getMessage(ConfigReader.KEY_APP_DIR), ConfigReader.getMessage(ConfigReader.KEY_CACHE_DIR), width + "x" + height, pathToPhotoInPhotosLibrary);
      File thumbnailCacheFile = thumbnailPath.toFile();

      if (thumbnailCacheFile.exists()) {
        return Files.readAllBytes(thumbnailPath);
      } else {
        return createCacheThumbnail(width, height, photoOriginal, thumbnailCacheFile);
      }
    } else {
      return null;
    }
  }

  private byte[] createCacheThumbnail(Integer width, Integer height, File photoOriginal, File thumbnailCacheFile) throws IOException {
    if (!thumbnailCacheFile.getParentFile().exists()) {
      if (!thumbnailCacheFile.getParentFile().mkdirs()) {
        throw new IOException("Can't create cache directories");
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

    BufferedImage resizedImageHintJpg = PhotosUtils.resizeImage(originalImage, width, height);

    storeBufferedImage(resizedImageHintJpg, thumbnailCacheFile);
    return Files.readAllBytes(thumbnailCacheFile.toPath());
  }

  private void storeBufferedImage(BufferedImage resizedImageHintJpg, File thumbnailCacheFile) throws IOException {
    JPEGImageWriteParam jpegParams = new JPEGImageWriteParam(null);
    jpegParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
    jpegParams.setCompressionQuality(0.5f);


    final ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();

    writer.setOutput(ImageIO.createImageOutputStream(new FileOutputStream(thumbnailCacheFile)));
    writer.write(null, new IIOImage(resizedImageHintJpg, null, null), jpegParams);
  }
}
