package com.benjd90.photos2.utils;

import com.benjd90.photos2.beans.PhotosListStorage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.CharEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

/**
 * Created by Benjamin on 11/03/2016.
 */
public class SchedulerUtils {
  private static final Logger LOG = LoggerFactory.getLogger(SchedulerUtils.class);

  public static void createRunningFile(String appDirectory, String isRunningFileName) throws IOException {
    File fileToCreate = new File(appDirectory, isRunningFileName);
    if (!fileToCreate.exists()) {
      LOG.info("Create file " + fileToCreate.getAbsolutePath());
      PrintWriter writer = new PrintWriter(fileToCreate, CharEncoding.UTF_8);
      writer.println("start=" + new Date().getTime());
      writer.close();
    }
  }

  public static void deleteRunningFile(String appDirectory, String isRunningFileName) throws FileNotFoundException {
    File fileToDelete = new File(appDirectory, isRunningFileName);
    if (fileToDelete.exists()) {
      LOG.info("Delete file " + fileToDelete.getAbsolutePath());
      if (!fileToDelete.delete()) {
        LOG.error("Can't delete file : " + fileToDelete.getAbsolutePath());
      }
    }
  }


  public synchronized static void storeScanResultToFile(PhotosListStorage objectToStore, String appDirectory, String listFilesFileName) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    File outTempFile = new File(appDirectory, listFilesFileName + ".tmp");
    File outFile = new File(appDirectory, listFilesFileName);
    mapper.writeValue(outTempFile, objectToStore);
    if (outFile.delete()) {
      if (!outTempFile.renameTo(outFile)) {
        throw new IOException("Can't rename tmp file to nominal" + listFilesFileName);
      }
    }
  }
}
