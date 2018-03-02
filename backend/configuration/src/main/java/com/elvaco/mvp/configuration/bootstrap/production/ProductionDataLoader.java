package com.elvaco.mvp.configuration.bootstrap.production;

import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.spi.repository.MeterDefinitions;
import com.elvaco.mvp.core.spi.repository.Organisations;
import com.elvaco.mvp.core.spi.repository.Users;
import com.elvaco.mvp.core.usecase.SettingUseCases;
import com.elvaco.mvp.database.repository.jpa.RoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import static com.elvaco.mvp.configuration.bootstrap.production.ProductionData.meterDefinitions;
import static com.elvaco.mvp.configuration.bootstrap.production.ProductionData.organisations;
import static com.elvaco.mvp.configuration.bootstrap.production.ProductionData.superAdminUser;
import static com.elvaco.mvp.configuration.bootstrap.production.ProductionData.users;

@Order(1)
@Component
@Slf4j
class ProductionDataLoader implements CommandLineRunner {

  private final RoleRepository roleRepository;
  private final MeterDefinitions meterDefinitions;
  private final Organisations organisations;
  private final Users users;
  private final SettingUseCases settingUseCases;

  @Autowired
  ProductionDataLoader(
    RoleRepository roleRepository,
    MeterDefinitions meterDefinitions,
    Organisations organisations,
    Users users,
    SettingUseCases settingUseCases
  ) {
    this.roleRepository = roleRepository;
    this.meterDefinitions = meterDefinitions;
    this.organisations = organisations;
    this.users = users;
    this.settingUseCases = settingUseCases;
  }

  @Override
  public void run(String... args) {
    if (settingUseCases.isProductionDataLoaded()) {
      log.info("Production data seems to already be loaded - skipping production data loading!");
      return;
    }

    log.info("Seeding database with initial data ...");
    createRoles();
    createOrganisations();
    createSuperAdministratorIfNotPresent();
    createMeterDefinitions();
    log.info("Initial database seeding done.");

    settingUseCases.setProductionDataLoaded();
  }

  private void createMeterDefinitions() {
    meterDefinitions().forEach(meterDefinitions::save);
  }

  private void createOrganisations() {
    organisations().forEach(organisations::save);
  }

  private void createSuperAdministratorIfNotPresent() {
    User user = superAdminUser();
    if (!users.findByEmail(user.email).isPresent()) {
      users.create(user);
    }
  }

  private void createRoles() {
    roleRepository.save(users());
  }
}
