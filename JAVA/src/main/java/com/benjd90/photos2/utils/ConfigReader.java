package com.benjd90.photos2.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Benjamin on 11/02/2016.
 */
public class ConfigReader {

  public static final String KEY_PATH = "path";
  public static final String KEY_APP_DIR = "appFilesDir";
  public static final String KEY_CACHE_DIR = "cacheDir";
  public static final String KEY_PHOTOS_EXTENSION = "photosExtensions";
  public static final String KEY_FILENAME_LIST_PHOTOS = "listPhotosFileName";
  public static final String KEY_THUMBNAIL_HEIGHT = "thumbnailHeight";
  public static final String KEY_CRON_SCHEDULER = "cronScheduler";

  /**
   * LOGGER For the class.
   */
  private static final Logger LOG = LoggerFactory.getLogger(ConfigReader.class);


  /**
   * Properties file.
   */
  private static final java.util.Properties PROPS = new java.util.Properties();

  private static final String PROPERTIES_FILE = "config.properties";
  public static final String KEY_IS_RUNNING_FILE_NAME = "isRunningFileName";


  static {
    final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    final InputStream streamDB = classLoader.getResourceAsStream(PROPERTIES_FILE);
    try {
      PROPS.load(streamDB);
    } catch (final IOException e) {
      LOG.error("Acces problem to the propertie file:  (" + PROPERTIES_FILE + ")", e);
    }
  }

  private static List<String> photosExtensions;

  /**
   * Constructor for this class.
   */
  private ConfigReader() {
  }

  /**
   * Method to get a message by a key.
   *
   * @param key we want to get
   * @return the message associate to this key
   */
  public static String getMessage(final String key) {
    if (PROPS.getProperty(key) != null) {
      return PROPS.getProperty(key);
    } else {
      LOG.error(key + " not found in file " + PROPERTIES_FILE);
      return "";
    }
  }

  public static List<String> getPhotosExtensions() {
    String[] values = getMessage(ConfigReader.KEY_PHOTOS_EXTENSION).toLowerCase().split(Constants.SEMICOLON);
    return Arrays.asList(values);
  }
}
