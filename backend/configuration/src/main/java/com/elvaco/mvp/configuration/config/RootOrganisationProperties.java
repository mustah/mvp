package com.elvaco.mvp.configuration.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mvp.root-organisation")
@Getter
@Setter
public class RootOrganisationProperties {
  private String name = "Elvaco";
  private String slug = "elvaco";
}
