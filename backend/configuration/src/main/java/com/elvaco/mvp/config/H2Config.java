package com.elvaco.mvp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;

@H2
@Configuration
class H2Config {

  @Bean
  UserDetailsManager userDetailsService() {
    return new InMemoryUserDetailsManager();
  }
}
