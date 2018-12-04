package com.elvaco.mvp.configuration.bootstrap.production;

import java.util.List;

import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.Role;
import com.elvaco.mvp.core.domainmodels.User;

public interface ProductionDataProvider {

  List<Role> roles();

  List<MeterDefinition> meterDefinitions();

  List<Organisation> organisations();

  User superAdminUser();
}
