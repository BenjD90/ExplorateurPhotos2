package com.benjd90.photos2.scheduler;

import com.benjd90.photos2.beans.State;
import com.benjd90.photos2.utils.Constants;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by Benjamin on 23/02/2016.
 */
@Component("ScanScheduler")
public class ScanScheduler {
  private static final Logger LOG = LoggerFactory.getLogger(ScanScheduler.class);
  private final State state = new State();
  private Scheduler scheduler = null;

  @Value("${cronScheduler}")
  private String cronScheduler;

  @Value("${appFilesDir}")
  private String appDirectory;

  @Value("${isRunningFileName}")
  private String isRunningFileName;

  @Value("${listPhotosFileName}")
  private String listFilesFileName;

  @Value("${path}")
  private String photosPath;

  @Value("${thumbnailHeight}")
  private Integer thumbnailHeight;

  @PostConstruct
  public void init() {
    JobDataMap jobData = new JobDataMap();
    jobData.put(Constants.STATE, state);
    jobData.put("cronScheduler", cronScheduler);
    jobData.put("appDirectory", appDirectory);
    jobData.put("isRunningFileName", isRunningFileName);
    jobData.put("listFilesFileName", listFilesFileName);
    jobData.put("photosPath", photosPath);
    jobData.put("thumbnailHeight", thumbnailHeight);

    JobDetail job = JobBuilder.newJob(ScanJob.class)
            .usingJobData(jobData)
            .withIdentity("scanJob", "group1").build();

    // Trigger the job to run on the next round minute
    Trigger trigger = TriggerBuilder
            .newTrigger()
            .withIdentity("scanTrigger", "group1")
            .withSchedule(CronScheduleBuilder.cronSchedule(cronScheduler))
            .build();


    // schedule it
    try {
      scheduler = new StdSchedulerFactory().getScheduler();
      scheduler.start();
      scheduler.scheduleJob(job, trigger);
      LOG.info("Job scheduled : " + trigger);
    } catch (SchedulerException e) {
      LOG.error("Can't schedule Job", e);
    }
  }


  public State getState() {
    return state;
  }
}
