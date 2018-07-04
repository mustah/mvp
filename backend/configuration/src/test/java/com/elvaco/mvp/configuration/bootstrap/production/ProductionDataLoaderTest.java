package com.elvaco.mvp.configuration.bootstrap.production;

import java.util.Collections;
import java.util.List;

import com.elvaco.mvp.core.domainmodels.Language;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.Role;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.spi.repository.Organisations;
import com.elvaco.mvp.testing.repository.MockMeterDefinitions;
import com.elvaco.mvp.testing.repository.MockOrganisations;
import com.elvaco.mvp.testing.repository.MockQuantities;
import com.elvaco.mvp.testing.repository.MockRoles;
import com.elvaco.mvp.testing.repository.MockUsers;
import org.junit.Test;

import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class ProductionDataLoaderTest {

  @Test
  public void organisationUniquenessIsEnforcedByCode() {
    Organisation initialOrganisation = new Organisation(
      randomUUID(),
      "Organisation",
      "organisation",
      "Organisation"
    );
    Organisation secondOrganisation = new Organisation(
      randomUUID(),
      "Organisation",
      "organisation",
      "Organisation"
    );

    List<Organisation> existingOrganisations = singletonList(initialOrganisation);
    Organisations organisations = new MockOrganisations(existingOrganisations);
    ProductionDataLoader productionDataLoader = newProductionDataLoader(
      newDataProvider(singletonList(secondOrganisation)),
      organisations
    );

    productionDataLoader.seedDatabase();

    assertThat(organisations.findAll()).hasSize(1);
  }

  private FakeProductionDataProvider newDataProvider(
    List<Organisation> organisations
  ) {
    return new FakeProductionDataProvider(
      new User(
        randomUUID(),
        "superadmin",
        "superadmin@elvaco.se",
        "password",
        Language.en,
        organisations.get(0),
        singletonList(Role.SUPER_ADMIN)
      ),
      organisations
    );
  }

  private ProductionDataLoader newProductionDataLoader(
    ProductionDataProvider productionDataProvider,
    Organisations organisations
  ) {
    return new ProductionDataLoader(
      new MockRoles(),
      new MockMeterDefinitions(),
      organisations,
      new MockUsers(Collections.emptyList()),
      productionDataProvider,
      new MockQuantities()
    );
  }
}
