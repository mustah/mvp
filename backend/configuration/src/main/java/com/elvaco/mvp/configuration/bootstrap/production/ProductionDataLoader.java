package com.elvaco.mvp.configuration.bootstrap.production;

import com.elvaco.mvp.core.spi.repository.MeterDefinitions;
import com.elvaco.mvp.core.spi.repository.Organisations;
import com.elvaco.mvp.core.spi.repository.Users;
import com.elvaco.mvp.database.repository.jpa.RoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(1)
@Component
@Slf4j
class ProductionDataLoader implements CommandLineRunner {
  private final RoleRepository roleRepository;
  private final ProductionData productionData;
  private final MeterDefinitions meterDefinitions;
  private final Organisations organisations;
  private final Users users;

  @Autowired
  ProductionDataLoader(
    RoleRepository roleRepository,
    MeterDefinitions meterDefinitions,
    Organisations organisations,
    Users users
  ) {
    this.roleRepository = roleRepository;
    this.meterDefinitions = meterDefinitions;
    this.organisations = organisations;
    this.productionData = new ProductionData();
    this.users = users;
  }

  @Override
  public void run(String... args) throws Exception {
    log.info("Seeding database with initial data ...");

    createRoles();
    createElvacoOrganisation();
    createSuperAdministrator();
    createMeterDefinitions();
    log.info("Initial database seeding done.");
  }

  public ProductionData getProductionData() {
    return productionData;
  }

  private void createMeterDefinitions() {
    productionData.meterDefinitions().forEach(meterDefinitions::save);
  }

  private void createElvacoOrganisation() {
    productionData.organisations().forEach(organisations::save);
  }

  private void createSuperAdministrator() {
    if (!users.findById(1L).isPresent()) {
      users.create(
        productionData.superAdmin()
      );
    }
  }

  private void createRoles() {
    roleRepository.save(
      productionData.users()
    );
  }

}
