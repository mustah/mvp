package com.elvaco.mvp.config;

import com.elvaco.mvp.core.usecase.Settings;
import com.elvaco.mvp.core.usecase.Users;
import com.elvaco.mvp.repository.access.SettingMapper;
import com.elvaco.mvp.repository.access.SettingRepository;
import com.elvaco.mvp.repository.access.UserMapper;
import com.elvaco.mvp.repository.access.UserRepository;
import com.elvaco.mvp.repository.jpa.SettingJpaRepository;
import com.elvaco.mvp.repository.jpa.UserJpaRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
class DataProviderConfig {

  private final UserJpaRepository userJpaRepository;
  private final SettingJpaRepository settingJpaRepository;
  private final ModelMapper modelMapper;
  private final PasswordEncoder passwordEncoder;

  @Autowired
  DataProviderConfig(
    UserJpaRepository userJpaRepository,
    SettingJpaRepository settingJpaRepository,
    ModelMapper modelMapper,
    PasswordEncoder passwordEncoder
  ) {
    this.userJpaRepository = userJpaRepository;
    this.settingJpaRepository = settingJpaRepository;
    this.modelMapper = modelMapper;
    this.passwordEncoder = passwordEncoder;
  }

  @Bean
  Users users() {
    return new UserRepository(
      userJpaRepository,
      new UserMapper(modelMapper),
      passwordEncoder::encode
    );
  }

  @Bean
  Settings settings() {
    return new SettingRepository(
      settingJpaRepository,
      new SettingMapper()
    );
  }
}
