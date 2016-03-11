package com.benjd90.photos2.scheduler;

import com.benjd90.photos2.beans.FileLight;
import com.benjd90.photos2.beans.PhotoLight;
import com.benjd90.photos2.beans.PhotosListStorage;
import com.benjd90.photos2.beans.State;
import com.benjd90.photos2.beans.comparator.PhotoLightDefaultComparator;
import com.benjd90.photos2.scheduler.utils.PhotosExplorer;
import com.benjd90.photos2.utils.ConfigReader;
import com.benjd90.photos2.utils.Constants;
import com.benjd90.photos2.utils.TimeUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.CharEncoding;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Job that write the file : listPhotos.json
 * Created by Benjamin on 23/02/2016.
 */
//TODO add scan on file change https://docs.oracle.com/javase/tutorial/essential/io/notification.html
public class ScanJob implements Job {
  private static final Logger LOG = LoggerFactory.getLogger(ScanJob.class);

  private final String appDirectory = ConfigReader.getMessage(ConfigReader.KEY_APP_DIR);
  private String isRunningFileName = "isRunning.txt";
  private String listFilesFileName = ConfigReader.getMessage(ConfigReader.KEY_FILENAME_LIST_PHOTOS);
  private State state;

  public void execute(JobExecutionContext context)
          throws JobExecutionException {
    long start = System.currentTimeMillis();
    state = (State) context.getMergedJobDataMap().get(Constants.STATE);
    LOG.info("Start scan job of appDirectory : " + ConfigReader.getMessage(ConfigReader.KEY_PATH));
    state.setLastStart(start);

    File directoryFile = new File(appDirectory);
    if (!directoryFile.exists()) {
      directoryFile.mkdir();
    }

    File isRunningToken = new File(appDirectory, isRunningFileName);
    if (!isRunningToken.exists()) {
      try {
        LOG.info("Scan START " + appDirectory);
        launchScan();
        runScan();
        LOG.info("Scan END OK" + appDirectory);
        endScan();
      } catch (IOException e) {
        LOG.error("IO Error while scanning", e);
        LOG.info("Scan END KO" + appDirectory);
        state.setLastRunEndState(Constants.KO);
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

    state.setActualPath(Constants.EMPTY_STRING);
    state.setLastEnd(System.currentTimeMillis());
    state.setLastRunEndState(Constants.OK);

    LOG.info("End scan job in " + TimeUtils.getTime(start) + ". " + appDirectory);
  }

  private synchronized void runScan() throws IOException {
    List<PhotoLight> photos = getListOfPhotosRecursively(ConfigReader.getMessage(ConfigReader.KEY_PATH));
    ObjectMapper mapper = new ObjectMapper();

    PhotosListStorage objectToStore = new PhotosListStorage();
    objectToStore.setDateScan(new Date());
    Collections.sort(photos, new PhotoLightDefaultComparator());
    objectToStore.setPhotos(photos);

    File outTempFile = new File(appDirectory, listFilesFileName + ".tmp");
    File outFile = new File(appDirectory, listFilesFileName);
    mapper.writeValue(outTempFile, objectToStore);
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
    File fileToCreate = new File(appDirectory, isRunningFileName);
    if (!fileToCreate.exists()) {
      LOG.info("Create file " + fileToCreate.getAbsolutePath());
      PrintWriter writer = new PrintWriter(fileToCreate, CharEncoding.UTF_8);
      writer.println("start=" + new Date().getTime());
      writer.close();
    }
  }

  private void deleteRunningFile() throws FileNotFoundException {
    File fileToDelete = new File(appDirectory, isRunningFileName);
    if (fileToDelete.exists()) {
      LOG.info("Delete file " + fileToDelete.getAbsolutePath());
      fileToDelete.delete();
    }
  }


  private List<PhotoLight> getListOfPhotos(String path) throws IOException {
    File directory = new File(path);
    File[] list = directory.listFiles();
    List<PhotoLight> ret = new ArrayList<>();

    if (list != null) {
      for (File file : list) {
        PhotoLight photoLight = new PhotoLight();
        photoLight.setPath(file.getPath());
        this.state.setActualPath(photoLight.getPath());
        photoLight.setSize(file.length());
        photoLight.setIsDirectory(file.isDirectory());
        photoLight.setDate(PhotosExplorer.getPhotoDate(file));
        photoLight.setDateLastModified(new Date(file.lastModified()));
        Dimension photoDimensions = PhotosExplorer.getDimensions(file);
        photoLight.setHeight((long) photoDimensions.getHeight());
        photoLight.setWidth((long) photoDimensions.getWidth());
        ret.add(photoLight);
      }
    }

    return ret;
  }

  public List<PhotoLight> getListOfPhotosRecursively(String path) throws IOException {
    List<PhotoLight> ret = getListOfPhotos(path);

    for (int i = 0; i < ret.size(); i++) {
      FileLight f = ret.get(i);
      if (f.getIsDirectory()) {
        ret.addAll(getListOfPhotos(f.getPath()));
      }
    }

    ret = PhotosExplorer.filterOnlyPhotos(ret);

    return ret;
  }

}
