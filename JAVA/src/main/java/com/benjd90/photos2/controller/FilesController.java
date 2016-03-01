package com.benjd90.photos2.controller;

import com.benjd90.photos2.beans.PhotoLight;
import com.benjd90.photos2.dao.FileLister;
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
public class FilesController {

  private static final Logger LOG = LoggerFactory.getLogger(FilesController.class);


  @Resource(name = "FileLister")
  private FileLister fileLister;

  @RequestMapping(value = "/photos", method = RequestMethod.GET)
  @ResponseBody
  public List<PhotoLight> photos() throws IOException {
    return fileLister.getAllPhotosExisting();
  }
}