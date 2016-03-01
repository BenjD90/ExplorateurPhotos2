package com.benjd90.photos2.scheduler;

import com.benjd90.photos2.beans.PhotoLight;
import com.benjd90.photos2.beans.PhotosListStorage;
import com.benjd90.photos2.scheduler.utils.PhotosExplorer;
import com.benjd90.photos2.utils.ConfigReader;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.CharEncoding;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Date;
import java.util.List;

/**
 * Job that write the file : listPhotos.json
 * Created by Benjamin on 23/02/2016.
 */
public class ScanJob implements Job {
  private static final Logger LOG = LoggerFactory.getLogger(ScanJob.class);

  private final String directory = ConfigReader.getMessage(ConfigReader.KEY_APP_DIR);
  private String isRunningFileName = "isRunning.txt";
  private String listFilesFileName = ConfigReader.getMessage(ConfigReader.KEY_FILENAME_LIST_PHOTOS);

  public void execute(JobExecutionContext context)
          throws JobExecutionException {

    LOG.info("Start scan job " + directory);

    File directoryFile = new File(directory);
    if (!directoryFile.exists()) {
      directoryFile.mkdir();
    }

    File isRunningToken = new File(directory, isRunningFileName);
    if (!isRunningToken.exists()) {
      try {
        LOG.info("Scan START " + directory);
        launchScan();
        runScan();
        LOG.info("Scan END OK" + directory);
        endScan();
      } catch (IOException e) {
        LOG.error("IO Error while scanning", e);
        LOG.info("Scan END KO" + directory);
        try {
          PrintWriter writer = new PrintWriter(new FileOutputStream(new File(isRunningFileName)), true);
          writer.println("end=" + new Date().getTime());
          writer.println("status=ERR");
          writer.close();
        } catch (FileNotFoundException e1) {
          LOG.error("IO Error while ending scanning", e1);
        }
      }
    }

    LOG.info("End scan job" + directory);
  }

  private synchronized void runScan() throws IOException {
    List<PhotoLight> photos = PhotosExplorer.getListOfPhotosRecursively(ConfigReader.getMessage(ConfigReader.KEY_PATH));
    ObjectMapper mapper = new ObjectMapper();

    PhotosListStorage objectToStore = new PhotosListStorage();
    objectToStore.setDateScan(new Date());
    objectToStore.setPhotos(photos);

    File outTempFile = new File(directory, listFilesFileName + ".tmp");
    File outFile = new File(directory, listFilesFileName);
    mapper.writeValue(outTempFile, objectToStore);
    try {
      Thread.sleep(10000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    if (outFile.delete()) {
      if (!outTempFile.renameTo(outFile)) {
        throw new IOException("Can't rename tmp file to nominal" + listFilesFileName);
      }
    }
  }


  private void launchScan() throws IOException {
    createRunningFile();
  }

  private void endScan() throws FileNotFoundException {
    LOG.info("DEB End scan");
    deleteRunningFile();
    LOG.info("FIN End scan");
  }


  private void createRunningFile() throws IOException {
    File fileToCreate = new File(directory, isRunningFileName);
    if (!fileToCreate.exists()) {
      LOG.info("Create file " + fileToCreate.getAbsolutePath());
      PrintWriter writer = new PrintWriter(fileToCreate, CharEncoding.UTF_8);
      writer.println("start=" + new Date().getTime());
      writer.close();
    }
  }

  private void deleteRunningFile() throws FileNotFoundException {
    File fileToDelete = new File(directory, isRunningFileName);
    if (fileToDelete.exists()) {
      LOG.info("Delete file " + fileToDelete.getAbsolutePath());
      fileToDelete.delete();
    }
  }


}
