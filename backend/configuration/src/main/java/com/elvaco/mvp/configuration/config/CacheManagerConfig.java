package com.elvaco.mvp.configuration.config;

import java.time.Duration;

import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.producers.rabbitmq.dto.GetReferenceInfoDto;
import lombok.extern.slf4j.Slf4j;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.core.events.CacheEventListenerConfiguration;
import org.ehcache.event.CacheEvent;
import org.ehcache.event.EventType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.elvaco.mvp.cache.EhTokenServiceCache.TOKEN_SERVICE_CACHE_NAME;
import static com.elvaco.mvp.configuration.config.CacheConfig.METERING_MESSAGE_CACHE_NAME;
import static java.util.Arrays.asList;
import static org.ehcache.config.builders.CacheConfigurationBuilder.newCacheConfigurationBuilder;
import static org.ehcache.config.builders.CacheEventListenerConfigurationBuilder.newEventListenerConfiguration;
import static org.ehcache.config.builders.CacheManagerBuilder.newCacheManagerBuilder;
import static org.ehcache.config.builders.ExpiryPolicyBuilder.timeToLiveExpiration;

@Slf4j
@EnableCaching
@Configuration
class CacheManagerConfig {

  private final int heapEntries;
  private final int tokenIdleTime;
  private final int meteringMessageIdleTime;
  private final int meteringMessageHeapEntries;

  @Autowired
  CacheManagerConfig(
    @Value("${ehcache.token.heap-entries}") int tokenHeapEntries,
    @Value("${ehcache.token.idle-time}") int tokenIdleTime,
    @Value("${ehcache.metering.idle-time}") int meteringMessageIdleTime,
    @Value("${ehcache.metering.heap-entries}") int meteringMessageHeapEntries
  ) {
    this.heapEntries = tokenHeapEntries;
    this.tokenIdleTime = tokenIdleTime;
    this.meteringMessageIdleTime = meteringMessageIdleTime;
    this.meteringMessageHeapEntries = meteringMessageHeapEntries;
  }

  @Bean
  CacheManager ehCacheManager() {
    return newCacheManagerBuilder()
      .withCache(
        TOKEN_SERVICE_CACHE_NAME,
        newCacheConfigurationBuilder(
          String.class,
          AuthenticatedUser.class,
          ResourcePoolsBuilder.heap(heapEntries)
        ).add(cacheEventListenerConfiguration())
          .withExpiry(timeToLiveExpiration(Duration.ofMinutes(tokenIdleTime)))
      )
      .withCache(
        METERING_MESSAGE_CACHE_NAME,
        newCacheConfigurationBuilder(
          String.class,
          GetReferenceInfoDto.class,
          ResourcePoolsBuilder.heap(meteringMessageHeapEntries)
        ).add(cacheEventListenerConfiguration())
          .withExpiry(timeToLiveExpiration(Duration.ofMinutes(meteringMessageIdleTime)))
      )
      .build(true);
  }

  @Bean
  org.springframework.cache.CacheManager cacheManager() {
    SimpleCacheManager cacheManager = new SimpleCacheManager();
    cacheManager.setCaches(asList(
      new ConcurrentMapCache("organisation.slug"),
      new ConcurrentMapCache("organisation.externalId"),
      new ConcurrentMapCache("physicalMeter.organisationIdExternalIdAddress"),
      new ConcurrentMapCache("logicalMeter.organisationIdExternalId"),
      new ConcurrentMapCache("gateway.organisationIdSerial")
    ));
    return cacheManager;
  }

  private CacheEventListenerConfiguration cacheEventListenerConfiguration() {
    return newEventListenerConfiguration(
      this::onEvent,
      EventType.EXPIRED,
      EventType.EVICTED,
      EventType.REMOVED
    )
      .unordered()
      .asynchronous()
      .build();
  }

  private void onEvent(CacheEvent<?, ?> event) {
    log.info("Ehcache event listener callback: {}", event);
  }
}
