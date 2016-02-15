package com.benjd90.photos2.dao;

import com.benjd90.photos2.beans.FileLight;
import com.benjd90.photos2.beans.PhotoLight;
import com.benjd90.photos2.utils.ConfigReader;
import com.benjd90.photos2.utils.PhotosUtils;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.ZoneId;
import java.util.*;

/**
 * Class to read file structure
 * Created by Benjamin on 11/02/2016.
 */
@Repository("FileLister")
public class FileLister implements IFileLister {
    private static final Logger LOG = LoggerFactory.getLogger(FileLister.class);


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
                fileLight.setDate(new Date(file.lastModified()));
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
                photoLight.setIsDirectory(file.isDirectory());
                photoLight.setDate(PhotosUtils.getPhotoDate(file));
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
            // TODO : Add use of exif
            if (ConfigReader.getPhotosExtensions().contains(FilenameUtils.getExtension(p.getPath()).toLowerCase())) {
                ret.add(p);
            }
        }
        return ret;
    }


}
