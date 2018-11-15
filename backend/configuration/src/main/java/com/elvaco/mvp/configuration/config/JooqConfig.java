package com.elvaco.mvp.configuration.config;

import javax.sql.DataSource;

import com.elvaco.mvp.configuration.config.properties.JooqProperties;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.ExecuteContext;
import org.jooq.SQLDialect;
import org.jooq.conf.SettingsTools;
import org.jooq.impl.DataSourceConnectionProvider;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultDSLContext;
import org.jooq.impl.DefaultExecuteListener;
import org.jooq.impl.DefaultExecuteListenerProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;

@RequiredArgsConstructor
@Configuration
@EnableConfigurationProperties(JooqProperties.class)
class JooqConfig {

  private final JooqProperties jooqProperties;
  private final DataSource dataSource;

  @Bean
  DSLContext dsl() {
    return new DefaultDSLContext(configuration());
  }

  @Bean
  org.jooq.Configuration configuration() {
    return new DefaultConfiguration()
      .set(SettingsTools.defaultSettings().withRenderSchema(false))
      .set(new DataSourceConnectionProvider(dataSource))
      .set(new DefaultExecuteListenerProvider(new ExceptionTranslator()))
      .set(SQLDialect.valueOf(jooqProperties.getSqlDialect()));
  }

  private static class ExceptionTranslator extends DefaultExecuteListener {

    @Override
    public void exception(ExecuteContext context) {
      var dialectName = context.configuration().dialect().name();
      var translator = new SQLErrorCodeSQLExceptionTranslator(dialectName);

      context.exception(translator.translate(
        "Access database using jOOQ",
        context.sql(),
        context.sqlException()
      ));
    }
  }
}
