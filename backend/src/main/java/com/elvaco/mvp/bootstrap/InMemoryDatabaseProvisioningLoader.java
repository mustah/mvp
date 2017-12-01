package com.elvaco.mvp.bootstrap;

import com.elvaco.mvp.config.InMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@InMemory
public class InMemoryDatabaseProvisioningLoader implements CommandLineRunner {
  private JdbcTemplate template;

  @Autowired
  public InMemoryDatabaseProvisioningLoader(JdbcTemplate template) {
    this.template = template;
  }

  @Override
  public void run(String... args) throws Exception {
    template.execute("CREATE ALIAS unit_at FOR \"com.elvaco.mvp.dialect.function"
        + ".h2.CompatibilityFunctions.unitAt\"");
  }
}
