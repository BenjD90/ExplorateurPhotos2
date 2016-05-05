package com.benjd90.photos2.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Benjamin on 11/03/2016.
 */
public class FileChangedTreatment extends Thread {
  private static final Logger LOG = LoggerFactory.getLogger(ScanOnFileChange.class);

  private ScanOnFileChange scanOnFileChange;

  public FileChangedTreatment(ScanOnFileChange scanOnFileChange) {
    this.scanOnFileChange = scanOnFileChange;
  }

  @Override
  public void run() {
    scanOnFileChange.treatAllFilesChanged();
  }

}
