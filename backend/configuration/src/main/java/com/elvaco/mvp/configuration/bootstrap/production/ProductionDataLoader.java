package com.elvaco.mvp.configuration.bootstrap.production;

import java.util.List;

import com.elvaco.mvp.core.access.QuantityAccess;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.spi.repository.MeterDefinitions;
import com.elvaco.mvp.core.spi.repository.Organisations;
import com.elvaco.mvp.core.spi.repository.Quantities;
import com.elvaco.mvp.core.spi.repository.Roles;
import com.elvaco.mvp.core.spi.repository.Users;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(1)
@Component
@Slf4j
@RequiredArgsConstructor
class ProductionDataLoader implements CommandLineRunner {

  private final Roles roles;
  private final MeterDefinitions meterDefinitions;
  private final Organisations organisations;
  private final Users users;
  private final ProductionDataProvider productionDataProvider;
  private final Quantities quantities;

  @Override
  public void run(String... args) {
    log.info("Seeding database with initial data ...");
    seedDatabase();
    log.info("Initial database seeding done.");
  }

  void seedDatabase() {
    createRoles();
    createOrganisations();
    createSuperAdminIfNotPresent();
    createQuantities();
    createMeterDefinitions();
  }

  public ProductionDataProvider getProductionDataProvider() {
    return productionDataProvider;
  }

  private void createRoles() {
    roles.save(productionDataProvider.users());
  }

  private void createOrganisations() {
    productionDataProvider.organisations()
      .stream()
      .filter(organisation -> !organisations.findBySlug(organisation.slug).isPresent())
      .forEach(organisations::save);
  }

  private void createSuperAdminIfNotPresent() {
    User user = productionDataProvider.superAdminUser();
    if (!users.findByEmail(user.email).isPresent()) {
      users.create(user);
    }
  }

  private void createMeterDefinitions() {
    productionDataProvider.meterDefinitions().forEach(meterDefinitions::save);
  }

  private void createQuantities() {
    Quantity.QUANTITIES.forEach(quantity ->
      quantities.findByName(quantity.name).orElseGet(() -> quantities.save(quantity)));

    List<Quantity> all = quantities.findAll();
    QuantityAccess.singleton().loadAll(all);
    log.info("Loaded {} quantities. {}", all.size(), all);
  }
}
