package com.benjd90.photos2.dao;

import com.benjd90.photos2.utils.PhotosUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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

      Path thumbnailPath = PhotosUtils.getThumbnailPath(width, height, photoOriginal);
      File thumbnailCacheFile = thumbnailPath.toFile();

      if (thumbnailCacheFile.exists()) {
        return Files.readAllBytes(thumbnailPath);
      } else {
        return PhotosUtils.createCacheThumbnail(width, height, photoOriginal, thumbnailCacheFile);
      }
    } else {
      return null;
    }
  }

}
