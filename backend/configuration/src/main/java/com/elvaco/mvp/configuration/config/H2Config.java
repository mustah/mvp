package com.elvaco.mvp.configuration.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.StandardPasswordEncoder;

@Profile("h2")
@Configuration
class H2Config {

  @Bean
  PasswordEncoder passwordEncoder() {
    return new StandardPasswordEncoder();
  }
}
