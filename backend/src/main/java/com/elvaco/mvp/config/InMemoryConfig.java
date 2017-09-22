package com.elvaco.mvp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import com.elvaco.mvp.auth.Roles;

@InMemory
@Configuration
class InMemoryConfig {

  @Bean
  public UserDetailsService userDetailsService() {
    InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
    manager.createUser(User.withUsername("user").password("password").roles(Roles.USER).build());
    manager.createUser(User.withUsername("evanil@elvaco.se").password("eva123").roles(Roles.USER).build());
    manager.createUser(User.withUsername("hansjo@elvaco.se").password("hanna123").roles(Roles.USER, Roles.ADMIN).build());
    manager.createUser(User.withUsername("emitir@elvaco.se").password("emil123").roles(Roles.USER, Roles.ADMIN).build());
    manager.createUser(User.withUsername("a").password("a").roles(Roles.USER, Roles.ADMIN).build());
    return manager;
  }
}
