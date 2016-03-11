package com.benjd90.photos2.scheduler;

import com.benjd90.photos2.beans.FileChangeEvent;
import com.benjd90.photos2.beans.PhotoLight;
import com.benjd90.photos2.beans.PhotosListStorage;
import com.benjd90.photos2.utils.ConfigReader;
import com.benjd90.photos2.utils.PhotosUtils;
import com.benjd90.photos2.utils.SchedulerUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.util.*;

/**
 * Created by Benjamin on 11/03/2016.
 * <p/>
 * src : https://docs.oracle.com/javase/tutorial/essential/io/notification.html#try
 */
@Component
public class ScanOnFileChange {
  private static final Logger LOG = LoggerFactory.getLogger(ScanOnFileChange.class);
  private final String appDirectory = ConfigReader.getMessage(ConfigReader.KEY_APP_DIR);
  private final String isRunningFileName = ConfigReader.getMessage(ConfigReader.KEY_IS_RUNNING_FILE_NAME);
  private final String listFilesFileName = ConfigReader.getMessage(ConfigReader.KEY_FILENAME_LIST_PHOTOS);
  private LinkedList<FileChangeEvent> filesToTreat = new LinkedList<>();

  public ScanOnFileChange() throws IOException {
    FileChangeDetector detector = new FileChangeDetector(this);
    FileChangedTreatment fileChangedTreatment = new FileChangedTreatment(this);
    detector.start();
    fileChangedTreatment.start();
  }

  public void onChange(Path filename, WatchEvent.Kind<Path> kind) {
    FileChangeEvent fileChangeEvent = new FileChangeEvent();
    fileChangeEvent.setFile(filename.toFile());
    fileChangeEvent.setKind(kind);
    filesToTreat.add(fileChangeEvent);
  }


  public void treatAllFilesChanged() {
    File isRunningToken = new File(appDirectory, isRunningFileName);
    try {
      while (true) {
        int size = filesToTreat.size();
        if (size > 0 && !isRunningToken.exists()) {
          startUpdateScan();
          Set<File> filesToDelete = new HashSet<>();
          Set<File> filesToAdd = new HashSet<>();

          // treat "size" elements
          for (int i = 0; i < size; i++) {
            FileChangeEvent fileChanged = filesToTreat.get(i);

            if (fileChanged.getKind() == StandardWatchEventKinds.ENTRY_CREATE) {
              LOG.info("fichier ajoute " + fileChanged.getFile().getAbsolutePath());
              filesToAdd.add(fileChanged.getFile());
            } else if (fileChanged.getKind() == StandardWatchEventKinds.ENTRY_DELETE) {
              LOG.info("fichier supprime " + fileChanged.getFile().getAbsolutePath());
              filesToDelete.add(fileChanged.getFile());
            } else { // if (fileChanged.getKind() == StandardWatchEventKinds.ENTRY_MODIFY) {
              LOG.info("fichier modifie" + fileChanged.getFile().getAbsolutePath());
              filesToAdd.add(fileChanged.getFile());
              filesToDelete.add(fileChanged.getFile());
            }
          }

          // remove treated elements
          for (int i = 0; i < size; i++) {
            filesToTreat.removeFirst();
          }

          addAndRemoveFiles(filesToAdd, filesToDelete);

          endUpdateScan();
        }
        Thread.sleep(500);
      }
    } catch (IOException e) {
      LOG.error("Can't update list", e);
    } catch (InterruptedException e) {
      LOG.error("Can't update list", e);
    }
  }

  private void addAndRemoveFiles(Set<File> filesToAdd, Set<File> filesToDelete) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    File outFile = new File(appDirectory, listFilesFileName);
    PhotosListStorage objectStored = mapper.readValue(outFile, PhotosListStorage.class);
    List<PhotoLight> photos = objectStored.getPhotos();

    photos = removeAllFilesFromIndex(filesToDelete, photos);
    photos = addAllFilesToIndex(filesToAdd, photos);

    //TODO add remove and add for cache
    objectStored.setDateScan(new Date());
    SchedulerUtils.storeScanResultToFile(objectStored, appDirectory, listFilesFileName);
  }

  private List<PhotoLight> removeAllFilesFromIndex(Set<File> filesToDelete, List<PhotoLight> photos) {
    for (File file : filesToDelete) {
      PhotoLight photoToRemove = new PhotoLight();
      photoToRemove.setPath(file.getAbsolutePath());
      photos.remove(photoToRemove);
    }
    return photos;
  }

  private List<PhotoLight> addAllFilesToIndex(Set<File> filesToAdd, List<PhotoLight> photos) throws IOException {
    for (File file : filesToAdd) {
      if (PhotosUtils.isPhoto(file)) {
        PhotoLight photoToAdd = PhotosUtils.getPhotoLightFromFile(file);
        photos.add(photoToAdd);
      }
    }
    return photos;
  }

  private void startUpdateScan() throws IOException {
    SchedulerUtils.createRunningFile(appDirectory, isRunningFileName);
  }

  private void endUpdateScan() throws IOException {
    SchedulerUtils.deleteRunningFile(appDirectory, isRunningFileName);
  }

}
