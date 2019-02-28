package com.elvaco.mvp.core.usecase;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.DisplayMode;
import com.elvaco.mvp.core.domainmodels.DisplayQuantity;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.domainmodels.Units;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.exception.InvalidMeterDefinition;
import com.elvaco.mvp.core.exception.Unauthorized;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.core.spi.repository.MeterDefinitions;
import com.elvaco.mvp.core.unitconverter.UnitConverter;
import com.elvaco.mvp.testing.fixture.DefaultTestFixture;
import com.elvaco.mvp.testing.repository.MockLogicalMeters;
import com.elvaco.mvp.testing.repository.MockMeterDefinitions;
import com.elvaco.mvp.testing.repository.MockOrganisations;
import com.elvaco.mvp.testing.security.MockAuthenticatedUser;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static com.elvaco.mvp.testing.fixture.UserTestData.ELVACO_ADMIN_USER;
import static com.elvaco.mvp.testing.fixture.UserTestData.ELVACO_SUPER_ADMIN_USER;
import static com.elvaco.mvp.testing.fixture.UserTestData.ELVACO_USER;
import static com.elvaco.mvp.testing.fixture.UserTestData.OTHER_ADMIN_USER;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;

public class MeterDefinitionUseCasesTest extends DefaultTestFixture {

  private MeterDefinition systemMeterDefinition;
  private MeterDefinitions meterDefinitions;

  @Before
  public void before() {
    systemMeterDefinition = systemMeterDefinition();
    meterDefinitions = new MockMeterDefinitions(List.of(systemMeterDefinition));
  }

  @Test
  public void findAll_superAdminWillGetAll() {
    meterDefinitions.save(meterDefinition(ELVACO_SUPER_ADMIN_USER.organisation));
    meterDefinitions.save(meterDefinition(organisation().build()));

    MeterDefinitionUseCases useCases = newUseCases(ELVACO_SUPER_ADMIN_USER);
    assertThat(useCases.findAll()).hasSize(3);
  }

  @Test
  public void findAll_adminWillGetAllForOrganisation() {
    User adminUser = newUser().organisation(organisation().build()).asAdmin().build();

    meterDefinitions.save(meterDefinition(defaultOrganisation()));
    meterDefinitions.save(meterDefinition(organisation().build()));
    var mdOrganisation = meterDefinitions.save(meterDefinition(adminUser.organisation));

    MeterDefinitionUseCases useCases = newUseCases(adminUser);
    assertThat(useCases.findAll()).hasSize(2)
      .extracting(md -> md.id)
      .containsOnly(mdOrganisation.id, systemMeterDefinition.id);
  }

  @Test
  public void findAll_userWillGetAllForOrganisation() {
    Organisation organisation = organisation().build();
    meterDefinitions.save(meterDefinition(organisation));
    MeterDefinition organisationMd =
      meterDefinitions.save(meterDefinition(ELVACO_USER.organisation));

    MeterDefinitionUseCases useCases = newUseCases(ELVACO_USER);
    assertThat(useCases.findAll())
      .extracting(md -> md.id)
      .containsExactlyInAnyOrder(organisationMd.id, systemMeterDefinition.id)
      .hasSize(2);
  }

  @Test
  public void savingPersistsMeterDefinition() {
    MeterDefinitionUseCases useCases = newUseCases(ELVACO_SUPER_ADMIN_USER);

    MeterDefinition saved = useCases.save(meterDefinition(ELVACO_ADMIN_USER.organisation));

    assertThat(meterDefinitions.findById(saved.id).get()).isEqualTo(saved);
  }

  @Test
  public void superAdminCanSaveOrganisationMeterDefinition() {
    MeterDefinitionUseCases useCases = newUseCases(ELVACO_SUPER_ADMIN_USER);

    MeterDefinition meterDefinition = meterDefinition(ELVACO_ADMIN_USER.organisation);
    assertThat(useCases.save(meterDefinition))
      .isEqualToIgnoringGivenFields(meterDefinition, "id");
  }

  @Test
  public void adminCanSaveOrganisationMeterDefinition() {
    MeterDefinitionUseCases useCases = newUseCases(ELVACO_ADMIN_USER);

    MeterDefinition meterDefinition = meterDefinition(ELVACO_ADMIN_USER.organisation);
    assertThat(useCases.save(meterDefinition))
      .isEqualToIgnoringGivenFields(meterDefinition, "id");
  }

