package com.benjd90.photos2.scheduler;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Created by Benjamin on 23/02/2016.
 */
@Component
public class ScanScheduler {
  private static final Logger LOG = LoggerFactory.getLogger(ScanScheduler.class);

  public ScanScheduler() {
    JobDetail job = JobBuilder.newJob(ScanJob.class)
            .withIdentity("dummyJobName", "group1").build();


    // Trigger the job to run on the next round minute
    Trigger trigger = TriggerBuilder
            .newTrigger()
            .withIdentity("dummyTriggerName", "group1")
            .withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInMinutes(100).repeatForever())
            .build();

    // schedule it
    Scheduler scheduler = null;
    try {
      scheduler = new StdSchedulerFactory().getScheduler();
      scheduler.start();
      scheduler.scheduleJob(job, trigger);
    } catch (SchedulerException e) {
      LOG.error("Can't schedule Job", e);
    }
  }


}
