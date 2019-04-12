package com.elvaco.mvp.configuration.config;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.elvaco.mvp.configuration.bootstrap.production.ProductionDataProvider;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.spi.repository.MeterDefinitions;
import com.elvaco.mvp.core.spi.repository.Organisations;
import com.elvaco.mvp.core.spi.repository.Quantities;
import com.elvaco.mvp.core.spi.repository.Roles;
import com.elvaco.mvp.core.spi.repository.Users;
import com.elvaco.mvp.testdata.IntegrationTest;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

public class DataProviderConfigTest extends IntegrationTest {

  @Autowired
  private Users users;

  @Autowired
  private Organisations organisations;

  @Autowired
  private Organisation rootOrganisation;

  @Autowired
  private Roles roles;

  @Autowired
  private MeterDefinitions meterDefinitions;

  @Autowired
  private Quantities quantities;

  @Autowired
  private ProductionDataProvider productionDataProvider;

  @Test
  public void users() {
    var preloadedUsers = users.findAll();

    assertThat(preloadedUsers).hasSize(1);
    assertThat(preloadedUsers.get(0))
      .isEqualToIgnoringGivenFields(
        productionDataProvider.superAdminUser(),
        "id", "password", "organisation.id"
      );
  }

  @Test
  public void organisations() {
    var preloadedOrganisations = organisations.findAll();

    assertThat(preloadedOrganisations).hasSize(1);
    assertThat(preloadedOrganisations.get(0))
      .isEqualToIgnoringGivenFields(
        productionDataProvider.organisations().get(0),
        "id"
      );
  }

  @Test
  public void rootOrganisation() {
    assertThat(rootOrganisation)
      .isEqualToIgnoringGivenFields(Organisation.of("Elvaco"), "id");
  }

  @Test
  public void roles() {
    assertThat(roles.findAll())
      .containsAll(productionDataProvider.roles());
  }

  @Test
  public void meterDefinitions() {
    List<MeterDefinition> all = meterDefinitions.findAll();
    assertThat(all)
      .hasSize(productionDataProvider.meterDefinitions().size());

    all.stream()
      .forEach(saved -> {
        var unsavedMeterDefinition = productionDataProvider.meterDefinitions().stream()
          .filter(unsaved -> unsaved.medium.name.equals(saved.medium.name))
          .findAny()
          .orElseThrow();

        assertThat(unsavedMeterDefinition)
          .isEqualToIgnoringGivenFields(saved, "quantities", "medium", "id");

        assertThat(unsavedMeterDefinition.medium)
          .isEqualToIgnoringGivenFields(saved.medium, "id");

        assertQuantitiesMatchFixtureIgnoringId(
          saved.quantities.stream()
            .map(displayQuantity -> displayQuantity.quantity)
            .collect(
              Collectors.toSet()),
          unsavedMeterDefinition.quantities.stream()
            .map(displayQuantity -> displayQuantity.quantity)
            .collect(
              Collectors.toSet())
        );
      });
  }

  @Test
  public void quantities() {
    List<Quantity> all = quantities.findAll();
    assertThat(all)
      .allMatch(quantity -> quantity.id != null)
      .hasSize(Quantity.QUANTITIES.size());

    assertQuantitiesMatchFixtureIgnoringId(all, Quantity.QUANTITIES);
  }

  private void assertQuantitiesMatchFixtureIgnoringId(
    Collection<Quantity> quantitiesToMatch,
    Collection<Quantity> quantitiesBaseCase
  ) {
    quantitiesToMatch.stream()
      .forEach(quantity ->
        assertThat(unsavedQuantityInFixture(quantity.name, quantitiesBaseCase))
          .get()
          .isEqualToIgnoringGivenFields(quantity, "id"));
  }

  private Optional<Quantity> unsavedQuantityInFixture(
    String quantityName,
    Collection<Quantity> quantitiesBaseCase
  ) {
    return quantitiesBaseCase.stream()
      .filter(quantity -> quantity.name.equals(quantityName))
      .findAny();
  }
}
