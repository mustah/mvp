package com.elvaco.mvp.configuration.bootstrap.production;

import java.util.List;

import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.Role;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.database.entity.user.RoleEntity;

import static com.elvaco.mvp.core.fixture.DomainModels.ELVACO;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;

final class ProductionData {

  private ProductionData() {}

  static List<RoleEntity> users() {
    return unmodifiableList(asList(
      RoleEntity.user(),
      RoleEntity.admin(),
      RoleEntity.superAdmin()
    ));
  }

  static List<MeterDefinition> meterDefinitions() {
    return unmodifiableList(asList(
      MeterDefinition.UNKNOWN_METER,
      MeterDefinition.DISTRICT_HEATING_METER,
      MeterDefinition.HOT_WATER_METER,
      MeterDefinition.DISTRICT_COOLING_METER
    ));
  }

  static List<Organisation> organisations() {
    return singletonList(ELVACO);
  }

  static User superAdminUser(String email, String password) {
    return new User(
      "System Administrator",
      email,
      password,
      ELVACO,
      singletonList(Role.SUPER_ADMIN)
    );
  }
}
