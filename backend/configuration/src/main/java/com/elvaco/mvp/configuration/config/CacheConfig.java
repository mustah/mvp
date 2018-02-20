package com.elvaco.mvp.configuration.config;

import javax.annotation.PreDestroy;

import com.elvaco.mvp.cache.EhTokenServiceCache;
import com.elvaco.mvp.cache.EhUserCache;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.security.TokenFactory;
import com.elvaco.mvp.core.spi.security.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.ehcache.CacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.UserDetails;

import static com.elvaco.mvp.cache.EhTokenServiceCache.TOKEN_SERVICE_CACHE_NAME;
import static com.elvaco.mvp.cache.EhUserCache.USER_CACHE_NAME;
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
  UserCache userCache() {
    return new EhUserCache(cacheManager.getCache(
      USER_CACHE_NAME,
      String.class,
      UserDetails.class
    ));
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
    cacheManager.removeCache(USER_CACHE_NAME);
    cacheManager.removeCache(TOKEN_SERVICE_CACHE_NAME);
  }
}