  @Test
  public void adminCanNotSaveOrganisationMeterDefinitionForOtherOrganisation() {
    MeterDefinitionUseCases useCases = newUseCases(ELVACO_ADMIN_USER);

    assertThatThrownBy(() -> useCases.save(meterDefinition(OTHER_ADMIN_USER.organisation)))
      .isInstanceOf(Unauthorized.class).hasMessageContaining("User is not authorized to save");
  }

  @Test
  public void userCanNotSaveOrganisationMeterDefinition() {
    MeterDefinitionUseCases useCases = newUseCases(ELVACO_USER);

    assertThatThrownBy(() -> useCases.save(meterDefinition(ELVACO_USER.organisation)))
      .isInstanceOf(Unauthorized.class).hasMessageContaining("User is not authorized to save");
  }

  @Test
  public void adminCanNotSaveSystemMeterDefinitions() {
    MeterDefinitionUseCases useCases = newUseCases(ELVACO_ADMIN_USER);

    assertThatThrownBy(() -> useCases.save(systemMeterDefinition()))
      .isInstanceOf(Unauthorized.class)
      .hasMessageContaining("System meter definitions can not be created");
  }

  @Test
  public void userCanNotSaveSystemMeterDefinitions() {
    MeterDefinitionUseCases useCases = newUseCases(ELVACO_USER);

    assertThatThrownBy(() -> useCases.save(systemMeterDefinition()))
      .isInstanceOf(Unauthorized.class)
      .hasMessageContaining("System meter definitions can not be created");
  }

  @Test
  public void superAdminCanNotSaveSystemMeterDefinitions() {
    MeterDefinitionUseCases useCases = newUseCases(ELVACO_SUPER_ADMIN_USER);

    assertThatThrownBy(() -> useCases.save(systemMeterDefinition()))
      .isInstanceOf(Unauthorized.class)
      .hasMessageContaining("System meter definitions can not be created");
  }

  @Test
  @Ignore
  public void onlyOneSystemMeterDefinitionPerMedium() {
    MeterDefinitionUseCases useCases = newUseCases(ELVACO_SUPER_ADMIN_USER);

    assertThatThrownBy(() -> useCases.save(systemMeterDefinition()))
      .isInstanceOf(InvalidMeterDefinition.class)
      .hasMessageContaining("Only one system meter definition per medium");
  }

  @Test
  public void uniqueNamePerOrganisationAtSave() {
    MeterDefinition md = meterDefinition(organisation().build());
    meterDefinitions.save(md);
    MeterDefinitionUseCases useCases = newUseCases(ELVACO_SUPER_ADMIN_USER);

    MeterDefinition mdWithSameName = md.toBuilder().id(random().nextLong()).build();
    assertThatThrownBy(() -> useCases.save(mdWithSameName))
      .isInstanceOf(InvalidMeterDefinition.class)
      .hasMessageContaining("Name must be unique for organisation");
  }

  @Test
  public void uniqueNamePerOrganisationAtUpdate() {
    Organisation organisation = organisation().build();
    MeterDefinition existing1 = meterDefinition(organisation);
    meterDefinitions.save(existing1);
    MeterDefinition existing2 = meterDefinition(organisation);
    meterDefinitions.save(existing2);
    MeterDefinitionUseCases useCases = newUseCases(ELVACO_SUPER_ADMIN_USER);

    MeterDefinition updateToExisting1Name = existing2.toBuilder().name(existing1.name).build();
    assertThatThrownBy(() -> useCases.update(updateToExisting1Name))
      .isInstanceOf(InvalidMeterDefinition.class)
      .hasMessageContaining("Name must be unique for organisation");
  }

  @Test
  public void onlyOneAutoAppliedMeterDefinitionPerOrganisationAndMedium() {
    MeterDefinition md = meterDefinition()
      .organisation(organisation().build())
      .medium(systemMeterDefinition.medium)
      .autoApply(true)
      .build();
    meterDefinitions.save(md);
    MeterDefinition systemMeterDefinitionWithOtherMedium = systemMeterDefinition();
    meterDefinitions.save(systemMeterDefinitionWithOtherMedium);
    MeterDefinitionUseCases useCases = newUseCases(ELVACO_SUPER_ADMIN_USER);

    assertThatThrownBy(() -> useCases.save(
      meterDefinition()
        .organisation(md.organisation)
        .medium(md.medium)
        .autoApply(true)
        .build()
    )).isInstanceOf(InvalidMeterDefinition.class)
      .hasMessageContaining("Only one auto applied meter definition");

    MeterDefinition otherOrganisation = meterDefinition()
      .organisation(organisation().build())
      .medium(md.medium)
      .autoApply(true)
      .build();
    assertThat(useCases.save(otherOrganisation))
      .isEqualTo(meterDefinitions.findById(otherOrganisation.id).get());

    MeterDefinition otherMedium = meterDefinition()
      .organisation(md.organisation)
      .medium(systemMeterDefinitionWithOtherMedium.medium)
      .autoApply(true)
      .build();
    assertThat(useCases.save(otherMedium))
      .isEqualTo(meterDefinitions.findById(otherMedium.id).get());
  }

