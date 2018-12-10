package com.elvaco.mvp.web.security;

import com.elvaco.mvp.core.spi.repository.Users;
import com.elvaco.mvp.core.spi.security.TokenFactory;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class JpaUserDetailsService implements UserDetailsService {

  private final Users users;
  private final TokenFactory tokenFactory;

  public JpaUserDetailsService(Users users, TokenFactory tokenFactory) {
    this.users = users;
    this.tokenFactory = tokenFactory;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return users.findByEmail(username)
      .map(user -> new MvpUserDetails(user, tokenFactory.newToken()))
      .orElseThrow(() -> usernameNotFoundException(username));
  }

  private UsernameNotFoundException usernameNotFoundException(String username) {
    return new UsernameNotFoundException(String.format("Unable to find: '%s'", username));
  }
}
