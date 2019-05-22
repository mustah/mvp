package com.elvaco.mvp.configuration.config;

import javax.sql.DataSource;

import com.elvaco.mvp.configuration.config.properties.JooqProperties;
import com.elvaco.mvp.core.access.QuantityProvider;
import com.elvaco.mvp.core.unitconverter.UnitConverter;
import com.elvaco.mvp.core.util.MeasurementThresholdParser;
import com.elvaco.mvp.database.repository.jooq.FilterAcceptor;
import com.elvaco.mvp.database.repository.jooq.FilterVisitors;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.ExecuteContext;
import org.jooq.SQLDialect;
import org.jooq.conf.RenderNameStyle;
import org.jooq.conf.SettingsTools;
import org.jooq.impl.DataSourceConnectionProvider;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultDSLContext;
import org.jooq.impl.DefaultExecuteListener;
import org.jooq.impl.DefaultExecuteListenerProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

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
  MeasurementThresholdParser measurementThresholdParser(
    QuantityProvider quantityProvider,
    UnitConverter unitConverter
  ) {
    return new MeasurementThresholdParser(quantityProvider, unitConverter);
  }

  @Bean
  @Scope(value = SCOPE_PROTOTYPE, proxyMode = ScopedProxyMode.TARGET_CLASS)
  FilterAcceptor logicalMeterFilters(
    DSLContext dsl,
    MeasurementThresholdParser measurementThresholdParser
  ) {
    return FilterVisitors.logicalMeter(dsl, measurementThresholdParser);
  }

  @Bean
  @Scope(value = SCOPE_PROTOTYPE, proxyMode = ScopedProxyMode.TARGET_CLASS)
  FilterAcceptor logicalMeterMeasurementFilters(
    DSLContext dsl,
    MeasurementThresholdParser measurementThresholdParser
  ) {
    return FilterVisitors.measurement(dsl, measurementThresholdParser);
  }

  @Bean
  @Scope(value = SCOPE_PROTOTYPE, proxyMode = ScopedProxyMode.TARGET_CLASS)
  FilterAcceptor displayQuantityFilters(
    DSLContext dsl,
    MeasurementThresholdParser measurementThresholdParser
  ) {
    return FilterVisitors.displayQuantity(dsl, measurementThresholdParser);
  }

  @Bean
  @Scope(value = SCOPE_PROTOTYPE, proxyMode = ScopedProxyMode.TARGET_CLASS)
  FilterAcceptor gatewayFilters(
    DSLContext dsl,
    MeasurementThresholdParser measurementThresholdParser
  ) {
    return FilterVisitors.gateway(dsl, measurementThresholdParser);
  }

  @Bean
  org.jooq.Configuration configuration() {
    return new DefaultConfiguration()
      .set(SettingsTools.defaultSettings()
        .withRenderSchema(false)
        .withRenderNameStyle(RenderNameStyle.AS_IS))
      .set(new DataSourceConnectionProvider(
        new TransactionAwareDataSourceProxy(dataSource))
      )
      .set(new DefaultExecuteListenerProvider(new ExceptionTranslator()))
      .set(SQLDialect.valueOf(jooqProperties.getSqlDialect()));
  }

  private static class ExceptionTranslator extends DefaultExecuteListener {

    private static final long serialVersionUID = -5095612289357251572L;

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
