package com.benjd90.photos2.controller;

import com.benjd90.photos2.beans.State;
import com.benjd90.photos2.scheduler.ScanJob;
import com.benjd90.photos2.scheduler.ScanScheduler;
import com.benjd90.photos2.utils.PhotosUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
public class SchecdulerController {

  private static final Logger LOG = LoggerFactory.getLogger(SchecdulerController.class);


  @Resource(name = "ScanScheduler")
  private ScanScheduler scanScheduler;


  @RequestMapping(value = "/state", method = RequestMethod.GET)
  @ResponseBody
  public State state() throws IOException {
    return scanScheduler.getState();
  }

  @RequestMapping(value = "/errors", method = RequestMethod.GET)
  @ResponseBody
  public List errors() throws IOException {
    return PhotosUtils.getPhotosInError();
  }

  @RequestMapping(value = "/launchScan", method = RequestMethod.GET)
  @ResponseBody
  public void launchScan() throws IOException {
    new Thread() {
      @Override
      public void run() {
        ScanJob scanJob = new ScanJob();
        scanJob.run(scanScheduler.getState());
      }
    }.start();
  }

}