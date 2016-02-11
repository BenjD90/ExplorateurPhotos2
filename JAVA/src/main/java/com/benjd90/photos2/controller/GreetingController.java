package com.benjd90.photos2.controller;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import com.benjd90.photos2.beans.FileLight;
import com.benjd90.photos2.dao.FileLister;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api")
public class GreetingController {

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @Resource(name = "FileLister")
    private FileLister fileLister;

    @RequestMapping(value = "/photos", method = RequestMethod.GET)
    @ResponseBody
    public List<FileLight> photos(@RequestParam(value = "name", defaultValue = "World") String name) throws IOException {
        return fileLister.getListOfFiles("C:/");
    }
}