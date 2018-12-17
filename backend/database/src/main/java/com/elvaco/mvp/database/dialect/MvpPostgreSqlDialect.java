package com.elvaco.mvp.database.dialect;

import com.elvaco.mvp.database.dialect.types.Types;
import com.elvaco.mvp.database.dialect.types.postgresql.PostgreSqlJsonFieldType;
import com.elvaco.mvp.database.dialect.types.postgresql.PostgreSqlTsTzRangeType;
import com.elvaco.mvp.database.entity.meter.JsonField;

import org.hibernate.boot.model.TypeContributions;
import org.hibernate.dialect.PostgreSQL94Dialect;
import org.hibernate.service.ServiceRegistry;

public class MvpPostgreSqlDialect extends PostgreSQL94Dialect {

  @Override
  public void contributeTypes(
    TypeContributions typeContributions,
    ServiceRegistry serviceRegistry
  ) {
    super.contributeTypes(typeContributions, serviceRegistry);
    typeContributions.contributeType(
      new PostgreSqlJsonFieldType(),
      Types.JsonField.toString(),
      JsonField.class.getCanonicalName()
    );
    typeContributions.contributeType(
      new PostgreSqlTsTzRangeType(),
      "tstzrange"
    );
  }
}
