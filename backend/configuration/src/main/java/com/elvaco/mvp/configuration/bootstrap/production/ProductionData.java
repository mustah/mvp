package com.elvaco.mvp.configuration.bootstrap.production;

import java.util.List;

import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.Role;
import com.elvaco.mvp.core.domainmodels.User;
import lombok.RequiredArgsConstructor;

import static com.elvaco.mvp.core.fixture.DomainModels.ELVACO;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;

@RequiredArgsConstructor
public class ProductionData implements ProductionDataProvider {

  private final String superAdminEmail;
  private final String superAdminPassword;

  @Override
  public List<Role> users() {
    return unmodifiableList(asList(
      Role.USER,
      Role.ADMIN,
      Role.SUPER_ADMIN
    ));
  }

  @Override
  public List<MeterDefinition> meterDefinitions() {
    return unmodifiableList(asList(
      MeterDefinition.UNKNOWN_METER,
      MeterDefinition.HOT_WATER_METER,
      MeterDefinition.DISTRICT_HEATING_METER,
      MeterDefinition.DISTRICT_COOLING_METER
    ));
  }

  @Override
  public List<Organisation> organisations() {
    return singletonList(ELVACO);
  }

  @Override
  public User superAdminUser() {
    return new User(
      "System Administrator",
      superAdminEmail,
      superAdminPassword,
      ELVACO,
      singletonList(Role.SUPER_ADMIN)
    );
  }
}
