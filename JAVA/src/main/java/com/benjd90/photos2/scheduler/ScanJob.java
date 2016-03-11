package com.benjd90.photos2.scheduler;

import com.benjd90.photos2.beans.FileLight;
import com.benjd90.photos2.beans.PhotoLight;
import com.benjd90.photos2.beans.PhotosListStorage;
import com.benjd90.photos2.beans.State;
import com.benjd90.photos2.beans.comparator.PhotoLightDefaultComparator;
import com.benjd90.photos2.scheduler.utils.PhotosExplorer;
import com.benjd90.photos2.utils.*;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
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
  private final String isRunningFileName = ConfigReader.getMessage(ConfigReader.KEY_IS_RUNNING_FILE_NAME);
  private String listFilesFileName = ConfigReader.getMessage(ConfigReader.KEY_FILENAME_LIST_PHOTOS);
  private State state;

  public void execute(JobExecutionContext context)
          throws JobExecutionException {
    long start = System.currentTimeMillis();
    state = (State) context.getMergedJobDataMap().get(Constants.STATE);
    LOG.info("Start scan job of appDirectory : " + ConfigReader.getMessage(ConfigReader.KEY_PATH));
    state.setLastStart(start);
    state.setStep("INIT");

    File appDirectoryFile = new File(appDirectory);
    if (!appDirectoryFile.exists()) {
      if (!appDirectoryFile.mkdir()) {
        LOG.error("Can't create appDirectory" + appDirectoryFile.getAbsolutePath());
        state.setLastEnd(System.currentTimeMillis());
        state.setLastRunEndState(Constants.KO + 1);
        return;
      }
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
        state.setStep("");
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
    state.setStep("DONE");

    LOG.info("End scan job in " + TimeUtils.getTime(start) + ". " + appDirectory);
  }

  private synchronized void runScan() throws IOException {
    state.setStep("SCAN, build index");
    List<PhotoLight> photos = getListOfPhotosRecursively(ConfigReader.getMessage(ConfigReader.KEY_PATH));

    PhotosListStorage objectToStore = new PhotosListStorage();
    objectToStore.setDateScan(new Date());
    Collections.sort(photos, new PhotoLightDefaultComparator());
    objectToStore.setPhotos(photos);

    SchedulerUtils.storeScanResultToFile(objectToStore, appDirectory, listFilesFileName);
    state.setStep("SCAN, create cache");
    createThumbnailsCache(photos);
  }

  private void createThumbnailsCache(List<PhotoLight> photos) throws IOException {
    Integer thumbnailHeight = Integer.valueOf(ConfigReader.getMessage(ConfigReader.KEY_THUMBNAIL_HEIGHT));
    int size = photos.size();
    int i = 1;
    for (PhotoLight photo : photos) {
      File photoOriginal = new File(photo.getPath());
      Path thumbnailPath = PhotosUtils.getThumbnailPath(null, thumbnailHeight, photoOriginal);
      File thumbnailCacheFile = thumbnailPath.toFile();
      state.setActualPath(photo.getPath());
      state.setStep("SCAN, create cache (" + i + "/" + size + ")");
      PhotosUtils.createCacheThumbnail(null, thumbnailHeight, photoOriginal, thumbnailCacheFile);
      i++;
    }
  }


  private void launchScan() throws IOException {
    SchedulerUtils.createRunningFile(appDirectory, isRunningFileName);
  }

  private void endScan() throws FileNotFoundException {
    LOG.debug("DEB End scan");
    SchedulerUtils.deleteRunningFile(appDirectory, isRunningFileName);
    LOG.debug("FIN End scan");
  }


  private List<PhotoLight> getListOfPhotos(String path) throws IOException {
    File directory = new File(path);
    File[] list = directory.listFiles();
    List<PhotoLight> ret = new ArrayList<>();

    if (list != null) {
      for (File file : list) {
        this.state.setActualPath(file.getAbsolutePath());
        PhotoLight photoLight = PhotosUtils.getPhotoLightFromFile(file);
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
