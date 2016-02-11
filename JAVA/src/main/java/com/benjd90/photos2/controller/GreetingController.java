package com.benjd90.photos2.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import com.benjd90.photos2.beans.FileLight;
import com.benjd90.photos2.dao.FileLister;
import com.benjd90.photos2.utils.ConfigReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api")
public class GreetingController {

    private static final Logger LOG = LoggerFactory.getLogger(GreetingController.class);


    @Resource(name = "FileLister")
    private FileLister fileLister;

    @RequestMapping(value = "/photos", method = RequestMethod.GET)
    @ResponseBody
    public List<FileLight> photos(@RequestParam(value = "name", defaultValue = "World") String name) throws IOException {
        long start = System.currentTimeMillis();
        List<FileLight> ret = fileLister.getListOfFilesRecursively(ConfigReader.getMessage(ConfigReader.KEY_PATH));
        LOG.info((System.currentTimeMillis()-start) + " ms");

        return ret;
    }
}