package com.elvaco.mvp.web.security;

import com.elvaco.mvp.core.spi.repository.Users;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class JpaUserDetailsService implements UserDetailsService {

  private final Users users;

  public JpaUserDetailsService(Users users) {
    this.users = users;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return users.findByEmail(username)
      .map(MvpUserDetails::new)
      .orElseThrow(() -> new UsernameNotFoundException("Bad credentials"));
  }
}
