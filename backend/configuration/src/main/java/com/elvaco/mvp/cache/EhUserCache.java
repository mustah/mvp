package com.elvaco.mvp.cache;

import org.ehcache.Cache;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.UserDetails;

public class EhUserCache implements UserCache {

  public static final String USER_CACHE_NAME = "userCache";

  private final Cache<String, UserDetails> cache;

  public EhUserCache(Cache<String, UserDetails> cache) {
    this.cache = cache;
  }

  @Override
  public UserDetails getUserFromCache(String username) {
    return cache.get(username);
  }

  @Override
  public void putUserInCache(UserDetails user) {
    cache.put(user.getUsername(), user);
  }

  @Override
  public void removeUserFromCache(String username) {
    cache.remove(username);
  }
}
