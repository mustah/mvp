package com.elvaco.mvp.configuration.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "jooq")
public class JooqProperties {

  private String sqlDialect;
}
