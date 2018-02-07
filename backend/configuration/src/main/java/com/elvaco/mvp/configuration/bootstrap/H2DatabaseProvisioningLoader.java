package com.elvaco.mvp.configuration.bootstrap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Profile("h2")
@Component
public class H2DatabaseProvisioningLoader implements CommandLineRunner {

  private final JdbcTemplate template;

  @Autowired
  public H2DatabaseProvisioningLoader(JdbcTemplate template) {
    this.template = template;
  }

  @Override
  public void run(String... args) {
    template.execute("CREATE ALIAS unit_at FOR \"com.elvaco.mvp.dialect.function"
      + ".h2.CompatibilityFunctions.unitAt\"");
    template.execute("CREATE ALIAS jsonb_contains FOR \"com.elvaco.mvp.dialect.function"
      + ".h2.CompatibilityFunctions.jsonbContains\"");
    template.execute("CREATE ALIAS jsonb_exists FOR \"com.elvaco.mvp.dialect.function"
      + ".h2.CompatibilityFunctions.jsonbExists\"");
  }
}
