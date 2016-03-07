package com.benjd90.photos2.utils;

import java.util.concurrent.TimeUnit;


/**
 * Created by Benjamin on 07/03/2016.
 */
public class TimeUtils {

  public static String getTime(long start) {
    long time = System.currentTimeMillis() - start;
    return String.format("%02d:%02d:%02d,%03d", TimeUnit.MILLISECONDS.toHours(time),
            TimeUnit.MILLISECONDS.toMinutes(time) % TimeUnit.HOURS.toMinutes(1),
            TimeUnit.MILLISECONDS.toSeconds(time) % TimeUnit.MINUTES.toSeconds(1),
            TimeUnit.MILLISECONDS.toMillis(time) % TimeUnit.SECONDS.toMillis(1));
  }
}
