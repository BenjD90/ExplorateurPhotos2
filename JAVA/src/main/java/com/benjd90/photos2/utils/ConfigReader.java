package com.benjd90.photos2.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Benjamin on 11/02/2016.
 */
public class ConfigReader {

    public static final String KEY_PATH = "path";

    /**
     * LOGGER For the class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ConfigReader.class);


    /**
     * Properties file.
     */
    private static final java.util.Properties PROPS = new java.util.Properties();

    private static final String PROPERTIES_FILE = "config.properties";


    static {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final InputStream streamDB = classLoader.getResourceAsStream(PROPERTIES_FILE);
        try {
            PROPS.load(streamDB);
        } catch (final IOException e) {
            LOG.error("Acces problem to the propertie file:  (" + PROPERTIES_FILE + ")", e);
        }
    }

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
        return PROPS.getProperty(key);
    }
}
