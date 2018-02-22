package com.elvaco.mvp.configuration.config;

import javax.annotation.PreDestroy;

import com.elvaco.mvp.cache.EhTokenServiceCache;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.security.TokenFactory;
import com.elvaco.mvp.core.spi.security.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.ehcache.CacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.elvaco.mvp.cache.EhTokenServiceCache.TOKEN_SERVICE_CACHE_NAME;
import static java.util.UUID.randomUUID;

@Slf4j
@Configuration
class CacheConfig {

  private final CacheManager cacheManager;

  @Autowired
  CacheConfig(CacheManager cacheManager) {
    this.cacheManager = cacheManager;
  }

  @Bean
  TokenService tokenService() {
    return new EhTokenServiceCache(cacheManager.getCache(
      TOKEN_SERVICE_CACHE_NAME,
      String.class,
      AuthenticatedUser.class
    ));
  }

  @Bean
  TokenFactory tokenFactory() {
    return () -> randomUUID().toString();
  }

  @PreDestroy
  void removeCache() {
    cacheManager.removeCache(TOKEN_SERVICE_CACHE_NAME);
  }
}