  @Test
  public void savingWithIncompatibleUnitsIsRejected() {
    MeterDefinitionUseCases useCases = newUseCases(
      ELVACO_SUPER_ADMIN_USER,
      unitConverter(false),
      new MockLogicalMeters()
    );
    assertThatThrownBy(() -> useCases.save(
      meterDefinition()
        .organisation(ELVACO_USER.organisation)
        .quantities(Set.of(new DisplayQuantity(Quantity.POWER, DisplayMode.READOUT, Units.PERCENT)))
        .build()
      )
    ).hasMessageContaining("Invalid display unit '%' for quantity");
  }

  @Test
  public void savingWithDuplicatedQuantitesIsRejected() {
    MeterDefinitionUseCases useCases = newUseCases(ELVACO_SUPER_ADMIN_USER);
    assertThatThrownBy(() -> useCases.save(
      meterDefinition()
        .organisation(ELVACO_USER.organisation)
        .quantities(Set.of(
          new DisplayQuantity(Quantity.ENERGY, DisplayMode.READOUT, Units.KILOWATT_HOURS),
          new DisplayQuantity(Quantity.ENERGY, DisplayMode.READOUT, Units.MEGAWATT_HOURS)
        ))
        .build()
      )
    ).hasMessageContaining("Duplicated quantity");
  }

  @Test
  public void savingWithBothReadoutAndConsumptionForQuantity() {
    MeterDefinitionUseCases useCases = newUseCases(ELVACO_SUPER_ADMIN_USER);
    var meterDefinition = useCases.save(
      meterDefinition()
        .organisation(ELVACO_USER.organisation)
        .quantities(Set.of(
          new DisplayQuantity(Quantity.ENERGY, DisplayMode.READOUT, Units.KILOWATT_HOURS),
          new DisplayQuantity(Quantity.ENERGY, DisplayMode.CONSUMPTION, Units.KILOWATT_HOURS)
        ))
        .build()
    );
    assertThat(meterDefinitions.findById(meterDefinition.id).get().quantities)
      .extracting(q -> q.quantity, q -> q.displayMode)
      .containsOnly(
        tuple(Quantity.ENERGY, DisplayMode.READOUT),
        tuple(Quantity.ENERGY, DisplayMode.CONSUMPTION)
      );
  }

  @Test
  public void meterDefinitionIsAppliedToOrgansiationMetersAtCreate() {
    Organisation organisation1 = organisation().build();
    Organisation organisation2 = organisation().build();
    var meter1 = logicalMeter()
      .organisationId(organisation1.id)
      .meterDefinition(systemMeterDefinition)
      .build();
    var meter2 = logicalMeter()
      .organisationId(organisation2.id)
      .meterDefinition(systemMeterDefinition)
      .build();
    LogicalMeters logicalMeters = new MockLogicalMeters(asList(meter1, meter2));

    var useCases = newUseCases(ELVACO_SUPER_ADMIN_USER, logicalMeters);
    var organisationDefinition = useCases.save(
      meterDefinition()
        .organisation(organisation1)
        .medium(systemMeterDefinition.medium)
        .autoApply(true)
        .build());

    assertThat(logicalMeters.findById(meter1.id).get().meterDefinition)
      .isEqualTo(organisationDefinition);
    assertThat(logicalMeters.findById(meter2.id).get().meterDefinition)
      .isEqualTo(systemMeterDefinition);
  }

  @Test
  public void update_userCannotUpdateMeterDefinition() {
    MeterDefinition md = meterDefinition(organisation().build());
    MeterDefinition saved = meterDefinitions.save(md);
    MeterDefinitionUseCases useCases = newUseCases(ELVACO_USER);

    MeterDefinition updateMeterDefinition = saved.toBuilder()
      .name(UUID.randomUUID().toString())
      .build();
    assertThatThrownBy(() -> useCases.update(updateMeterDefinition))
      .isInstanceOf(Unauthorized.class).hasMessageContaining("User is not authorized to save");
  }

