package com.benjd90.photos2.controller;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import com.benjd90.photos2.beans.FileLight;
import com.benjd90.photos2.beans.PhotoLight;
import com.benjd90.photos2.dao.FileLister;
import com.benjd90.photos2.utils.ConfigReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api")
public class FilesController {

    private static final Logger LOG = LoggerFactory.getLogger(FilesController.class);


    @Resource(name = "FileLister")
    private FileLister fileLister;

    @RequestMapping(value = "/photos", method = RequestMethod.GET)
    @ResponseBody
    public List<PhotoLight> photos() throws IOException {
        return fileLister.getListOfPhotosRecursively(ConfigReader.getMessage(ConfigReader.KEY_PATH));
    }
}