package com.benjd90.photos2.scheduler;

import com.benjd90.photos2.beans.FileLight;
import com.benjd90.photos2.beans.PhotoLight;
import com.benjd90.photos2.beans.PhotosListStorage;
import com.benjd90.photos2.beans.State;
import com.benjd90.photos2.beans.comparator.PhotoLightDefaultComparator;
import com.benjd90.photos2.scheduler.utils.PhotosExplorer;
import com.benjd90.photos2.utils.Constants;
import com.benjd90.photos2.utils.PhotosUtils;
import com.benjd90.photos2.utils.SchedulerUtils;
import com.benjd90.photos2.utils.TimeUtils;
import org.ini4j.Ini;
import org.ini4j.Profile;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * Job that write the file : listPhotos.json
 * Created by Benjamin on 23/02/2016.
 */
public class ScanJob implements Job {
  private static final Logger LOG = LoggerFactory.getLogger(ScanJob.class);
  public static final String PICASA_INI = ".picasa.ini";
  private static final String YES = "\"yes\"";

  private String appDirectory;

  private String isRunningFileName;

  private String listFilesFileName;

  private String photosPath;

  private Integer thumbnailHeight;

  private State state;

  public void execute(JobExecutionContext context)
          throws JobExecutionException {
    this.state = (State) context.getMergedJobDataMap().get(Constants.STATE);
    this.appDirectory = (String) context.getMergedJobDataMap().get("appDirectory");
    this.isRunningFileName = (String) context.getMergedJobDataMap().get("isRunningFileName");
    this.listFilesFileName = (String) context.getMergedJobDataMap().get("listFilesFileName");
    this.photosPath = (String) context.getMergedJobDataMap().get("photosPath");
    this.thumbnailHeight = (Integer) context.getMergedJobDataMap().get("thumbnailHeight");
    run();
  }

  public void run(State state, String appDirectory, String isRunningFileName, String listFilesFileName, String photosPath, Integer thumbnailHeight) {
    this.state = state;
    this.appDirectory = appDirectory;
    this.isRunningFileName = isRunningFileName;
    this.listFilesFileName = listFilesFileName;
    this.photosPath = photosPath;
    this.thumbnailHeight = thumbnailHeight;
    run();
  }

  private void run() {
    long start = System.currentTimeMillis();
    LOG.info("Start scan job of appDirectory : " + this.photosPath);
    state.setLastStart(start);
    state.setStep("INIT");

    File appDirectoryFile = new File(this.appDirectory);
    if (!appDirectoryFile.exists()) {
      if (!appDirectoryFile.mkdir()) {
        LOG.error("Can't create appDirectory" + appDirectoryFile.getAbsolutePath());
        state.setLastEnd(System.currentTimeMillis());
        state.setLastRunEndState(Constants.KO + 1);
        return;
      }
    }

    File isRunningToken = new File(this.appDirectory, this.isRunningFileName);
    if (!isRunningToken.exists()) {
      try {
        LOG.info("Scan START " + this.appDirectory);
        PhotosUtils.resetErrors();
        launchScan();
        runScan();
        LOG.info("Scan END OK" + this.appDirectory);
        endScan();
        state.setLastRunEndState(Constants.OK);
      } catch (IOException e) {
        LOG.error("IO Error while scanning", e);
        LOG.info("Scan END KO " + this.appDirectory);
        state.setLastRunEndState(Constants.KO);
        state.setStep("");
        try {
          PrintWriter writer = new PrintWriter(new FileOutputStream(new File(this.isRunningFileName)), true);
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
    state.setStep("DONE");

    LOG.info("End scan job in " + TimeUtils.getTime(start) + ". " + this.appDirectory);
  }

  private synchronized void runScan() throws IOException {
    state.setStep("SCAN, build index");
    List<PhotoLight> photos = getListOfPhotosRecursively(photosPath);

    PhotosListStorage objectToStore = new PhotosListStorage();
    objectToStore.setDateScan(new Date());
    Collections.sort(photos, new PhotoLightDefaultComparator());
    objectToStore.setPhotos(photos);

    SchedulerUtils.storeScanResultToFile(objectToStore, appDirectory, listFilesFileName);
    state.setStep("SCAN, create cache");
    LOG.info("End build index, create cache");
    createThumbnailsCache(photos);
  }

  private void createThumbnailsCache(List<PhotoLight> photos) throws IOException {
    int size = photos.size();
    int i = 1;
    for (PhotoLight photo : photos) {
      File photoOriginal = new File(photo.getPath());

      BasicFileAttributes attr = Files.readAttributes(photoOriginal.toPath(), BasicFileAttributes.class);
      state.setActualPath(photo.getPath());
      state.setStep("SCAN, create cache (" + i + "/" + size + ")");
      long modificationTime = attr.creationTime().toMillis();

      if (state.getLastRunEndState() == null || (state.getLastRunEndState().equals(Constants.OK) && (modificationTime == 0 || modificationTime > state.getLastStart()))) {
        Path thumbnailPath = PhotosUtils.getThumbnailPath(null, thumbnailHeight, photoOriginal);
        File thumbnailCacheFile = thumbnailPath.toFile();
        PhotosUtils.createCacheThumbnail(null, thumbnailHeight, photoOriginal, thumbnailCacheFile);
      }
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


  private List<PhotoLight> getListOfPhotos(String directoryPath) throws IOException {
    File directory = new File(directoryPath);
    File[] list = directory.listFiles();
    List<PhotoLight> ret = new ArrayList<>();


    HashMap<String, Date> selectedPhotosInDirectory = new HashMap<>();
    if (isTherePicasaIniHere(list)) {
      selectedPhotosInDirectory.putAll(readPicasaIniFile(Paths.get(directoryPath, PICASA_INI)));
    }

    if (list != null) {
      for (File file : list) {
        this.state.setActualPath(file.getAbsolutePath());
        // add all, even directories
        PhotoLight photoLight = PhotosUtils.getPhotoLightFromFile(file, selectedPhotosInDirectory.get(file.getName()));
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
      state.setStep("SCAN, build index (" + ret.size() + ")");
    }

    ret = PhotosExplorer.filterOnlyPhotos(ret);

    return ret;
  }

  private boolean isTherePicasaIniHere(File[] files) {
    for (File file : files) {
      if (file.getName().equals(PICASA_INI)) {
        return true;
      }
    }
    return false;
  }

  private HashMap<String, Date> readPicasaIniFile(Path path) throws IOException {
    Ini ini = new Ini(path.toFile());
    HashMap<String, Date> ret = new HashMap<>();
    for (String sectionName : ini.keySet()) {
      Profile.Section section = ini.get(sectionName);

      String date = null;
      String star = section.get("star");
      String starred = section.get("stared");
      if (star != null) {
        date = star;
      } else if (starred != null) {
        date = starred;
      }

      if (date != null) {
        if (date.equals(YES)) {
          ret.put(sectionName, new Date(0));
        } else {
          try {
            ret.put(sectionName, new Date(Long.valueOf(date) * 1000));
          } catch (NumberFormatException e) {
            ret.put(sectionName, new Date(0));
          }
        }
      }
    }
    return ret;
  }
}
