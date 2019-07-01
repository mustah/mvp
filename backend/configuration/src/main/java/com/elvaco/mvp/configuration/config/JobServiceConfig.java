package com.elvaco.mvp.configuration.config;

import com.elvaco.mvp.amqp.MeterSyncJobService;
import com.elvaco.mvp.core.spi.amqp.JobService;
import com.elvaco.mvp.core.spi.cache.Cache;
import com.elvaco.mvp.producers.rabbitmq.dto.MeteringReferenceInfoMessageDto;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class JobServiceConfig {

  @Bean
  JobService<MeteringReferenceInfoMessageDto> meterSyncJobService(
    Cache<String, MeteringReferenceInfoMessageDto> jobIdCache
  ) {
    return new MeterSyncJobService(jobIdCache);
  }
}
