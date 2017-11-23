package com.elvaco.mvp.dialect;

import com.elvaco.mvp.dialect.types.Types;
import com.elvaco.mvp.dialect.types.postgresql.PostgreSqlMeasurementUnitType;
import com.elvaco.mvp.dialect.types.postgresql.PostgreSqlPropertyCollectionType;
import org.hibernate.boot.model.TypeContributions;
import org.hibernate.dialect.PostgreSQL94Dialect;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.CustomType;

public class MvpPostgreSqlDialect extends PostgreSQL94Dialect {

  public MvpPostgreSqlDialect() {
    registerFunction("unit_at",
        new SQLFunctionTemplate(
            new CustomType(new PostgreSqlMeasurementUnitType()), "unit_at(?1, ?2)"));
  }

  @Override
  public void contributeTypes(TypeContributions typeContributions,
                              ServiceRegistry serviceRegistry) {
    super.contributeTypes(typeContributions, serviceRegistry);
    typeContributions.contributeType(new PostgreSqlPropertyCollectionType(), Types
        .PropertyCollection.toString());
    typeContributions.contributeType(new PostgreSqlMeasurementUnitType(), Types.MeasurementUnit
        .toString());
  }
}
