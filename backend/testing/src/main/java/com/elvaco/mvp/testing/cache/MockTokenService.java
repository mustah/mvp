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
  public void saveToken(String token, AuthenticatedUser authenticatedUser) {
    cache.put(token, authenticatedUser);
  }

  @Override
  public void removeToken(String token) {
    cache.remove(token);
  }

  @Override
  public void removeTokenByEmail(String email) {
    cache.values()
      .stream()
      .filter(u -> u.getUsername().equals(email))
      .findFirst()
      .ifPresent(authenticatedUser -> cache.remove(authenticatedUser.getToken()));
  }
}
