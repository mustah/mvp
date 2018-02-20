package com.elvaco.mvp.configuration.config;

import java.util.concurrent.TimeUnit;

import com.elvaco.mvp.core.security.AuthenticatedUser;
import lombok.extern.slf4j.Slf4j;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.core.events.CacheEventListenerConfiguration;
import org.ehcache.event.CacheEvent;
import org.ehcache.event.EventType;
import org.ehcache.expiry.Duration;
import org.ehcache.expiry.Expiry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetails;

import static com.elvaco.mvp.cache.EhTokenServiceCache.TOKEN_SERVICE_CACHE_NAME;
import static com.elvaco.mvp.cache.EhUserCache.USER_CACHE_NAME;
import static org.ehcache.config.builders.CacheConfigurationBuilder.newCacheConfigurationBuilder;
import static org.ehcache.config.builders.CacheEventListenerConfigurationBuilder.newEventListenerConfiguration;
import static org.ehcache.config.builders.CacheManagerBuilder.newCacheManagerBuilder;
import static org.ehcache.expiry.Expirations.timeToIdleExpiration;

@Slf4j
@Configuration
class CacheManagerConfig {

  private final int heapEntries;
  private final int idleTime;

  @Autowired
  CacheManagerConfig(
    @Value("${ehcache.heap-entries}") int heapEntries,
    @Value("${ehcache.idle-time}") int idleTime
  ) {
    this.heapEntries = heapEntries;
    this.idleTime = idleTime;
  }

  @Bean
  CacheManager cacheManager() {
    CacheEventListenerConfiguration eventListenerConfiguration = cacheEventListenerConfiguration();
    Expiry<Object, Object> expiry = timeToIdleExpiration(Duration.of(idleTime, TimeUnit.MINUTES));

    return newCacheManagerBuilder()
      .withCache(
        USER_CACHE_NAME,
        userCacheConfig(eventListenerConfiguration, expiry)
      )
      .withCache(
        TOKEN_SERVICE_CACHE_NAME,
        tokenServiceCacheConfig(eventListenerConfiguration, expiry)
      )
      .build(true);
  }

  private CacheConfigurationBuilder<String, UserDetails> userCacheConfig(
    CacheEventListenerConfiguration eventListenerConfiguration,
    Expiry<Object, Object> expiry
  ) {
    return newCacheConfigurationBuilder(
      String.class,
      UserDetails.class,
      ResourcePoolsBuilder.heap(heapEntries)
    )
      .add(eventListenerConfiguration)
      .withExpiry(expiry);
  }

  private CacheConfigurationBuilder<String, AuthenticatedUser> tokenServiceCacheConfig(
    CacheEventListenerConfiguration eventListenerConfiguration,
    Expiry<Object, Object> expiry
  ) {
    return newCacheConfigurationBuilder(
      String.class,
      AuthenticatedUser.class,
      ResourcePoolsBuilder.heap(heapEntries)
    )
      .add(eventListenerConfiguration)
      .withExpiry(expiry);
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
