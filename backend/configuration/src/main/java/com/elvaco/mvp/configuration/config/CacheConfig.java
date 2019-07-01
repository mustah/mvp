package com.elvaco.mvp.configuration.config;

import javax.annotation.PreDestroy;

import com.elvaco.mvp.adapters.ehcache.CacheAdapter;
import com.elvaco.mvp.cache.EhTokenServiceCache;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.cache.Cache;
import com.elvaco.mvp.core.spi.security.TokenFactory;
import com.elvaco.mvp.core.spi.security.TokenService;
import com.elvaco.mvp.core.util.MessageThrottler;
import com.elvaco.mvp.producers.rabbitmq.dto.GetReferenceInfoDto;
import com.elvaco.mvp.producers.rabbitmq.dto.MeteringReferenceInfoMessageDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ehcache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.elvaco.mvp.cache.EhTokenServiceCache.TOKEN_SERVICE_CACHE_NAME;
import static java.util.UUID.randomUUID;

@Slf4j
@RequiredArgsConstructor
@Configuration
class CacheConfig {

  static final String METERING_MESSAGE_CACHE_NAME = "meteringMessageThrottleCache";
  static final String JOB_ID_CACHE_NAME = "meteringRequestJobIdCache";

  private final CacheManager ehCacheManager;

  @Bean
  TokenService tokenService() {
    return new EhTokenServiceCache(ehCacheManager.getCache(
      TOKEN_SERVICE_CACHE_NAME,
      String.class,
      AuthenticatedUser.class
    ));
  }

  @Bean
  MessageThrottler<String, GetReferenceInfoDto> meteringMessageThrottler() {
    Cache<String, GetReferenceInfoDto> cacheAdapter = new CacheAdapter<>(
      ehCacheManager.getCache(
        METERING_MESSAGE_CACHE_NAME,
        String.class,
        GetReferenceInfoDto.class
      ));
    return new MessageThrottler<>(cacheAdapter, String::valueOf);
  }

  @Bean
  Cache<String, MeteringReferenceInfoMessageDto> jobIdCache() {
    return new CacheAdapter<>(ehCacheManager.getCache(
      JOB_ID_CACHE_NAME,
      String.class,
      MeteringReferenceInfoMessageDto.class
    ));
  }

  @Bean
  TokenFactory tokenFactory() {
    return () -> randomUUID().toString();
  }

  @PreDestroy
  void removeCache() {
    ehCacheManager.removeCache(TOKEN_SERVICE_CACHE_NAME);
  }
}
