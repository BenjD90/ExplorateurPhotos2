package com.benjd90.photos2.dao;

import java.io.IOException;

/**
 * Class to read photos
 * Created by Benjamin on 11/02/2016.
 */
public interface IPhotosDao {

  byte[] getThumbnail(String path, Integer width, Integer height) throws IOException;
}
