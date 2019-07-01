package com.elvaco.mvp.testing.amqp;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.elvaco.mvp.core.spi.amqp.JobService;

public class MockJobService<V> implements JobService<V> {

  private final Map<String, V> jobs = new HashMap<>();

  @Override
  public void newPendingJob(String jobId) {
    jobs.put(jobId, null);
  }

  @Override
  public void removeAllJobs() {
    jobs.clear();
  }

  @Override
  public V getJob(String jobId) {
    return jobs.get(jobId);
  }

  @Override
  public boolean isActive(String jobId) {
    return jobs.containsKey(jobId);
  }

  @Override
  public void update(String jobId, V jobInformation) {
    jobs.put(jobId, jobInformation);
  }

  public Collection<V> getAll() {
    return jobs.values();
  }
}
