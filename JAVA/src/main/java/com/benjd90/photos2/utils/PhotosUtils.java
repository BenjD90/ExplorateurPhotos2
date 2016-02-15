package com.benjd90.photos2.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * Created by Benjamin on 15/02/2016.
 */
public class PhotosUtils {
    private static final Logger LOG = LoggerFactory.getLogger(PhotosUtils.class);

    //TODO : Task too long, migrate to Spring Batch or Quartz only
    //https://docs.oracle.com/javase/tutorial/essential/io/notification.html
    public static Date getPhotoDate(File file) throws IOException {
        Date editDate = new Date(file.lastModified());
        Date creationDate = null;
        Date takenDate = null;
        if (file.isFile()) {
//            try {
//                Metadata metadata = ImageMetadataReader.readMetadata(file);
//                Directory directoryMetadata = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
//                if (directoryMetadata != null) {
//                    Date date = directoryMetadata.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL, TimeZone.getTimeZone(ZoneId.systemDefault()));
//                    if (date != null && date.getTime() > 0) {
//                        takenDate = date;
//                    }
//                }
//            } catch (ImageProcessingException e) {
//                LOG.debug("Can't read photo", e);
//            }
//
//            BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
//            creationDate = new Date(attr.creationTime().toMillis());
        }

        if (takenDate != null) {
            return takenDate;
        } else if (creationDate != null && editDate.after(creationDate)) {
            return creationDate;
        } else {
            return editDate;
        }
    }
}
