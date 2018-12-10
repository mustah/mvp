package com.elvaco.mvp.amqp;

import com.elvaco.mvp.core.spi.amqp.JobService;
import com.elvaco.mvp.core.spi.cache.Cache;
import com.elvaco.mvp.producers.rabbitmq.dto.Constants;
import com.elvaco.mvp.producers.rabbitmq.dto.MeteringReferenceInfoMessageDto;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MeterSyncJobService implements JobService<MeteringReferenceInfoMessageDto> {

  private final Cache<String, MeteringReferenceInfoMessageDto> jobIdCache;

  @Override
  public void newPendingJob(String jobId) {
    jobIdCache.put(jobId, Constants.NULL_METERING_REFERENCE_INFO_MESSAGE_DTO);
  }

  @Override
  public void removeAllJobs() {
    jobIdCache.clear();
  }

  @Override
  public MeteringReferenceInfoMessageDto getJob(String jobId) {
    return jobIdCache.get(jobId);
  }

  @Override
  public boolean isActive(String jobId) {
    return jobId != null && !jobId.isEmpty() && jobIdCache.containsKey(jobId);
  }

  @Override
  public void update(
    String jobId, MeteringReferenceInfoMessageDto jobInformation
  ) {
    jobIdCache.put(jobId, jobInformation);
  }

}
