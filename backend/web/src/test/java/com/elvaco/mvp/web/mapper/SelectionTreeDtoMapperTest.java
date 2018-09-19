package com.elvaco.mvp.web.mapper;

import java.util.UUID;
import java.util.stream.Stream;

import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Medium;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.SelectionTree;
import com.elvaco.mvp.web.dto.SelectionTreeDto;
import com.elvaco.mvp.web.dto.SelectionTreeDto.AddressDto;
import com.elvaco.mvp.web.dto.SelectionTreeDto.CityDto;
import com.elvaco.mvp.web.dto.SelectionTreeDto.MeterDto;

import org.junit.Before;
import org.junit.Test;

import static java.util.Collections.singleton;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Java6Assertions.assertThat;

public class SelectionTreeDtoMapperTest {

  private SelectionTree selectionTree;

  @Before
  public void setUp() {
    selectionTree = new SelectionTree();
  }

  @Test
  public void addsCityToDto() {
    LogicalMeter logicalMeter = newLogicalMeter(
      "sweden",
      "kungsbacka",
      "kabelgatan 1",
      "12345"
    );

    selectionTree.add(logicalMeter);

    assertThat(selectionTree.getCity("sweden,kungsbacka").name).isEqualTo("kungsbacka");
  }

  @Test
  public void addsUniqueCityAddressAndMeterOnlyOnce() {
    LogicalMeter logicalMeter = newLogicalMeter(
      "sweden",
      "kungsbacka",
      "kabelgatan 1",
      "12345"
    );

    Stream.of(logicalMeter, logicalMeter).forEach((lm) -> selectionTree.add(lm));

    assertThat(selectionTree.getCities().size()).isEqualTo(1);
    assertThat(selectionTree.getCity("sweden,kungsbacka").getAddresses().size()).isEqualTo(1);
    assertThat(
      selectionTree
        .getCity("sweden,kungsbacka")
        .getAddress("kabelgatan 1")
        .getMeters().size())
      .isEqualTo(1);
  }

  @Test
  public void branchesCitiesFromDifferentCountries() {
    Stream.of(
      newLogicalMeter("sweden", "kungsbacka", "kabelgatan 1", "1245"),
      newLogicalMeter("finland", "kungsbacka", "kabelgatan 1", "1245")
    )
      .forEach((lm) -> selectionTree.add(lm));

    assertThat(selectionTree.getCities().size()).isEqualTo(2);
    assertThat(selectionTree.getCity("sweden,kungsbacka").getAddresses().size()).isEqualTo(1);
    assertThat(selectionTree.getCity("finland,kungsbacka").getAddresses().size()).isEqualTo(1);
  }

  @Test
  public void branchesAddressesFromDifferentCities() {
    Stream.of(
      newLogicalMeter("sweden", "kungsbacka", "kabelgatan 1", "1245"),
      newLogicalMeter("sweden", "gothenburg", "kabelgatan 1", "1245")
    )
      .forEach((lm) -> selectionTree.add(lm));

    assertThat(selectionTree.getCity("sweden,kungsbacka").getAddresses().size()).isEqualTo(1);
    assertThat(selectionTree.getCity("sweden,gothenburg").getAddresses().size()).isEqualTo(1);
  }

  @Test
  public void branchesMetersFromDifferentAddresses() {
    UUID logicalMeterId = randomUUID();

    Stream.of(
      newLogicalMeter("sweden", "kungsbacka", "kabelgatan 1", logicalMeterId, "1245"),
      newLogicalMeter("sweden", "kungsbacka", "kabelgatan 2", logicalMeterId, "1245")
    )
      .forEach((lm) -> selectionTree.add(lm));

    assertThat(selectionTree.getCity("sweden,kungsbacka")
      .getAddress("kabelgatan 1")
      .getMeters()
      .size()).isEqualTo(1);
    assertThat(selectionTree.getCity("sweden,kungsbacka")
      .getAddress("kabelgatan 2")
      .getMeters()
      .size()).isEqualTo(1);
  }

