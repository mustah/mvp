package com.elvaco.mvp.testing.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.security.TokenService;

public class MockTokenService implements TokenService {

  private final Map<String, AuthenticatedUser> cache = new HashMap<>();

  @Override
  public Optional<AuthenticatedUser> getToken(String key) {
    return Optional.ofNullable(cache.get(key));
  }

  @Override
  public void saveToken(AuthenticatedUser authenticatedUser) {
    cache.put(authenticatedUser.getToken(), authenticatedUser);
  }

  @Override
  public void removeToken(String token) {
    cache.remove(token);
  }

  @Override
  public void removeTokenByUsername(String username) {
    cache.values().stream()
      .filter(u -> u.hasSameUsernameAs(() -> username))
      .findFirst()
      .ifPresent(authenticatedUser -> cache.remove(authenticatedUser.getToken()));
  }
}
