package com.elvaco.mvp.configuration.config;

import javax.annotation.PreDestroy;

import com.elvaco.mvp.cache.EhUserCache;
import lombok.extern.slf4j.Slf4j;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.UserDetails;

import static com.elvaco.mvp.cache.EhUserCache.USER_CACHE_NAME;

@Slf4j
@Configuration
class CacheConfig {

  private final CacheManager cacheManager;

  @Autowired
  CacheConfig(CacheManager cacheManager) {
    this.cacheManager = cacheManager;
  }

  @Bean
  UserCache userCache() {
    Cache<String, UserDetails> cacheDelegate = cacheManager.getCache(
      USER_CACHE_NAME,
      String.class,
      UserDetails.class
    );
    return new EhUserCache(cacheDelegate);
  }

  @PreDestroy
  void removeCache() {
    cacheManager.removeCache(USER_CACHE_NAME);
  }
}
