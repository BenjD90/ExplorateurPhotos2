package com.benjd90.photos2.dao;

import com.benjd90.photos2.beans.FileLight;
import com.benjd90.photos2.beans.PhotoLight;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.util.Date;
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
    public List<PhotoLight> getListOfPhotos(String path) throws IOException {
        File directory = new File(path);
        File[] list = directory.listFiles();
        List<PhotoLight> ret = new ArrayList<>();

        if (list != null) {
            for (File file : list) {
                PhotoLight photoLight = new PhotoLight();
                photoLight.setPath(file.getPath());
                photoLight.setSize(file.length());
                photoLight.setDateEdit(new Date(file.lastModified()));
                photoLight.setIsDirectory(file.isDirectory());
                ret.add(photoLight);
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

    @Override
    public List<PhotoLight> getListOfPhotosRecursively(String path) throws IOException {
        List<PhotoLight> ret = getListOfPhotos(path);

        for (int i = 0; i < ret.size(); i++) {
            FileLight f = ret.get(i);
            if (f.getIsDirectory()) {
                ret.addAll(getListOfPhotos(f.getPath()));
            }
        }

        ret = filterOnlyPhotos(ret);

        return ret;
    }

    private List<PhotoLight> filterOnlyPhotos(List<PhotoLight> listToFilter) {
        List<PhotoLight> ret = new ArrayList<>(listToFilter.size());
        for (PhotoLight p : listToFilter) {
            // TODO : externalize photos extension
            // TODO : Add use of exif
            if (FilenameUtils.getExtension(p.getPath()).equals("jpg")) {
                ret.add(p);
            }
        }
        return ret;
    }


}
