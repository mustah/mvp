package com.elvaco.mvp.configuration.bootstrap.production;

import com.elvaco.mvp.core.spi.repository.Users;
import com.elvaco.mvp.database.repository.jpa.MeterDefinitionJpaRepository;
import com.elvaco.mvp.database.repository.jpa.OrganisationJpaRepository;
import com.elvaco.mvp.database.repository.jpa.RoleJpaRepository;
import com.elvaco.mvp.testdata.IntegrationTest;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

public class ProductionDataLoaderTest extends IntegrationTest {

  @Autowired
  private ProductionDataLoader productionDataLoader;

  @Autowired
  private RoleJpaRepository roleRepository;

  @Autowired
  private MeterDefinitionJpaRepository meterDefinitionJpaRepository;

  @Autowired
  private OrganisationJpaRepository organisationJpaRepository;

  @Autowired
  private Users users;

  @Test
  public void productionDataLoaderIsIdempotent() {
    try {
      /*NOTE: The production data loader is run once before this as a consequence of
       * constructing the Spring context. Thus, this is the second time it's run! */
      productionDataLoader.run("");
    } catch (Exception ex) {
      Assertions.fail("Running production data loader failed", ex);
    }

    assertThat(roleRepository.findAll())
      .hasSize(productionDataLoader.getProductionDataProvider().users().size());

    assertThat(meterDefinitionJpaRepository.findAll())
      .hasSize(productionDataLoader.getProductionDataProvider().meterDefinitions().size());

    assertThat(organisationJpaRepository.findAll())
      .hasSize(productionDataLoader.getProductionDataProvider().organisations().size());

    assertThat(users.findAll())
      .filteredOn("name", "System Administrator")
      .hasSize(1);
  }
}
