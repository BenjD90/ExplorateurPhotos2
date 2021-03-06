package com.benjd90.photos2.scheduler;

import com.benjd90.photos2.utils.PhotosUtils;
import com.sun.nio.file.ExtendedWatchEventModifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

/**
 * Created by Benjamin on 11/03/2016
 */
public class FileChangeDetectorThread extends Thread {
  private static final Logger LOG = LoggerFactory.getLogger(FileChangeDetectorThread.class);
  private ScanOnFileChange scanOnFileChange;

  public FileChangeDetectorThread(ScanOnFileChange scanOnFileChange) {
    this.scanOnFileChange = scanOnFileChange;
  }


  @Override
  public void run() {
    Path dir = new File(scanOnFileChange.getPhotosPath()).toPath();
    WatchService watcher;
    try {
      watcher = FileSystems.getDefault().newWatchService();
    } catch (IOException e) {
      LOG.error("Can't start watchService", e);
      return;
    }

    WatchEvent.Kind<?>[] eventsArray = {
            StandardWatchEventKinds.ENTRY_CREATE,
            StandardWatchEventKinds.ENTRY_DELETE,
            StandardWatchEventKinds.ENTRY_MODIFY
    };

    try {
      WatchKey key = dir.register(watcher, eventsArray, ExtendedWatchEventModifier.FILE_TREE);
    } catch (IOException e) {
      LOG.error("Can't register watchService", e);
    }

    while (true) {

      // wait for key to be signaled
      WatchKey key;
      try {
        key = watcher.take();
      } catch (InterruptedException x) {
        return;
      }

      for (WatchEvent<?> event : key.pollEvents()) {
        WatchEvent.Kind<?> kind = event.kind();
        // This key is registered only for ENTRY_CREATE events,
        // but an OVERFLOW event can occur regardless if events
        // are lost or discarded.
        if (kind == StandardWatchEventKinds.OVERFLOW) {
          LOG.warn("OVERFLOW");
          continue;
        }

        // The filename is the  context of the event.
        @SuppressWarnings("unchecked")
        WatchEvent<Path> ev = (WatchEvent<Path>) event;
        Path filename = Paths.get(scanOnFileChange.getPhotosPath(), ev.context().toString());
        if (PhotosUtils.isPhoto(filename.toFile())) {
          @SuppressWarnings("unchecked")
          WatchEvent.Kind<Path> pathKind = (WatchEvent.Kind<Path>) kind;
          scanOnFileChange.onChange(filename, pathKind);
        }
      }

      // Reset the key -- this step is critical if you want to
      // receive further watch events.  If the key is no longer valid,
      // the directory is inaccessible so exit the loop.
      boolean valid = key.reset();
      if (!valid) {
        break;
      }
    }
  }
}
