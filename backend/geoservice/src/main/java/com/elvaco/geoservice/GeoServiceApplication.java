package com.elvaco.geoservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@EntityScan(
  basePackageClasses = {
    GeoServiceApplication.class,
    Jsr310JpaConverters.class
  }
)
@SpringBootApplication
@EnableScheduling
@EnableJpaAuditing
public class GeoServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(GeoServiceApplication.class, args);
  }
}
