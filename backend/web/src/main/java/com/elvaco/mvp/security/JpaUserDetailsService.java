package com.elvaco.mvp.security;

import com.elvaco.mvp.core.usecase.UserUseCases;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class JpaUserDetailsService implements UserDetailsService {

  private final UserUseCases userUseCases;

  public JpaUserDetailsService(UserUseCases userUseCases) {
    this.userUseCases = userUseCases;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return userUseCases.findByEmail(username)
      .map(MvpUserDetails::new)
      .orElseThrow(() -> new UsernameNotFoundException("Bad credentials"));
  }
}