  @Test
  public void branchesMetersWithDifferentIds() {
    UUID logicalMeterId1 = randomUUID();
    UUID logicalMeterId2 = randomUUID();

    Stream.of(
      newLogicalMeter("sweden", "kungsbacka", "kabelgatan 1", logicalMeterId1, "1245"),
      newLogicalMeter("sweden", "kungsbacka", "kabelgatan 1", logicalMeterId2, "1245")
    )
      .forEach((lm) -> selectionTree.add(lm));

    assertThat(selectionTree.getCity("sweden,kungsbacka")
      .getAddress("kabelgatan 1")
      .getMeters()
      .size()).isEqualTo(2);
  }

  @Test
  public void logicalMeterWithNullLocationDefaultsToUnknownLocation() {
    LogicalMeter logicalMeter = newLogicalMeter(null, null, null, "1245");

    selectionTree.add(logicalMeter);
    assertThat(
      selectionTree
        .getCity("unknown,unknown")
        .getAddress("unknown")
        .getMeters().size())
      .isEqualTo(1);
  }

  @Test
  public void toDtoCities() {
    Stream.of(
      newLogicalMeter("sweden", "kungsbacka", "kabelgatan 1", "1245"),
      newLogicalMeter("sweden", "gothenburg", "kabelgatan 1", "1234")
    ).forEach((lm) -> selectionTree.add(lm));

    SelectionTreeDto selectionTreeDto = SelectionTreeDtoMapper.toDto(selectionTree);

    assertThat(selectionTreeDto.cities).containsOnly(
      new CityDto(
        "sweden,gothenburg",
        "gothenburg",
        singleton(Medium.UNKNOWN_MEDIUM.medium)
      ),
      new CityDto(
        "sweden,kungsbacka",
        "kungsbacka",
        singleton(Medium.UNKNOWN_MEDIUM.medium)
      )
    );
  }

  @Test
  public void toDtoAddresses() {
    Stream.of(
      newLogicalMeter("sweden", "kungsbacka", "kabelgatan 1", "1234"),
      newLogicalMeter("sweden", "kungsbacka", "kabelgatan 2", "1234")
    ).forEach((lm) -> selectionTree.add(lm));

    SelectionTreeDto selectionTreeDto = SelectionTreeDtoMapper.toDto(selectionTree);

    assertThat(selectionTreeDto.cities.get(0).addresses).containsOnly(
      new AddressDto("kabelgatan 1"),
      new AddressDto("kabelgatan 2")
    );
  }

  @Test
  public void toDtoMeters() {
    UUID logicalMeterId1 = randomUUID();
    UUID logicalMeterId2 = randomUUID();

    Stream.of(
      newLogicalMeter("sweden", "kungsbacka", "kabelgatan 1", logicalMeterId1, "1245"),
      newLogicalMeter("sweden", "kungsbacka", "kabelgatan 1", logicalMeterId2, "1234")
    ).forEach((lm) -> selectionTree.add(lm));

    SelectionTreeDto selectionTreeDto = SelectionTreeDtoMapper.toDto(selectionTree);

    assertThat(selectionTreeDto.cities.get(0).addresses.get(0).meters).containsOnly(
      new MeterDto(logicalMeterId1, "1245", MeterDefinition.UNKNOWN_METER.medium),
      new MeterDto(logicalMeterId2, "1234", MeterDefinition.UNKNOWN_METER.medium)
    );
  }

  private static LogicalMeter newLogicalMeter(
    String country,
    String city,
    String address,
    String extId
  ) {
    return newLogicalMeter(
      country,
      city,
      address,
      randomUUID(),
      extId
    );
  }

  private static LogicalMeter newLogicalMeter(
    String country,
    String city,
    String address,
    UUID id,
    String externalId
  ) {
    return LogicalMeter.builder()
      .id(id)
      .externalId(externalId)
      .organisationId(randomUUID())
      .location(new LocationBuilder()
        .country(country)
        .city(city)
        .address(address)
        .build())
      .build();
  }
}
