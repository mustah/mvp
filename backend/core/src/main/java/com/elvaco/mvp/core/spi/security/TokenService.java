package com.elvaco.mvp.core.spi.security;

import java.util.Optional;

import com.elvaco.mvp.core.security.AuthenticatedUser;

public interface TokenService {

  String CACHE_NAME = "tokenServiceCache";

  Optional<AuthenticatedUser> getToken(String key);

  void saveToken(AuthenticatedUser authenticatedUser);

  void removeToken(String token);

  void removeTokenByUsername(String username);
}
