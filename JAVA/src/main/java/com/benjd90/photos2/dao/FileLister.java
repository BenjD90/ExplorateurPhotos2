package com.benjd90.photos2.dao;

import com.benjd90.photos2.beans.FileLight;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.FileHandler;

/**
 * Class to read file structure
 * Created by Benjamin on 11/02/2016.
 */
@Repository("FileLister")
public class FileLister implements IFileLister {
    @Override
    public List<FileLight> getListOfFiles(String path) throws IOException {
        File directory = new File(path);
        File[] list = directory.listFiles();
        List<FileLight> ret = new ArrayList<>();

        if (list != null) {
            for (File file : list) {
                FileLight fileLight = new FileLight();
                fileLight.setPath(file.getPath());
                fileLight.setSize(file.length());
                fileLight.setDateEdit(new Date(file.lastModified()));
                fileLight.setIsDirectory(file.isDirectory());
                ret.add(fileLight);
            }
        }

        return ret;
    }

    @Override
    public List<FileLight> getListOfFilesRecursively(String path) throws IOException {
        List<FileLight> ret = getListOfFiles(path);

        for (int i = 0; i < ret.size(); i++) {
            FileLight f = ret.get(i);
            if (f.getIsDirectory()) {
                ret.addAll(getListOfFiles(f.getPath()));
            }
        }

        return ret;
    }
}
