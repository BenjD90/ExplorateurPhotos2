package com.benjd90.photos2.dao;

import com.benjd90.photos2.utils.PhotosUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.MemoryCacheImageOutputStream;
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

      byte[] imageInByte = getBufferedImageBytes(resizedImageHintJpg);
      return imageInByte;
    } else {
      return null;
    }
  }

  private byte[] getBufferedImageBytes(BufferedImage resizedImageHintJpg) throws IOException {
    JPEGImageWriteParam jpegParams = new JPEGImageWriteParam(null);
    jpegParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
    jpegParams.setCompressionQuality(0.5f);


    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    final ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();

    writer.setOutput(new MemoryCacheImageOutputStream(outputStream));
    writer.write(null, new IIOImage(resizedImageHintJpg, null, null), jpegParams);

    byte[] imageInByte = outputStream.toByteArray();
    outputStream.close();
    return imageInByte;
  }
}
