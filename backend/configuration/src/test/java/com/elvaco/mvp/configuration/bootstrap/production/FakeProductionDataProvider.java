package com.elvaco.mvp.configuration.bootstrap.production;

import java.util.List;

import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.Role;
import com.elvaco.mvp.core.domainmodels.User;

import static java.util.Collections.emptyList;

class FakeProductionDataProvider implements ProductionDataProvider {

  private final User superAdmin;
  private final List<Organisation> organisations;

  FakeProductionDataProvider(User superAdmin, List<Organisation> organisations) {
    this.superAdmin = superAdmin;
    this.organisations = organisations;
  }

  @Override
  public List<Role> users() {
    return emptyList();
  }

  @Override
  public List<MeterDefinition> meterDefinitions() {
    return emptyList();
  }

  @Override
  public List<Organisation> organisations() {
    return organisations;
  }

  @Override
  public User superAdminUser() {
    return superAdmin;
  }
}
