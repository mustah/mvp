package com.elvaco.mvp.core.usecase;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import com.elvaco.mvp.core.access.SystemMeterDefinitionProvider;
import com.elvaco.mvp.core.domainmodels.DisplayMode;
import com.elvaco.mvp.core.domainmodels.DisplayQuantity;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.MeasurementUnit;
import com.elvaco.mvp.core.domainmodels.Medium;
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
import com.elvaco.mvp.testing.repository.MockLogicalMeters;
import com.elvaco.mvp.testing.repository.MockMeterDefinitions;
import com.elvaco.mvp.testing.repository.MockOrganisations;
import com.elvaco.mvp.testing.security.MockAuthenticatedUser;

import org.junit.Before;
import org.junit.Test;

import static com.elvaco.mvp.testing.fixture.UserTestData.ELVACO_ADMIN_USER;
import static com.elvaco.mvp.testing.fixture.UserTestData.ELVACO_SUPER_ADMIN_USER;
import static com.elvaco.mvp.testing.fixture.UserTestData.ELVACO_USER;
import static com.elvaco.mvp.testing.fixture.UserTestData.OTHER_ADMIN_USER;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class MeterDefinitionUseCasesTest {

  private static final MeterDefinition SYSTEM_METER_DEFINITION = newMeterDefinition(
    null,
    newMedium(),
    false
  );

  private static final MeterDefinition SYSTEM_METER_DEFINITION_2 = newMeterDefinition(
    null,
    newMedium(),
    false
  );

  private final SystemMeterDefinitionProvider systemMeterDefinitionProvider =
    medium -> {
      if (SYSTEM_METER_DEFINITION.medium.name.equals(medium.name)) {
        return Optional.of(SYSTEM_METER_DEFINITION);
      }
      if (SYSTEM_METER_DEFINITION_2.medium.name.equals(medium.name)) {
        return Optional.of(SYSTEM_METER_DEFINITION_2);
      }
      return Optional.empty();
    };

  private MeterDefinitions meterDefinitions;

  @Before
  public void before() {
    meterDefinitions = new MockMeterDefinitions(List.of(SYSTEM_METER_DEFINITION));
  }

  @Test
  public void savingPersistsMeterDefinition() {
    MeterDefinitionUseCases useCases = newUseCases(ELVACO_SUPER_ADMIN_USER);

    MeterDefinition saved = useCases.save(newMeterDefinition(ELVACO_ADMIN_USER.organisation));

    assertThat(meterDefinitions.findById(saved.id).get()).isEqualTo(saved);
  }

  @Test
  public void superAdminCanSaveOrganisationMeterDefinition() {
    MeterDefinitionUseCases useCases = newUseCases(ELVACO_SUPER_ADMIN_USER);

    MeterDefinition meterDefinition = newMeterDefinition(ELVACO_ADMIN_USER.organisation);
    assertThat(useCases.save(meterDefinition))
      .isEqualToIgnoringGivenFields(meterDefinition, "id");
  }

  @Test
  public void adminCanSaveOrganisationMeterDefinition() {
    MeterDefinitionUseCases useCases = newUseCases(ELVACO_ADMIN_USER);

    MeterDefinition meterDefinition = newMeterDefinition(ELVACO_ADMIN_USER.organisation);
    assertThat(useCases.save(meterDefinition))
      .isEqualToIgnoringGivenFields(meterDefinition, "id");
  }

  @Test
  public void adminCanNotSaveOrganisationMeterDefinitionForOtherOrganisation() {
    MeterDefinitionUseCases useCases = newUseCases(ELVACO_ADMIN_USER);

    assertThatThrownBy(() -> useCases.save(newMeterDefinition(OTHER_ADMIN_USER.organisation)))
      .isInstanceOf(Unauthorized.class).hasMessageContaining("User is not authorized to save");
  }

  @Test
  public void userCanNotSaveOrganisationMeterDefinition() {
    MeterDefinitionUseCases useCases = newUseCases(ELVACO_USER);

    assertThatThrownBy(() -> useCases.save(newMeterDefinition(ELVACO_USER.organisation)))
      .isInstanceOf(Unauthorized.class).hasMessageContaining("User is not authorized to save");
  }

  @Test
  public void adminCanNotSaveSystemMeterDefinitions() {
    MeterDefinitionUseCases useCases = newUseCases(ELVACO_ADMIN_USER);

    assertThatThrownBy(() -> useCases.save(newMeterDefinition(null)))
      .isInstanceOf(Unauthorized.class).hasMessageContaining("User is not authorized to save");
  }

  @Test
  public void userCanNotSaveSystemMeterDefinitions() {
    MeterDefinitionUseCases useCases = newUseCases(ELVACO_USER);

    assertThatThrownBy(() -> useCases.save(newMeterDefinition(null)))
      .isInstanceOf(Unauthorized.class).hasMessageContaining("User is not authorized to save");
  }

  @Test
  public void superAdminCanSaveSystemMeterDefinitions() {
    MeterDefinitionUseCases useCases = newUseCases(ELVACO_SUPER_ADMIN_USER);

    MeterDefinition meterDefinition = newMeterDefinition(
      null,
      newMedium(),
      false
    );
    assertThat(useCases.save(meterDefinition))
      .isEqualToIgnoringGivenFields(meterDefinition, "id");
  }

  @Test
  public void onlyOneSystemMeterDefinitionPerMedium() {
    MeterDefinitionUseCases useCases = newUseCases(ELVACO_SUPER_ADMIN_USER);

    assertThatThrownBy(() -> useCases.save(newMeterDefinition(null)))
      .isInstanceOf(InvalidMeterDefinition.class)
      .hasMessageContaining("Only one system meter definition per medium");
  }

  @Test
  public void onlyOneAutoAppliedMeterDefinitionPerOrganisationAndMedium() {
    MeterDefinition md = newMeterDefinition(
      newOrganisation(),
      SYSTEM_METER_DEFINITION.medium,
      true
    );
    meterDefinitions.save(md);
    MeterDefinitionUseCases useCases = newUseCases(ELVACO_SUPER_ADMIN_USER);

    assertThatThrownBy(() -> useCases.save(newMeterDefinition(
      md.organisation,
      md.medium,
      true
    ))).isInstanceOf(InvalidMeterDefinition.class)
      .hasMessageContaining("Only one auto applied meter definition");

    MeterDefinition otherOrganisation = newMeterDefinition(
      newOrganisation(),
      md.medium,
      true
    );
    assertThat(useCases.save(otherOrganisation))
      .isEqualTo(meterDefinitions.findById(otherOrganisation.id).get());

    MeterDefinition otherMedium = newMeterDefinition(
      md.organisation,
      SYSTEM_METER_DEFINITION_2.medium,
      true
    );
    assertThat(useCases.save(otherMedium))
      .isEqualTo(meterDefinitions.findById(otherMedium.id).get());
  }

  @Test
  public void savingWithIncompatibleUnitsIsRejected() {
    MeterDefinitionUseCases useCases = newUseCases(
      ELVACO_SUPER_ADMIN_USER,
      newUnitConverter(false),
      new MockLogicalMeters()
    );
    assertThatThrownBy(() -> useCases.save(
      newMeterDefinition(
        ELVACO_USER.organisation,
        Set.of(new DisplayQuantity(Quantity.POWER, DisplayMode.READOUT, Units.PERCENT))
      ))
    ).hasMessageContaining("Invalid display unit '%' for quantity");
  }

  @Test
  public void meterDefintionIsAppliedToOrgansiationMetersAtCreate() {
    Organisation organisation = newOrganisation();
    var meter1 = LogicalMeter.builder()
      .organisationId(organisation.id)
      .meterDefinition(SYSTEM_METER_DEFINITION)
      .build();
    var meter2 = LogicalMeter.builder()
      .organisationId(newOrganisation().id)
      .meterDefinition(SYSTEM_METER_DEFINITION)
      .build();
    LogicalMeters logicalMeters = new MockLogicalMeters(asList(meter1, meter2));

    var useCases = newUseCases(ELVACO_SUPER_ADMIN_USER, null, logicalMeters);
    var organisationDefintion = useCases.save(newMeterDefinition(organisation).toBuilder()
      .autoApply(true)
      .build());

    assertThat(logicalMeters.findById(meter1.id).get().meterDefinition)
      .isEqualTo(organisationDefintion);
    assertThat(logicalMeters.findById(meter2.id).get().meterDefinition)
      .isEqualTo(SYSTEM_METER_DEFINITION);
  }

  @Test
  public void superAdminCanNotDeleteSystemMeterDefintition() {
    assertThatThrownBy(() -> newUseCases(ELVACO_SUPER_ADMIN_USER)
      .deleteById(SYSTEM_METER_DEFINITION.id))
      .isInstanceOf(Unauthorized.class).hasMessageContaining("User is not authorized to delete");
  }

  @Test
  public void adminCanNotDeleteSystemMeterDefintition() {
    assertThatThrownBy(() -> newUseCases(ELVACO_ADMIN_USER)
      .deleteById(SYSTEM_METER_DEFINITION.id))
      .isInstanceOf(Unauthorized.class).hasMessageContaining("User is not authorized to delete");
  }

  @Test
  public void userCanNotDeleteSystemMeterDefintition() {
    assertThatThrownBy(() -> newUseCases(ELVACO_USER)
      .deleteById(SYSTEM_METER_DEFINITION.id))
      .isInstanceOf(Unauthorized.class).hasMessageContaining("User is not authorized to delete");
  }

  @Test
  public void superAdminCanDeleteOrganistaionMeterDefintition() {
    MeterDefinition md = newMeterDefinition(newOrganisation());
    meterDefinitions.save(md);

    assertThat(newUseCases(ELVACO_SUPER_ADMIN_USER).deleteById(md.id)).isEqualTo(md);
    assertThat(meterDefinitions.findById(md.id)).isEmpty();
  }

  @Test
  public void adminCanDeleteOrganistaionMeterDefintition() {
    MeterDefinition md = newMeterDefinition(ELVACO_ADMIN_USER.organisation);
    meterDefinitions.save(md);

    assertThat(newUseCases(ELVACO_ADMIN_USER).deleteById(md.id)).isEqualTo(md);
    assertThat(meterDefinitions.findById(md.id)).isEmpty();
  }

  @Test
  public void adminCanNotDeleteOrganistaionMeterDefintitionForOtherOrganisation() {
    MeterDefinition md = newMeterDefinition(newOrganisation());
    meterDefinitions.save(md);

    assertThatThrownBy(() -> newUseCases(ELVACO_ADMIN_USER).deleteById(md.id))
      .isInstanceOf(Unauthorized.class).hasMessageContaining("User is not authorized to delete");
  }

  @Test
  public void userCanNotDeleteMeterDefintition() {
    MeterDefinition md = newMeterDefinition(ELVACO_USER.organisation);
    meterDefinitions.save(md);

    assertThatThrownBy(() -> newUseCases(ELVACO_USER).deleteById(md.id))
      .isInstanceOf(Unauthorized.class).hasMessageContaining("User is not authorized to delete");
  }

  @Test
  public void meterDefinitionIsSetToDefaultWhenOrganisationMeterDefinitionIsDeleted() {
    MeterDefinition md1 = newMeterDefinition(
      newOrganisation(),
      SYSTEM_METER_DEFINITION.medium,
      true
    );
    MeterDefinition md2 = newMeterDefinition(
      newOrganisation(),
      SYSTEM_METER_DEFINITION.medium,
      true
    );
    meterDefinitions.save(md1);
    meterDefinitions.save(md2);

    var meterWithDefintion1 = LogicalMeter.builder()
      .organisationId(md1.organisation.id)
      .meterDefinition(md1)
      .build();
    var meterWithDefintion2 = LogicalMeter.builder()
      .organisationId(md2.organisation.id)
      .meterDefinition(md2)
      .build();
    LogicalMeters mockLogicalMeters = new MockLogicalMeters(asList(
      meterWithDefintion1,
      meterWithDefintion2
    ));
    var useCases = newUseCases(ELVACO_SUPER_ADMIN_USER, null, mockLogicalMeters);

    useCases.deleteById(md1.id);

    assertThat(mockLogicalMeters.findById(meterWithDefintion1.id).get().meterDefinition)
      .isEqualTo(SYSTEM_METER_DEFINITION);
    assertThat(mockLogicalMeters.findById(meterWithDefintion2.id).get().meterDefinition)
      .isEqualTo(md2);
  }

  private Organisation newOrganisation() {
    return new Organisation(
      UUID.randomUUID(),
      "test-organisation",
      "test-organisation",
      "test-organisation"
    );
  }

  private MeterDefinitionUseCases newUseCases(User user) {
    return newUseCases(user, newUnitConverter(true), new MockLogicalMeters());
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
      systemMeterDefinitionProvider,
      logicalMeters
    );
  }

  private UnitConverter newUnitConverter(boolean isSameDimension) {
    return new UnitConverter() {
      @Override
      public MeasurementUnit convert(
        MeasurementUnit measurementUnit, String targetUnit
      ) {
        return measurementUnit;
      }

      @Override
      public boolean isSameDimension(String firstUnit, String secondUnit) {
        return isSameDimension;
      }
    };
  }

  private static MeterDefinition newMeterDefinition(
    Organisation organisation
  ) {
    return newMeterDefinition(organisation, emptySet());
  }

  private static MeterDefinition newMeterDefinition(
    Organisation organisation, Set<DisplayQuantity> displayQuantities
  ) {
    return newMeterDefinition(
      organisation,
      displayQuantities,
      SYSTEM_METER_DEFINITION.medium,
      false
    );
  }

  private static MeterDefinition newMeterDefinition(
    Organisation organisation, boolean autoApply
  ) {
    return newMeterDefinition(
      organisation,
      emptySet(),
      SYSTEM_METER_DEFINITION.medium,
      autoApply
    );
  }

  private static MeterDefinition newMeterDefinition(
    Organisation organisation, Medium medium, boolean autoApply
  ) {
    return newMeterDefinition(
      organisation,
      emptySet(),
      medium,
      autoApply
    );
  }

  private static MeterDefinition newMeterDefinition(
    Organisation organisation,
    Set<DisplayQuantity> displayQuantities,
    Medium medium,
    boolean autoApply
  ) {
    return new MeterDefinition(
      new Random().nextLong(),
      organisation,
      "test",
      medium,
      autoApply,
      displayQuantities
    );
  }

  private static Medium newMedium() {
    long id = new Random().nextLong();
    return new Medium(id, "Test medium - " + id);
  }
}
