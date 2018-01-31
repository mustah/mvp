package com.elvaco.mvp.dialect;

import com.elvaco.mvp.dialect.types.Types;
import com.elvaco.mvp.dialect.types.h2.H2MeasurementUnitType;
import com.elvaco.mvp.dialect.types.h2.H2PropertyCollectionType;
import com.elvaco.mvp.entity.meteringpoint.PropertyCollection;
import org.hibernate.boot.model.TypeContributions;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.BooleanType;
import org.hibernate.type.CustomType;

public class MvpH2Dialect extends H2Dialect {

  public MvpH2Dialect() {
    super();
    registerFunction(
      "unit_at",
      new SQLFunctionTemplate(new CustomType(new H2MeasurementUnitType()), "unit_at(?1, ?2)")
    );
    registerFunction(
      "jsonb_contains",
      new SQLFunctionTemplate(BooleanType.INSTANCE, "jsonb_contains(?1, ?2)")
    );
  }

  @Override
  public void contributeTypes(
    TypeContributions typeContributions,
    ServiceRegistry serviceRegistry
  ) {
    typeContributions.contributeType(
      new H2PropertyCollectionType(),
      Types.PropertyCollection.toString(),
      PropertyCollection.class.getCanonicalName()
    );
    typeContributions.contributeType(
      new H2MeasurementUnitType(),
      Types.MeasurementUnit.toString()
    );
  }
}
