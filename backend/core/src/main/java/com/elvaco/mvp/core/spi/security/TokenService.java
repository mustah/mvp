package com.elvaco.mvp.core.spi.security;

import java.util.Optional;

import com.elvaco.mvp.core.security.AuthenticatedUser;

public interface TokenService {

  Optional<AuthenticatedUser> getToken(String key);

  void saveToken(String token, AuthenticatedUser authenticatedUser);

  void removeToken(String token);

  void removeTokenByEmail(String email);
}
