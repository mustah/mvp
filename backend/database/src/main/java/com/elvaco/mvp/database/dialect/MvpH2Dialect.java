package com.elvaco.mvp.database.dialect;

import com.elvaco.mvp.database.dialect.types.Types;
import com.elvaco.mvp.database.dialect.types.h2.H2MeasurementUnitType;
import org.hibernate.boot.model.TypeContributions;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.CustomType;

public class MvpH2Dialect extends H2Dialect {

  public MvpH2Dialect() {
    super();
    registerFunction(
      "unit_at",
      new SQLFunctionTemplate(new CustomType(new H2MeasurementUnitType()), "unit_at(?1, ?2)")
    );
  }

  @Override
  public void contributeTypes(
    TypeContributions typeContributions,
    ServiceRegistry serviceRegistry
  ) {
    typeContributions.contributeType(
      new H2MeasurementUnitType(),
      Types.MeasurementUnit.toString()
    );
  }
}
