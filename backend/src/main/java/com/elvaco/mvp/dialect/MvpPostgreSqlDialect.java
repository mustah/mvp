package com.elvaco.mvp.dialect;

import com.elvaco.mvp.dialect.types.Types;
import com.elvaco.mvp.dialect.types.postgresql.PostgreSqlMeasurementUnitType;
import com.elvaco.mvp.dialect.types.postgresql.PostgreSqlPropertyCollectionType;
import org.hibernate.boot.model.TypeContributions;
import org.hibernate.dialect.PostgreSQL94Dialect;
import org.hibernate.service.ServiceRegistry;

public class MvpPostgreSqlDialect extends PostgreSQL94Dialect {

  @Override
  public void contributeTypes(TypeContributions typeContributions, ServiceRegistry
      serviceRegistry) {
    typeContributions.contributeType(new PostgreSqlPropertyCollectionType(), Types
        .PropertyCollection.toString());
    typeContributions.contributeType(new PostgreSqlMeasurementUnitType(), Types.MeasurementUnit
        .toString());
  }
}
