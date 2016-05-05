package com.benjd90.photos2.scheduler;

import com.benjd90.photos2.beans.FileChangeEvent;
import com.benjd90.photos2.beans.PhotoLight;
import com.benjd90.photos2.beans.PhotosListStorage;
import com.benjd90.photos2.beans.comparator.PhotoLightDefaultComparator;
import com.benjd90.photos2.utils.PhotosUtils;
import com.benjd90.photos2.utils.SchedulerUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
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

  @Value("${appFilesDir}")
  private String appDirectory;

  @Value("${isRunningFileName}")
  private String isRunningFileName;

  @Value("${listPhotosFileName}")
  private String listPhotosFileName;


  @Value("${path}")
  private String photosPath;

  @Value("${thumbnailHeight}")
  private Integer thumbnailHeight;

  public String getPhotosPath() {
    return photosPath;
  }

  private LinkedList<FileChangeEvent> filesToTreat = new LinkedList<>();

  @PostConstruct
  public void init() {
    FileChangeDetectorThread detector = new FileChangeDetectorThread(this);
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
        try {
          int size = filesToTreat.size();
          if (size > 0 && !isRunningToken.exists()) {
            startUpdateScan();
            LOG.info("Debut de traitement des modifications locales");
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

            LOG.info("Fin de traitement des modifications locales");
            endUpdateScan();
          }
          Thread.sleep(500);
        } catch (IOException e) {
          LOG.error("Can't update list", e);
        }
      }
    } catch (InterruptedException e) {
      LOG.error("Can't update list", e);
    }
  }

  private void addAndRemoveFiles(Set<File> filesToAdd, Set<File> filesToDelete) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    File outFile = new File(appDirectory, listPhotosFileName);
    PhotosListStorage objectStored = mapper.readValue(outFile, PhotosListStorage.class);
    List<PhotoLight> photos = objectStored.getPhotos();

    photos = removeAllFilesFromIndex(filesToDelete, photos);
    addAllFilesToIndex(filesToAdd, photos);

    removeAllThumbnails(filesToDelete, thumbnailHeight);
    addAllThumbnails(filesToAdd, thumbnailHeight);

    Collections.sort(photos, new PhotoLightDefaultComparator());

    objectStored.setDateScan(new Date());
    SchedulerUtils.storeScanResultToFile(objectStored, appDirectory, listPhotosFileName);
  }

  private void removeAllThumbnails(Set<File> filesToDelete, Integer thumbnailHeight) throws IOException {
    for (File photoOriginal : filesToDelete) {
      Path thumbnailPath = PhotosUtils.getThumbnailPath(null, thumbnailHeight, photoOriginal);
      if (thumbnailPath.toFile().exists() && !thumbnailPath.toFile().delete()) {
        throw new IOException("Can't delete thumbnail : " + thumbnailPath);
      }
    }
  }

  private void addAllThumbnails(Set<File> filesToDelete, Integer thumbnailHeight) throws IOException {
    for (File photoOriginal : filesToDelete) {
      File thumbnailCacheFile = PhotosUtils.getThumbnailPath(null, thumbnailHeight, photoOriginal).toFile();
      PhotosUtils.createCacheThumbnail(null, thumbnailHeight, photoOriginal, thumbnailCacheFile);
    }
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
        // a photo can't be already selected when added on live
        PhotoLight photoToAdd = PhotosUtils.getPhotoLightFromFile(file, null);
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
