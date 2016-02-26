package com.benjd90.photos2.scheduler;

import org.apache.commons.lang3.CharEncoding;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Created by Benjamin on 23/02/2016.
 */
public class ScanJob implements Job {
    private static final Logger LOG = LoggerFactory.getLogger(ScanJob.class);

    private final String directory = System.getProperty("java.io.tmpdir") + "ExplorerPhotos2";
    private String isRunningFileName = "isRunning.txt";

    //TODO add config for nb thread, remove synchronized
    public synchronized void execute(JobExecutionContext context)
            throws JobExecutionException {

        LOG.info("Start scan " + directory);

        File directoryFile = new File(directory);
        if(!directoryFile.exists()) {
            directoryFile.mkdir();
        }

        File isRunningToken = new File(directory, isRunningFileName);
        if (!isRunningToken.exists()) {
            try {
                launchScan();
            } catch (IOException e) {
                LOG.error("IO Error while scanning", e);
            } finally {
                try {
                    endScan();
                } catch (FileNotFoundException e) {
                    LOG.error("IO Error while ending scanning", e);
                }
            }
        }

        LOG.info("End scan " + directory);
    }


    private void launchScan() throws IOException {
        createRunningFile();
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void endScan() throws FileNotFoundException {
        LOG.info("DEB End scan");
        deleteRunningFile();
        LOG.info("FIN End scan");
    }


    private void createRunningFile() throws IOException {
        File fileToCreate = new File(directory, isRunningFileName);
        if (!fileToCreate.exists()) {
            LOG.info("Create file " + fileToCreate.getAbsolutePath());
            PrintWriter writer = new PrintWriter(fileToCreate, CharEncoding.UTF_8);
            writer.println("start=" + new Date().getTime());
            writer.close();
        }
    }

    private void deleteRunningFile() throws FileNotFoundException {
        File fileToDelete = new File(directory, isRunningFileName);
        if(fileToDelete.exists()) {
            LOG.info("Delete file " + fileToDelete.getAbsolutePath());
            fileToDelete.delete();
        }
    }


}
