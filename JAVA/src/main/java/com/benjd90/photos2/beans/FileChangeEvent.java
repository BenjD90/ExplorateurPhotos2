package com.benjd90.photos2.beans;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.WatchEvent;

/**
 * Created by Benjamin on 11/03/2016.
 */
public class FileChangeEvent {
  private File file;
  private WatchEvent.Kind<Path> kind;

  public WatchEvent.Kind<Path> getKind() {
    return kind;
  }

  public void setKind(WatchEvent.Kind<Path> kind) {
    this.kind = kind;
  }

  public File getFile() {
    return file;
  }

  public void setFile(File file) {
    this.file = file;
  }
}