  @Test
  public void update_adminCanUpdateOrganisationMeterDefinition() {
    MeterDefinition md = meterDefinition(ELVACO_ADMIN_USER.organisation);
    MeterDefinition saved = meterDefinitions.save(md);
    MeterDefinitionUseCases useCases = newUseCases(ELVACO_ADMIN_USER);

    MeterDefinition updateMeterDefinition = saved.toBuilder().medium(medium().build()).build();
    useCases.update(updateMeterDefinition);

    assertThat(meterDefinitions.findById(md.id).get().name).isEqualTo(updateMeterDefinition.name);
  }

  @Test
  public void update_superAdminCanUpdateOrganisationMeterDefinition() {
    MeterDefinition md = meterDefinition(organisation().build());
    MeterDefinition saved = meterDefinitions.save(md);
    MeterDefinitionUseCases useCases = newUseCases(ELVACO_SUPER_ADMIN_USER);

    MeterDefinition updateMeterDefinition = saved.toBuilder().medium(medium().build()).build();
    useCases.update(updateMeterDefinition);

    assertThat(meterDefinitions.findById(md.id).get().name).isEqualTo(updateMeterDefinition.name);
  }

  @Test
  public void update_superAdminCanUpdateSystemMeterDefinition() {
    MeterDefinitionUseCases useCases = newUseCases(ELVACO_SUPER_ADMIN_USER);

    DisplayQuantity displayQuantity = new DisplayQuantity(
      Quantity.HUMIDITY,
      DisplayMode.READOUT,
      Units.PERCENT
    );
    MeterDefinition updateMeterDefinition = systemMeterDefinition.toBuilder()
      .quantities(Set.of(displayQuantity))
      .build();
    useCases.update(updateMeterDefinition);

    assertThat(meterDefinitions.findById(systemMeterDefinition.id)
      .get().quantities).containsExactly(displayQuantity);
  }

  @Test
  public void update_logicalMetersAreUpdatedWhenOrganisationIsUpdated() {
    Organisation organisation1 = organisation().name("Organisation1").build();
    Organisation organisation2 = organisation().name("Organisation2").build();

    var meterDefintion1 = meterDefinitions.save(meterDefinition()
      .organisation(organisation1)
      .medium(systemMeterDefinition.medium)
      .name("The one to change")
      .autoApply(true)
      .build());

    var meter1 = logicalMeter()
      .organisationId(organisation1.id)
      .meterDefinition(meterDefintion1)
      .externalId("Meter1")
      .build();
    var meter2 = logicalMeter()
      .organisationId(organisation2.id)
      .meterDefinition(systemMeterDefinition)
      .externalId("Meter2")
      .build();
    LogicalMeters logicalMeters = new MockLogicalMeters(asList(meter1, meter2));

    var updatedMeterDefinition = newUseCases(ELVACO_SUPER_ADMIN_USER, logicalMeters)
      .update(meterDefintion1.toBuilder()
        .organisation(organisation2)
        .autoApply(true)
        .build());

    assertThat(logicalMeters.findById(meter1.id).get().meterDefinition)
      .isEqualTo(systemMeterDefinition);
    assertThat(logicalMeters.findById(meter2.id).get().meterDefinition)
      .isEqualTo(updatedMeterDefinition);
  }

  @Test
  public void superAdminCanNotDeleteSystemMeterDefinition() {
    assertThatThrownBy(() -> newUseCases(ELVACO_SUPER_ADMIN_USER)
      .deleteById(systemMeterDefinition.id))
      .isInstanceOf(Unauthorized.class).hasMessageContaining("User is not authorized to delete");
  }

  @Test
  public void adminCanNotDeleteSystemMeterDefinition() {
    assertThatThrownBy(() -> newUseCases(ELVACO_ADMIN_USER)
      .deleteById(systemMeterDefinition.id))
      .isInstanceOf(Unauthorized.class).hasMessageContaining("User is not authorized to delete");
  }

  @Test
  public void userCanNotDeleteSystemMeterDefinition() {
    assertThatThrownBy(() -> newUseCases(ELVACO_USER)
      .deleteById(systemMeterDefinition.id))
      .isInstanceOf(Unauthorized.class).hasMessageContaining("User is not authorized to delete");
  }

