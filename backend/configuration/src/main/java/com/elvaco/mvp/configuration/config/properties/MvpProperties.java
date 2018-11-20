package com.elvaco.mvp.configuration.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "mvp")
public class MvpProperties {

  private RootOrganisation rootOrganisation;
  private Superadmin superadmin;
  private String url = "http://localhost:8080";

  @Getter
  @Setter
  public static class RootOrganisation {
    private String name = "Elvaco";
    private String slug = "elvaco";
  }

  @Getter
  @Setter
  public static class Superadmin {
    private String email = "mvpadmin@elvaco.se";
    private String password = "changeme";
  }
}
