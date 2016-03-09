package com.benjd90.photos2.controller;

import com.benjd90.photos2.beans.State;
import com.benjd90.photos2.scheduler.ScanScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;

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

}