  @Test
  public void superAdminCanDeleteOrganistaionMeterDefinition() {
    MeterDefinition md = meterDefinition()
      .organisation(organisation().build())
      .medium(systemMeterDefinition.medium)
      .build();
    meterDefinitions.save(md);

    assertThat(newUseCases(ELVACO_SUPER_ADMIN_USER).deleteById(md.id)).isEqualTo(md);
    assertThat(meterDefinitions.findById(md.id)).isEmpty();
  }

  @Test
  public void adminCanDeleteOrganistaionMeterDefinition() {
    MeterDefinition md = meterDefinition()
      .organisation(ELVACO_ADMIN_USER.organisation)
      .medium(systemMeterDefinition.medium)
      .build();
    meterDefinitions.save(md);

    assertThat(newUseCases(ELVACO_ADMIN_USER).deleteById(md.id)).isEqualTo(md);
    assertThat(meterDefinitions.findById(md.id)).isEmpty();
  }

  @Test
  public void adminCanNotDeleteOrganistaionMeterDefinitionForOtherOrganisation() {
    MeterDefinition md = meterDefinition(organisation().build());
    meterDefinitions.save(md);

    assertThatThrownBy(() -> newUseCases(ELVACO_ADMIN_USER).deleteById(md.id))
      .isInstanceOf(Unauthorized.class).hasMessageContaining("User is not authorized to delete");
  }

  @Test
  public void userCanNotDeleteMeterDefinition() {
    MeterDefinition md = meterDefinition(ELVACO_USER.organisation);
    meterDefinitions.save(md);

    assertThatThrownBy(() -> newUseCases(ELVACO_USER).deleteById(md.id))
      .isInstanceOf(Unauthorized.class).hasMessageContaining("User is not authorized to delete");
  }

  @Test
  public void meterDefinitionIsSetToDefaultWhenOrganisationMeterDefinitionIsDeleted() {
    MeterDefinition md1 = meterDefinition()
      .organisation(organisation().build())
      .medium(systemMeterDefinition.medium)
      .autoApply(true)
      .build();
    MeterDefinition md2 = meterDefinition()
      .organisation(organisation().build())
      .medium(systemMeterDefinition.medium)
      .autoApply(true)
      .build();
    meterDefinitions.save(md1);
    meterDefinitions.save(md2);

    var meterWithDefinition1 = LogicalMeter.builder()
      .organisationId(md1.organisation.id)
      .meterDefinition(md1)
      .build();
    var meterWithDefinition2 = LogicalMeter.builder()
      .organisationId(md2.organisation.id)
      .meterDefinition(md2)
      .build();
    LogicalMeters mockLogicalMeters = new MockLogicalMeters(asList(
      meterWithDefinition1,
      meterWithDefinition2
    ));
    var useCases = newUseCases(ELVACO_SUPER_ADMIN_USER, mockLogicalMeters);

    useCases.deleteById(md1.id);

    assertThat(mockLogicalMeters.findById(meterWithDefinition1.id).get().meterDefinition)
      .isEqualTo(systemMeterDefinition);
    assertThat(mockLogicalMeters.findById(meterWithDefinition2.id).get().meterDefinition)
      .isEqualTo(md2);
  }

  MeterDefinition systemMeterDefinition() {
    return meterDefinition().organisation(null).name("System Default").build();
  }

  MeterDefinition meterDefinition(Organisation organisation) {
    return meterDefinition().organisation(organisation).build();
  }

  private MeterDefinitionUseCases newUseCases(User user) {
    return newUseCases(user, unitConverter(true), new MockLogicalMeters());
  }

  private MeterDefinitionUseCases newUseCases(User user, LogicalMeters logicalMeters) {
    return newUseCases(user, unitConverter(true), logicalMeters);
  }

  private MeterDefinitionUseCases newUseCases(
    User user,
    UnitConverter unitConverter,
    LogicalMeters logicalMeters
  ) {
    return new MeterDefinitionUseCases(
      new MockAuthenticatedUser(user, "123"),
      meterDefinitions,
      unitConverter,
      new MockOrganisations(),
      null,
      null,
      (medium -> meterDefinitions.findAll().stream()
        .filter(md -> md.isDefault())
        .filter(md -> md.medium.name.equals(medium.name))
        .findAny()),
      logicalMeters
    );
  }
}
