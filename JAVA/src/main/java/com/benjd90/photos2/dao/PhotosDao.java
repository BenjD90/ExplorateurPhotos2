package com.benjd90.photos2.dao;

import com.benjd90.photos2.utils.PhotosUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

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
      BufferedImage originalImage = ImageIO.read(photoOriginal);

      int widthOriginal = originalImage.getWidth();
      int heightOriginal = originalImage.getHeight();
      float ratio = ((float) widthOriginal) / heightOriginal;

      if (width == null && height == null) { // no resize
        return Files.readAllBytes(photoOriginal.toPath());
      } else if (height == null && width != null) {
        height = (int) (width * ratio);
      } else if (width == null && height != null) {
        width = (int) (height * ratio);
      }

      BufferedImage resizedImageHintJpg = PhotosUtils.resizeImage(originalImage, width, height);

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ImageIO.write(resizedImageHintJpg, "jpg", baos);
      baos.flush();
      byte[] imageInByte = baos.toByteArray();
      baos.close();
      return imageInByte;
    } else {
      return null;
    }
  }
}
