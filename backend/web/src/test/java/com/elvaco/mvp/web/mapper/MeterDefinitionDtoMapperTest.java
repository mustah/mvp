package com.elvaco.mvp.web.mapper;

import java.util.Optional;
import java.util.Set;

import com.elvaco.mvp.core.domainmodels.DisplayMode;
import com.elvaco.mvp.core.domainmodels.DisplayQuantity;
import com.elvaco.mvp.core.domainmodels.Medium;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.domainmodels.Units;
import com.elvaco.mvp.web.dto.DisplayQuantityDto;
import com.elvaco.mvp.web.dto.IdNamedDto;
import com.elvaco.mvp.web.dto.MeterDefinitionDto;
import com.elvaco.mvp.web.dto.OrganisationDto;

import org.junit.Test;

import static com.elvaco.mvp.testing.fixture.OrganisationTestData.ELVACO;
import static org.assertj.core.api.Assertions.assertThat;

public class MeterDefinitionDtoMapperTest {

  private static final Quantity TEST_POWER = Quantity.POWER.toBuilder().id(1).build();

  @Test
  public void map_toDto_systemDefinition() {
    MeterDefinition meterDefinition = new MeterDefinition(
      1L,
      null,
      "test",
      new Medium(1L, "test-medium"),
      true,
      Set.of(new DisplayQuantity(TEST_POWER, DisplayMode.READOUT, 2, Units.WATT))
    );
    MeterDefinitionDto dto = MeterDefinitionDtoMapper.toDto(meterDefinition);

    assertThat(dto)
      .isEqualTo(new MeterDefinitionDto(
        1L,
        "test",
        Set.of(new DisplayQuantityDto(TEST_POWER.name, false, Units.WATT, 2)),
        null,
        new IdNamedDto("1", "test-medium"),
        true
      ));
  }

  @Test
  public void map_toDto() {
    MeterDefinition meterDefinition = new MeterDefinition(
      1L,
      ELVACO,
      "test",
      new Medium(1L, "test-medium"),
      true,
      Set.of(new DisplayQuantity(TEST_POWER, DisplayMode.READOUT, 2, Units.WATT))
    );
    MeterDefinitionDto dto = MeterDefinitionDtoMapper.toDto(meterDefinition);

    assertThat(dto)
      .isEqualTo(new MeterDefinitionDto(
        1L,
        "test",
        Set.of(new DisplayQuantityDto(TEST_POWER.name, false, Units.WATT, 2)),
        new OrganisationDto(ELVACO.id, ELVACO.name, ELVACO.slug),
        new IdNamedDto("1", "test-medium"),
        true
      ));
  }

  @Test
  public void map_toDomainModel() {
    MeterDefinitionDto dto = new MeterDefinitionDto(
      1L,
      "test",
      Set.of(new DisplayQuantityDto(TEST_POWER.name, false, Units.WATT, 2)),
      new OrganisationDto(ELVACO.id, ELVACO.name, ELVACO.slug),
      new IdNamedDto("1", "test-medium"),
      true
    );

    MeterDefinition domainModel = new MeterDefinitionDtoMapper(
      name -> Optional.of(TEST_POWER)
    ).toDomainModel(dto);
    assertThat(domainModel).isEqualTo(
      new MeterDefinition(
        1L,
        ELVACO,
        "test",
        new Medium(1L, "test-medium"),
        true,
        Set.of(new DisplayQuantity(TEST_POWER, DisplayMode.READOUT, 2, Units.WATT))
      )
    );
  }
}
