package com.elvaco.mvp.configuration.bootstrap.production;

import java.util.List;

import com.elvaco.mvp.core.domainmodels.Language;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.Role;
import com.elvaco.mvp.core.domainmodels.User;
import lombok.RequiredArgsConstructor;

import static java.util.Collections.singletonList;

@RequiredArgsConstructor
public class ProductionData implements ProductionDataProvider {
  private final String superAdminEmail;
  private final String superAdminPassword;
  private final Organisation rootOrganisation;

  @Override
  public List<Role> roles() {
    return List.of(
      Role.USER,
      Role.ADMIN,
      Role.SUPER_ADMIN
    );
  }

  @Override
  public List<MeterDefinition> meterDefinitions() {
    return List.of(
      MeterDefinition.UNKNOWN_METER,
      MeterDefinition.HOT_WATER_METER,
      MeterDefinition.DISTRICT_HEATING_METER,
      MeterDefinition.DISTRICT_COOLING_METER,
      MeterDefinition.GAS_METER,
      MeterDefinition.WATER_METER,
      MeterDefinition.ROOM_SENSOR_METER,
      MeterDefinition.ELECTRICITY_METER
    );
  }

  @Override
  public List<Organisation> organisations() {
    return List.of(rootOrganisation);
  }

  @Override
  public User superAdminUser() {
    return new User(
      "System Administrator",
      superAdminEmail,
      superAdminPassword,
      Language.sv,
      rootOrganisation,
      singletonList(Role.SUPER_ADMIN)
    );
  }
}
