package com.elvaco.mvp.dialect;

import org.hibernate.boot.model.TypeContributions;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.service.ServiceRegistry;

import com.elvaco.mvp.dialect.types.H2MvpPropertyCollectionType;

public class MvpH2Dialect extends H2Dialect {

  @Override
  public void contributeTypes(TypeContributions typeContributions, ServiceRegistry serviceRegistry) {
    typeContributions.contributeType(new H2MvpPropertyCollectionType(), "property-collection");
  }
}
