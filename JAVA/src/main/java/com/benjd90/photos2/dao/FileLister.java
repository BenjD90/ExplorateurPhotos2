package com.benjd90.photos2.dao;

import com.benjd90.photos2.beans.PhotoLight;
import com.benjd90.photos2.beans.PhotosListStorage;
import com.benjd90.photos2.utils.ConfigReader;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Class to read file structure
 * Created by Benjamin on 11/02/2016.
 */
@Repository("FileLister")
public class FileLister implements IFileLister {
  private static final Logger LOG = LoggerFactory.getLogger(FileLister.class);


  @Override
  public List<PhotoLight> getAllPhotosExisting() throws IOException {
    ObjectMapper om = new ObjectMapper();
    File storageFile = new File(ConfigReader.getMessage(ConfigReader.KEY_APP_DIR), ConfigReader.getMessage(ConfigReader.KEY_FILENAME_LIST_PHOTOS));
    PhotosListStorage photosListStorage = om.readValue(storageFile, PhotosListStorage.class);
    return photosListStorage.getPhotos();
  }
}
