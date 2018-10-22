package com.elvaco.mvp.configuration.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mvp")
@Getter
@Setter
public class MvpProperties {

  private RootOrganisation rootOrganisation;
  private Superadmin superadmin;
  private String url = "http://localhost:8080";

  @Getter
  @Setter
  static class RootOrganisation {
    private String name = "Elvaco";
    private String slug = "elvaco";
  }

  @Getter
  @Setter
  static class Superadmin {
    private String email = "mvpadmin@elvaco.se";
    private String password = "changeme";
  }
}
