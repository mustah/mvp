package com.elvaco.mvp.cache;

import java.util.Iterator;
import java.util.Optional;

import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.security.TokenService;

import lombok.RequiredArgsConstructor;
import org.ehcache.Cache;
import org.ehcache.Cache.Entry;

@RequiredArgsConstructor
public class EhTokenServiceCache implements TokenService {

  public static final String TOKEN_SERVICE_CACHE_NAME = "tokenServiceCache";

  private final Cache<String, AuthenticatedUser> cache;

  @Override
  public Optional<AuthenticatedUser> getToken(String key) {
    return Optional.ofNullable(cache.get(key));
  }

  @Override
  public void saveToken(String token, AuthenticatedUser authenticatedUser) {
    cache.put(token, authenticatedUser);
  }

  @Override
  public void removeToken(String token) {
    cache.remove(token);
  }

  @Override
  public void removeTokenByUsername(String username) {
    Iterator<Entry<String, AuthenticatedUser>> it = cache.iterator();
    while (it.hasNext()) {
      Entry<String, AuthenticatedUser> entry = it.next();
      if (entry.getValue().hasSameUsernameAs(() -> username)) {
        it.remove();
        break;
      }
    }
  }
}
