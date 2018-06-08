package com.elvaco.mvp.configuration.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Profile("prod")
@Configuration
class ProdConfig {

  @Bean
  @Primary
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(13);
  }
}
