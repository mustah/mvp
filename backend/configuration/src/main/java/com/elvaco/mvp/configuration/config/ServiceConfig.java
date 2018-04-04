package com.elvaco.mvp.configuration.config;

import java.net.URI;
import java.util.function.Function;

import com.elvaco.mvp.core.spi.geocode.GeocodeService;
import com.elvaco.mvp.web.service.GeocodeSpringService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
class ServiceConfig {

  private static final Function<URI, String> HTTP_GET_CLIENT = uri ->
    new RestTemplate().getForObject(uri, String.class);

  @Bean
  GeocodeService geocodeService(
    @Value("${geo-service.url}") String geoServiceUrl,
    @Value("${mvp.url}") String mvpUrl
  ) {
    return new GeocodeSpringService(mvpUrl, geoServiceUrl, HTTP_GET_CLIENT);
  }
}
