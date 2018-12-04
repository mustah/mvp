package com.elvaco.mvp.configuration.bootstrap.production;

import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.spi.repository.Organisations;
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

  private final Organisations organisations;
  private final Users users;
  private final ProductionDataProvider productionDataProvider;

  @Override
  public void run(String... args) {
    log.info("Seeding database with initial data ...");
    seedDatabase();
    log.info("Initial database seeding done.");
  }

  ProductionDataProvider getProductionDataProvider() {
    return productionDataProvider;
  }

  void seedDatabase() {
    createOrganisations();
    createSuperAdminIfNotPresent();
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
}
