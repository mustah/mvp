package com.elvaco.mvp.bootstrap;

import com.elvaco.mvp.config.H2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@H2
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
  }
}
