package com.elvaco.mvp.cache;

import java.util.Iterator;
import java.util.Optional;

import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.security.TokenService;
import lombok.extern.slf4j.Slf4j;
import org.ehcache.Cache;
import org.ehcache.Cache.Entry;

@Slf4j
public class EhTokenServiceCache implements TokenService {

  public static final String TOKEN_SERVICE_CACHE_NAME = "tokenServiceCache";

  private final Cache<String, AuthenticatedUser> cache;

  public EhTokenServiceCache(Cache<String, AuthenticatedUser> cache) {
    this.cache = cache;
  }

  @Override
  public Optional<AuthenticatedUser> getToken(String key) {
    log.info("getToken: {}", key);
    return Optional.ofNullable(cache.get(key));
  }

  @Override
  public void saveToken(String token, AuthenticatedUser authenticatedUser) {
    log.info("saveToken: {}", token);
    cache.put(token, authenticatedUser);
  }

  @Override
  public void removeToken(String token) {
    log.info("removeToken: {}", token);
    cache.remove(token);
  }

  @Override
  public void removeTokenByEmail(String email) {
    log.info("removeTokenByEmail: {}", email);
    Iterator<Entry<String, AuthenticatedUser>> it = cache.iterator();
    while (it.hasNext()) {
      Entry<String, AuthenticatedUser> entry = it.next();
      if (entry.getValue().getUsername().equals(email)) {
        it.remove();
        break;
      }
    }
  }
}
