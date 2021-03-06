package com.benjd90.photos2.dao;

import com.benjd90.photos2.beans.PhotoLight;

import java.io.IOException;
import java.util.List;

/**
 * Class to read file structure
 * Created by Benjamin on 11/02/2016.
 */
public interface IFileLister {

  List<PhotoLight> getAllPhotosExisting() throws IOException;
}
