package com.benjd90.photos2.controller;

import com.benjd90.photos2.beans.PhotoLight;
import com.benjd90.photos2.dao.IFileLister;
import com.benjd90.photos2.dao.IPhotosDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
public class PhotosController {

  private static final Logger LOG = LoggerFactory.getLogger(PhotosController.class);


  @Resource(name = "FileLister")
  private IFileLister fileLister;

  @Resource(name = "PhotosDao")
  private IPhotosDao photosDao;

  @RequestMapping(value = "/photos", method = RequestMethod.GET)
  @ResponseBody
  public List<PhotoLight> photos() throws IOException {
    return fileLister.getAllPhotosExisting();
  }

  @RequestMapping(value = "/thumbnail", method = RequestMethod.GET)
  @ResponseBody
  public ResponseEntity<byte[]> getThumbnail(HttpServletRequest req, @RequestParam String path, @RequestParam(required = false) Integer width, @RequestParam(required = false) Integer height) throws IOException {
    final HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.IMAGE_JPEG);

    return new ResponseEntity<>(photosDao.getThumbnail(path, width, height), headers, HttpStatus.CREATED);
  }


}