package com.benjd90.photos2.scheduler;

/**
 * Created by Benjamin on 11/03/2016.
 */
public class FileChangedTreatment extends Thread {
  private ScanOnFileChange scanOnFileChange;

  public FileChangedTreatment(ScanOnFileChange scanOnFileChange) {

    this.scanOnFileChange = scanOnFileChange;
  }

  @Override
  public void run() {
    scanOnFileChange.treatAllFilesChanged();
  }

}
