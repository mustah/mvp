package com.elvaco.mvp.configuration.config;

import java.util.concurrent.Executor;

import com.elvaco.mvp.core.spi.repository.Users;
import com.elvaco.mvp.core.spi.security.TokenFactory;
import com.elvaco.mvp.web.security.JpaUserDetailsService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.userdetails.UserDetailsService;

@EnableAsync
@Configuration
class ApplicationConfig {

  private final Users users;
  private final TokenFactory tokenFactory;

  @Autowired
  ApplicationConfig(@Lazy Users users, @Lazy TokenFactory tokenFactory) {
    this.users = users;
    this.tokenFactory = tokenFactory;
  }

  @Bean
  UserDetailsService userDetailsService() {
    return new JpaUserDetailsService(users, tokenFactory);
  }

  @Bean
  @Primary
  Executor threadPoolTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(4);
    executor.setMaxPoolSize(4);
    executor.setQueueCapacity(500);
    executor.setThreadNamePrefix("async-task-executor-");
    executor.initialize();
    return executor;
  }
}
