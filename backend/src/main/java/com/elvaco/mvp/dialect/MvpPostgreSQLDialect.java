package com.elvaco.mvp.dialect;

import org.hibernate.boot.model.TypeContributions;
import org.hibernate.dialect.PostgreSQL94Dialect;
import org.hibernate.service.ServiceRegistry;

import com.elvaco.mvp.dialect.types.PostgreSQLMvpPropertyCollectionType;

public class MvpPostgreSQLDialect extends PostgreSQL94Dialect {

  @Override
  public void contributeTypes(TypeContributions typeContributions, ServiceRegistry serviceRegistry) {
    typeContributions.contributeType(new PostgreSQLMvpPropertyCollectionType(), "property-collection");
  }
}
