package com.elvaco.mvp.schedule;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.elvaco.mvp.core.spi.repository.Measurements;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MeasurementStatDataJobComponent {

  @Autowired
  @Qualifier("measurementStatTaskExecutor")
  private TaskExecutor taskExecutor;

  @Autowired
  private Measurements measurements;

  @Value("${mvp.measurement.stat.workers:4}")
  int numberOfWorkers;
  @Value("${mvp.measurement.stat.queue.age:30000}")
  int queAge;

  List<JobWorker> workers = new ArrayList<>();

  @PostConstruct
  public void init() {
    log.info("Starting {} workers with queue age {}",numberOfWorkers,queAge);
    for (int i = 0; i < numberOfWorkers; i++) {
      workers.add(new JobWorker(i));
    }
    workers.forEach(w -> {
      taskExecutor.execute(w);
    });

  }

  @PreDestroy
  public void destruct() {
    workers.forEach(w -> {
      w.setStopped(true);
    });
  }

  @Setter
  private class JobWorker implements Runnable {
    private static final long MIN_SLEEP = 10;
    private static final long MAX_SLEEP = 500;

    int id;
    boolean stopped = false;
    long currentSleep = MIN_SLEEP;

    public JobWorker(int id) {
      this.id = id;
    }

    @Override
    public void run() {
      int count = 0;
      int exceptionCount = 0;
      int sleepMillis = 10;
      while (!stopped) {
        try {
          count = measurements.popAndCalculate(1, queAge, numberOfWorkers, id);
          //Reset exception counter and sleep
          exceptionCount = 0;
          currentSleep = MIN_SLEEP;
        } catch (RuntimeException re) {
          //Make sure wee sleep some before trying again
          exceptionCount++;
          //Do not log more than 10 failures in a row
          if (exceptionCount < 10) {
            log.error("Worker failed to popAndCalculate", re);
          }
        }
        if (count == 0 || exceptionCount > 0) {
          try {
            Thread.sleep(currentSleep);
            if (currentSleep < MAX_SLEEP) {
              currentSleep += MIN_SLEEP;
            }
          } catch (InterruptedException e) {
            stopped = true;
          }
        }
      }
      log.info("Worker {} is stopped",id);
    }
  }
}
