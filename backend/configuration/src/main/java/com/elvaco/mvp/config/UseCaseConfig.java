package com.elvaco.mvp.config;

import com.elvaco.mvp.core.usecase.AdminUserUseCases;
import com.elvaco.mvp.core.usecase.UserUseCases;
import com.elvaco.mvp.core.usecase.Users;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class UseCaseConfig {

  private final Users users;

  @Autowired
  UseCaseConfig(Users users) {
    this.users = users;
  }

  @Bean
  UserUseCases userUseCases() {
    return new UserUseCases(users);
  }

  @Bean
  AdminUserUseCases adminUserUseCases() {
    return new AdminUserUseCases(users);
  }
}
