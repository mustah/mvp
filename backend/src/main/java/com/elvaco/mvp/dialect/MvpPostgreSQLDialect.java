package com.elvaco.mvp.dialect;

import com.elvaco.mvp.dialect.types.postgresql.PostgreSQLPropertyCollectionType;
import org.hibernate.boot.model.TypeContributions;
import org.hibernate.dialect.PostgreSQL94Dialect;
import org.hibernate.service.ServiceRegistry;

public class MvpPostgreSQLDialect extends PostgreSQL94Dialect {

  @Override
  public void contributeTypes(TypeContributions typeContributions, ServiceRegistry serviceRegistry)  {
    typeContributions.contributeType(new PostgreSQLPropertyCollectionType(), "property-collection");
  }
}
