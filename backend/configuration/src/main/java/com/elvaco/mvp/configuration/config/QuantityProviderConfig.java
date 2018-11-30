package com.elvaco.mvp.configuration.config;

import com.elvaco.mvp.core.access.QuantityAccess;
import com.elvaco.mvp.core.access.QuantityProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class QuantityProviderConfig {

  @Bean
  QuantityProvider quantityProvider() {
    //FIXME: This should not be a singleton
    return QuantityAccess.singleton();
  }
}
