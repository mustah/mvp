package com.elvaco.mvp.configuration.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.StandardPasswordEncoder;

@Configuration
class InMemoryConfig {

  @Bean
  PasswordEncoder passwordEncoder() {
    return new StandardPasswordEncoder();
  }
}
