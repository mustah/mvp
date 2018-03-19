package com.elvaco.mvp.configuration.bootstrap.production;

import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.spi.repository.MeterDefinitions;
import com.elvaco.mvp.core.spi.repository.Organisations;
import com.elvaco.mvp.core.spi.repository.Roles;
import com.elvaco.mvp.core.spi.repository.Users;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(1)
@Component
@Slf4j
class ProductionDataLoader implements CommandLineRunner {

  private final Roles roles;
  private final MeterDefinitions meterDefinitions;
  private final Organisations organisations;
  private final Users users;
  private final ProductionDataProvider productionDataProvider;

  @Autowired
  ProductionDataLoader(
    Roles roles,
    MeterDefinitions meterDefinitions,
    Organisations organisations,
    Users users,
    ProductionDataProvider productionDataProvider
  ) {
    this.roles = roles;
    this.meterDefinitions = meterDefinitions;
    this.organisations = organisations;
    this.users = users;
    this.productionDataProvider = productionDataProvider;
  }

  @Override
  public void run(String... args) {
    log.info("Seeding database with initial data ...");
    seedDatabase();
    log.info("Initial database seeding done.");
  }

  void seedDatabase() {
    createRoles();
    createOrganisations();
    createSuperAdministratorIfNotPresent();
    createMeterDefinitions();
  }

  public ProductionDataProvider getProductionDataProvider() {
    return productionDataProvider;
  }

  private void createMeterDefinitions() {
    productionDataProvider.meterDefinitions().forEach(meterDefinitions::save);
  }

  private void createOrganisations() {
    productionDataProvider.organisations()
      .stream()
      .filter(organisation -> !organisations.findByCode(organisation.code).isPresent())
      .forEach(organisations::save);
  }

  private void createSuperAdministratorIfNotPresent() {
    User user = productionDataProvider.superAdminUser();
    if (!users.findByEmail(user.email).isPresent()) {
      users.create(user);
    }
  }

  private void createRoles() {
    roles.save(productionDataProvider.users());
  }
}
