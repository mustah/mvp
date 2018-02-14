package com.elvaco.mvp.configuration.bootstrap.production;

import com.elvaco.mvp.core.spi.repository.Users;
import com.elvaco.mvp.database.repository.jpa.MeterDefinitionJpaRepository;
import com.elvaco.mvp.database.repository.jpa.OrganisationJpaRepository;
import com.elvaco.mvp.database.repository.jpa.RoleRepository;
import com.elvaco.mvp.testdata.IntegrationTest;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

public class ProductionDataLoaderTest extends IntegrationTest {
  @Autowired
  ProductionDataLoader productionDataLoader;

  @Autowired
  RoleRepository roleRepository;

  @Autowired
  MeterDefinitionJpaRepository meterDefinitionJpaRepository;

  @Autowired
  OrganisationJpaRepository organisationJpaRepository;

  @Autowired
  Users users;

  @Test
  public void productionDataLoaderIsIdempotent() {
    try {
      /*NOTE: The production data loader is run once before this as a consequence of
       * constructing the Spring context. Thus, this is the second time it's run! */
      productionDataLoader.run("");
    } catch (Exception ex) {
      Assertions.fail("Running production data loader failed", ex);
    }
    ProductionData productionData = productionDataLoader.getProductionData();

    assertThat(roleRepository.findAll()).hasSize(productionData.users().size());
    assertThat(meterDefinitionJpaRepository.findAll()).hasSize(
      productionData.meterDefinitions().size());
    assertThat(organisationJpaRepository.findAll()).hasSize(productionData.organisations().size());
    assertThat(users.findAll())
      .filteredOn("name", "System Administrator").hasSize(1);
  }
}
