package com.elvaco.mvp.configuration.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class JacksonConfiguration {

  @Bean
  Jackson2ObjectMapperBuilderCustomizer includeNonNullSerializer() {
    return jacksonObjectMapperBuilder ->
      jacksonObjectMapperBuilder.serializationInclusion(JsonInclude.Include.NON_NULL);
  }
}
