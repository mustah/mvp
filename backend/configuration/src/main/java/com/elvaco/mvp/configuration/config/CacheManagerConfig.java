package com.elvaco.mvp.configuration.config;

import java.util.concurrent.TimeUnit;

import org.ehcache.CacheManager;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.expiry.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetails;

import static com.elvaco.mvp.cache.EhUserCache.USER_CACHE_NAME;
import static org.ehcache.config.builders.CacheConfigurationBuilder.newCacheConfigurationBuilder;
import static org.ehcache.config.builders.CacheManagerBuilder.newCacheManagerBuilder;
import static org.ehcache.expiry.Expirations.timeToLiveExpiration;

@Configuration
class CacheManagerConfig {

  private final int heapEntries;
  private final int timeToLive;

  @Autowired
  CacheManagerConfig(
    @Value("${ehcache.heap-entries}") int heapEntries,
    @Value("${ehcache.time-to-live}") int timeToLive
  ) {
    this.heapEntries = heapEntries;
    this.timeToLive = timeToLive;
  }

  @Bean
  CacheManager cacheManager() {
    return newCacheManagerBuilder()
      .withCache(
        USER_CACHE_NAME,
        newCacheConfigurationBuilder(
          String.class,
          UserDetails.class,
          ResourcePoolsBuilder.heap(heapEntries)
        )
          .withExpiry(timeToLiveExpiration(Duration.of(timeToLive, TimeUnit.MINUTES)))
      )
      .build(true);
  }
}
