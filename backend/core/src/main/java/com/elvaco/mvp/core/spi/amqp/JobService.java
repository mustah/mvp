package com.elvaco.mvp.core.spi.amqp;

public interface JobService<V> {

  void newPendingJob(String jobId);

  void removeAllJobs();

  V getJob(String jobId);

  boolean isActive(String jobId);

  void update(String jobId, V jobInformation);
}
