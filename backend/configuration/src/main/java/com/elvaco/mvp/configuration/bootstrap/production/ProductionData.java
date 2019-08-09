package com.elvaco.mvp.configuration.bootstrap.production;

import java.util.List;

import com.elvaco.mvp.core.domainmodels.Language;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.Role;
import com.elvaco.mvp.core.domainmodels.User;

import lombok.RequiredArgsConstructor;

import static com.elvaco.mvp.core.domainmodels.Role.ALL_MVP_ROLES;

@RequiredArgsConstructor
public class ProductionData implements ProductionDataProvider {

  private final String superAdminEmail;
  private final String superAdminPassword;
  private final Organisation rootOrganisation;

  @Override
  public List<Role> roles() {
    return ALL_MVP_ROLES;
  }

  @Override
  public List<MeterDefinition> meterDefinitions() {
    return List.of(
      MeterDefinition.UNKNOWN,
      MeterDefinition.DEFAULT_HOT_WATER,
      MeterDefinition.DEFAULT_DISTRICT_HEATING,
      MeterDefinition.DEFAULT_DISTRICT_COOLING,
      MeterDefinition.DEFAULT_GAS,
      MeterDefinition.DEFAULT_WATER,
      MeterDefinition.DEFAULT_ROOM_SENSOR,
      MeterDefinition.DEFAULT_ELECTRICITY
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
      List.of(Role.SUPER_ADMIN)
    );
  }
}
