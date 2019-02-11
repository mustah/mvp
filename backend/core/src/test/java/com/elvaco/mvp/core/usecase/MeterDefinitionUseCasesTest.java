package com.elvaco.mvp.core.usecase;

import java.util.Collections;
import java.util.Set;

import com.elvaco.mvp.core.domainmodels.DisplayMode;
import com.elvaco.mvp.core.domainmodels.DisplayQuantity;
import com.elvaco.mvp.core.domainmodels.MeasurementUnit;
import com.elvaco.mvp.core.domainmodels.Medium;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.domainmodels.Units;
import com.elvaco.mvp.core.domainmodels.User;
import com.elvaco.mvp.core.exception.Unauthorized;
import com.elvaco.mvp.core.spi.repository.MeterDefinitions;
import com.elvaco.mvp.core.unitconverter.UnitConverter;
import com.elvaco.mvp.testing.repository.MockMeterDefinitions;
import com.elvaco.mvp.testing.security.MockAuthenticatedUser;

import org.junit.Test;

import static com.elvaco.mvp.testing.fixture.UserTestData.ELVACO_ADMIN_USER;
import static com.elvaco.mvp.testing.fixture.UserTestData.ELVACO_SUPER_ADMIN_USER;
import static com.elvaco.mvp.testing.fixture.UserTestData.ELVACO_USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class MeterDefinitionUseCasesTest {

  private static final MeterDefinition SYSTEM_METER_DEFINITION = newMeterDefinition(null);

  @Test
  public void savingPersistsMeterDefinition() {
    MeterDefinitions meterDefinitions = new MockMeterDefinitions();
    MeterDefinitionUseCases useCases = newUseCases(ELVACO_SUPER_ADMIN_USER, meterDefinitions);

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
  public void userCanNotSaveOrganisationMeterDefinition() {
    MeterDefinitionUseCases useCases = newUseCases(ELVACO_USER);

    assertThatThrownBy(() -> useCases.save(newMeterDefinition(ELVACO_USER.organisation)))
      .isInstanceOf(Unauthorized.class);
  }

  @Test
  public void adminCanNotSaveSystemMeterDefinitions() {
    MeterDefinitionUseCases useCases = newUseCases(ELVACO_ADMIN_USER);
    assertThatThrownBy(() -> useCases.save(SYSTEM_METER_DEFINITION))
      .isInstanceOf(Unauthorized.class);
  }

  @Test
  public void userCanNotSaveSystemMeterDefinitions() {
    MeterDefinitionUseCases useCases = newUseCases(ELVACO_USER);
    assertThatThrownBy(() -> useCases.save(SYSTEM_METER_DEFINITION))
      .isInstanceOf(Unauthorized.class);
  }

  @Test
  public void superAdminCanSaveSystemMeterDefinitions() {
    MeterDefinitionUseCases useCases = newUseCases(ELVACO_SUPER_ADMIN_USER);
    assertThat(useCases.save(SYSTEM_METER_DEFINITION))
      .isEqualToIgnoringGivenFields(SYSTEM_METER_DEFINITION, "id");
  }

  @Test
  public void savingWithIncompatibleUnitsIsRejected() {
    MeterDefinitionUseCases useCases = newUseCases(
      ELVACO_SUPER_ADMIN_USER,
      new MockMeterDefinitions(),
      newUnitConverter(false)
    );
    assertThatThrownBy(() -> useCases.save(
      newMeterDefinition(
        ELVACO_USER.organisation,
        Set.of(new DisplayQuantity(Quantity.POWER, DisplayMode.READOUT, Units.PERCENT))
      ))
    ).hasMessageContaining("Invalid display unit '%' for quantity");
  }

  private MeterDefinitionUseCases newUseCases(User user) {
    return newUseCases(user, new MockMeterDefinitions());
  }

  private MeterDefinitionUseCases newUseCases(
    User user,
    MeterDefinitions meterDefinitions,
    UnitConverter unitConverter
  ) {
    return new MeterDefinitionUseCases(
      new MockAuthenticatedUser(user, "123"),
      meterDefinitions,
      unitConverter
    );
  }

  private MeterDefinitionUseCases newUseCases(
    User user,
    MeterDefinitions meterDefinitions
  ) {
    return new MeterDefinitionUseCases(
      new MockAuthenticatedUser(user, "123"),
      meterDefinitions,
      newUnitConverter(true)
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
    return newMeterDefinition(organisation, Collections.emptySet());
  }

  private static MeterDefinition newMeterDefinition(
    Organisation organisation, Set<DisplayQuantity> displayQuantities
  ) {
    return new MeterDefinition(
      null,
      organisation,
      "test",
      new Medium(0L, "test medium"),
      true,
      displayQuantities
    );
  }
}
