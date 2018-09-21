package com.elvaco.mvp.web;

import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Medium;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.spi.repository.Gateways;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.core.spi.repository.PhysicalMeters;
import com.elvaco.mvp.database.repository.jpa.GatewayJpaRepository;
import com.elvaco.mvp.database.repository.jpa.LogicalMeterJpaRepository;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterJpaRepository;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.web.dto.IdNamedDto;
import com.elvaco.mvp.web.dto.geoservice.AddressDto;
import com.elvaco.mvp.web.dto.geoservice.CityDto;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class SelectionControllerTest extends IntegrationTest {

  @Autowired
  private GatewayJpaRepository gatewayJpaRepository;

  @Autowired
  private LogicalMeterJpaRepository logicalMeterJpaRepository;

  @Autowired
  private PhysicalMeterJpaRepository physicalMeterJpaRepository;

  @Autowired
  private LogicalMeters logicalMeters;

  @Autowired
  private PhysicalMeters physicalMeters;

  @Autowired
  private Gateways gateways;

  @After
  public void tearDown() {
    physicalMeterJpaRepository.deleteAll();
    logicalMeterJpaRepository.deleteAll();
    gatewayJpaRepository.deleteAll();
  }

  @Test
  public void getCities() {
    prepareMeters();

    Page<CityDto> response = asTestUser()
      .getPage("/selections/cities", CityDto.class);

    assertThat(response.getTotalElements()).isEqualTo(3);
    assertThat(response.getTotalPages()).isEqualTo(1);
    assertThat(response.getContent()).containsExactlyInAnyOrder(
      new CityDto("kungsbacka", "sweden"),
      new CityDto("gothenburg", "sweden"),
      new CityDto("helsinki", "finland")
    );
  }

  @Test
  public void getCities_SortedByCityAsc() {
    prepareMeters();

    Page<CityDto> response = asTestUser()
      .getPage("/selections/cities?sort=city", CityDto.class);

    assertThat(response.getTotalElements()).isEqualTo(3);
    assertThat(response.getTotalPages()).isEqualTo(1);
    assertThat(response.getContent()).containsExactly(
      new CityDto("gothenburg", "sweden"),
      new CityDto("helsinki", "finland"),
      new CityDto("kungsbacka", "sweden")
    );
  }

  @Test
  public void getCities_SortedByCityDesc() {
    prepareMeters();

    Page<CityDto> response = asTestUser()
      .getPage("/selections/cities?sort=city,desc", CityDto.class);

    assertThat(response.getTotalElements()).isEqualTo(3);
    assertThat(response.getTotalPages()).isEqualTo(1);
    assertThat(response.getContent()).containsExactly(
      new CityDto("kungsbacka", "sweden"),
      new CityDto("helsinki", "finland"),
      new CityDto("gothenburg", "sweden")
    );
  }

  @Test
  public void getAddresses_SortAsc() {
    prepareMeters();

    Page<AddressDto> response = asTestUser()
      .getPage("/selections/addresses?sort=streetAddress,asc", AddressDto.class);

    assertThat(response.getTotalElements()).isEqualTo(4);
    assertThat(response.getTotalPages()).isEqualTo(1);
    assertThat(response.getContent()).containsExactly(
      new AddressDto("finland", "helsinki", "joksigatan 2"),
      new AddressDto("sweden", "kungsbacka", "kabelgatan 1"),
      new AddressDto("sweden", "kungsbacka", "kabelgatan 2"),
      new AddressDto("sweden", "gothenburg", "snabelgatan 3")
    );
  }

  @Test
  public void getAddresses_SortDesc() {
    prepareMeters();

    Page<AddressDto> response = asTestUser()
      .getPage("/selections/addresses?sort=streetAddress,desc", AddressDto.class);

    assertThat(response.getTotalElements()).isEqualTo(4);
    assertThat(response.getTotalPages()).isEqualTo(1);
    assertThat(response.getContent()).containsExactly(
      new AddressDto("sweden", "gothenburg", "snabelgatan 3"),
      new AddressDto("sweden", "kungsbacka", "kabelgatan 2"),
      new AddressDto("sweden", "kungsbacka", "kabelgatan 1"),
      new AddressDto("finland", "helsinki", "joksigatan 2")
    );
  }

  @Test
  public void getAddresses_IgnoresNull() {
    prepareMeters();
    createLogicalMeter(null, "kungsbacka", "kabelgatan 17", "extId17");

    Page<AddressDto> response = asTestUser()
      .getPage("/selections/addresses?sort=streetAddress,asc", AddressDto.class);

    assertThat(response.getTotalElements()).isEqualTo(4);
    assertThat(response.getTotalPages()).isEqualTo(1);
    assertThat(response.getContent()).containsExactly(
      new AddressDto("finland", "helsinki", "joksigatan 2"),
      new AddressDto("sweden", "kungsbacka", "kabelgatan 1"),
      new AddressDto("sweden", "kungsbacka", "kabelgatan 2"),
      new AddressDto("sweden", "gothenburg", "snabelgatan 3")
    );
  }

  @Test
  public void userCanNotAccessOtherOrganisationsCities() {
    createLogicalMeter(
      "sweden",
      "kungsbacka",
      "kabelgatan 1",
      "extId1",
      context().organisationId()
    );
    createLogicalMeter(
      "sweden",
      "kungsbacka",
      "kabelgatan 2",
      "extId2",
      context().organisationId()
    );
    createLogicalMeter(
      "sweden",
      "kungsbacka",
      "kabelgatan 3",
      "extId3",
      context().organisationId()
    );
    createLogicalMeter(
      "sweden",
      "stockholm",
      "kungsgatan 3",
      "extId4",
      context().organisationId2()
    );

    Page<CityDto> response = asOtherUser().getPage("/selections/cities", CityDto.class);

    assertThat(response.getTotalElements()).isEqualTo(1);
    assertThat(response.getTotalPages()).isEqualTo(1);
    assertThat(response.getContent()).containsExactly(new CityDto("stockholm", "sweden"));
  }

  @Test
  public void getFacilities_SortedAsc() {
    prepareMeters();

    Page<IdNamedDto> response = asTestUser()
      .getPage("/selections/facilities?sort=externalId,asc", IdNamedDto.class);

    assertThat(response.getTotalElements()).isEqualTo(4);
    assertThat(response.getTotalPages()).isEqualTo(1);
    assertThat(response.getContent()).containsExactly(
      new IdNamedDto("extId1"),
      new IdNamedDto("extId2"),
      new IdNamedDto("extId3"),
      new IdNamedDto("extId4")
    );
  }

  @Test
  public void getFacilities_SortedDesc() {
    prepareMeters();

    Page<IdNamedDto> response = asTestUser()
      .getPage("/selections/facilities?sort=externalId,desc", IdNamedDto.class);

    assertThat(response.getTotalElements()).isEqualTo(4);
    assertThat(response.getTotalPages()).isEqualTo(1);
    assertThat(response.getContent()).containsExactly(
      new IdNamedDto("extId4"),
      new IdNamedDto("extId3"),
      new IdNamedDto("extId2"),
      new IdNamedDto("extId1")
    );
  }

  @Test
  public void getFacilities_FilteredOnFacilitySearchText() {
    prepareMeters();

    Page<IdNamedDto> response = asTestUser()
      .getPage("/selections/facilities?sort=externalId,asc&facility=3", IdNamedDto.class);

    assertThat(response.getTotalElements()).isEqualTo(1);
    assertThat(response.getTotalPages()).isEqualTo(1);
    assertThat(response.getContent()).containsExactly(new IdNamedDto("extId3"));
  }

  @Test
  public void userCanNotAccessOtherOrganisationsFacilities() {
    prepareMeters();

    LogicalMeter logicalMeter = createLogicalMeter(
      "sweden",
      "kungsbacka",
      "kabelgatan 2",
      "extId5",
      context().organisationId2()
    );
    createPhysicalMeter(context().organisation2(), "", logicalMeter);

    logicalMeter = createLogicalMeter(
      "sweden",
      "gothenburg",
      "snabelgatan 3",
      "extId6",
      context().organisationId2()
    );
    createPhysicalMeter(context().organisation2(), "", logicalMeter);

    String url = "/selections/facilities?sort=externalId,asc&facility=extId6";

    Page<IdNamedDto> response = asTestUser().getPage(url, IdNamedDto.class);

    assertThat(response.getTotalElements()).isEqualTo(0);
    assertThat(response.getTotalPages()).isEqualTo(0);
    assertThat(response.getContent()).isEmpty();

    response = asOtherUser().getPage(url, IdNamedDto.class);

    assertThat(response.getTotalElements()).isEqualTo(1);
    assertThat(response.getTotalPages()).isEqualTo(1);
    assertThat(response.getContent()).containsExactly(new IdNamedDto("extId6"));
  }

  @Test
  public void getSecondaryAddresses_SortedDesc() {
    prepareMeters();

    Page<IdNamedDto> response = asTestUser()
      .getPage("/selections/secondary-addresses?sort=address,desc", IdNamedDto.class);

    assertThat(response.getTotalElements()).isEqualTo(4);
    assertThat(response.getTotalPages()).isEqualTo(1);
    assertThat(response.getContent()).containsExactly(
      new IdNamedDto("444"),
      new IdNamedDto("333"),
      new IdNamedDto("222"),
      new IdNamedDto("111")
    );
  }

  @Test
  public void getSecondaryAddressFiltered() {
    prepareMeters();

    Page<IdNamedDto> response = asTestUser()
      .getPage("/selections/secondary-addresses?secondaryAddress=444", IdNamedDto.class);

    assertThat(response.getTotalElements()).isEqualTo(1);
    assertThat(response.getTotalPages()).isEqualTo(1);
    assertThat(response.getContent()).containsExactly(new IdNamedDto("444"));
  }

  @Test
  public void userCanNotAccessOtherOrganisationsSecondaryAddresses() {
    prepareMeters();

    LogicalMeter logicalMeter = logicalMeters.save(createLogicalMeter(
      "finland",
      "helsinki",
      "joksigatan 2",
      "extId777",
      context().organisationId2()
    ));
    physicalMeters.save(createPhysicalMeter(context().organisation2(), "777", logicalMeter));

    String url = "/selections/secondary-addresses?secondaryAddress=777";

    Page<IdNamedDto> response = asTestUser().getPage(url, IdNamedDto.class);

    assertThat(response.getTotalElements()).isEqualTo(0);
    assertThat(response.getTotalPages()).isEqualTo(0);
    assertThat(response.getContent()).isEmpty();

    response = asOtherUser().getPage(url, IdNamedDto.class);

    assertThat(response.getTotalElements()).isEqualTo(1);
    assertThat(response.getTotalPages()).isEqualTo(1);
    assertThat(response.getContent()).containsExactly(new IdNamedDto("777"));
  }

  @Test
  public void getGatewaySerials_SortedDesc() {
    prepareGateways();

    Page<IdNamedDto> response = asTestUser()
      .getPage("/selections/gateway-serials?sort=serial,desc", IdNamedDto.class);

    assertThat(response.getTotalElements()).isEqualTo(3);
    assertThat(response.getTotalPages()).isEqualTo(1);
    assertThat(response.getContent()).containsExactly(
      new IdNamedDto("5566"),
      new IdNamedDto("3344"),
      new IdNamedDto("1122")
    );
  }

  @Test
  public void getGatewaySerials_FilteredOnQueryString() {
    prepareGateways();

    Page<IdNamedDto> response = asTestUser()
      .getPage("/selections/gateway-serials?serial=66", IdNamedDto.class);

    assertThat(response.getTotalElements()).isEqualTo(1);
    assertThat(response.getTotalPages()).isEqualTo(1);
    assertThat(response.getContent()).containsExactly(new IdNamedDto("5566"));
  }

  @Test
  public void userCanNotAccessOtherOrganisationsGatewaySerials() {
    prepareGateways();
    gateways.save(Gateway.builder()
      .organisationId(context().organisationId2())
      .serial("6666")
      .productModel("3100")
      .build());

    Page<IdNamedDto> response = asOtherUser()
      .getPage("/selections/gateway-serials", IdNamedDto.class);

    assertThat(response.getTotalElements()).isEqualTo(1);
    assertThat(response.getTotalPages()).isEqualTo(1);
    assertThat(response.getContent()).containsExactly(new IdNamedDto("6666"));
  }

  @Test
  public void facilityWildcardSearch() {
    UUID meterId = randomUUID();
    logicalMeters.save(LogicalMeter.builder()
      .id(meterId)
      .externalId("abcdef")
      .organisationId(context().organisationId())
      .meterDefinition(MeterDefinition.HOT_WATER_METER)
      .build());

    physicalMeters.save(PhysicalMeter.builder()
      .logicalMeterId(meterId)
      .externalId("abcdef")
      .readIntervalMinutes(60)
      .medium(Medium.DISTRICT_HEATING.medium)
      .organisation(context().organisation())
      .address("123456")
      .build());

    Page<IdNamedDto> response = asTestUser().getPage(
      "/selections/facilities?q=bcd",
      IdNamedDto.class
    );

    assertThat(response).hasSize(1);
    assertThat(response.getContent().get(0).name).isEqualTo("abcdef");
    assertThat(
      asTestUser().getPage(
        "/selections/facilities?q=qwerty",
        IdNamedDto.class
      )
    ).hasSize(0);
  }

  @Test
  public void secondaryAddressWildcardSearch() {
    UUID meterId = randomUUID();
    logicalMeters.save(
      LogicalMeter.builder()
        .id(meterId)
        .externalId("1234")
        .organisationId(context().organisationId())
        .meterDefinition(MeterDefinition.HOT_WATER_METER)
        .location(
          new LocationBuilder().city("Kungsbacka").address("Stora vägen 24").build()
        )
        .build());

    physicalMeters.save(PhysicalMeter.builder()
      .logicalMeterId(meterId)
      .externalId("1234")
      .readIntervalMinutes(60)
      .medium(Medium.DISTRICT_HEATING.medium)
      .organisation(context().organisation())
      .address("123456")
      .build());

    Page<IdNamedDto> response = asTestUser().getPage(
      "/selections/secondary-addresses?q=2345",
      IdNamedDto.class
    );

    assertThat(response).hasSize(1);
    assertThat(response.getContent().get(0).name).isEqualTo("123456");
    assertThat(
      asTestUser().getPage(
        "/selections/secondary-addresses?q=000000",
        IdNamedDto.class
      )
    ).hasSize(0);
  }

  @Test
  public void gatewaySerialWildcardSearch() {
    gateways.save(Gateway.builder()
      .organisationId(context().organisationId())
      .serial("1234567")
      .productModel("3100")
      .build());

    Page<IdNamedDto> response = asTestUser().getPage(
      "/selections/gateway-serials?q=3456",
      IdNamedDto.class
    );

    assertThat(response).hasSize(1);
    assertThat(response.getContent().get(0).name).isEqualTo("1234567");
    assertThat(asTestUser().getPage(
      "/selections/gateway-serials?q=90909090",
      IdNamedDto.class
    )).hasSize(0);
  }

  @Test
  public void cityWildcardSearch() {
    UUID meterId = randomUUID();
    logicalMeters.save(LogicalMeter.builder()
      .id(meterId)
      .externalId("1234")
      .organisationId(context().organisationId())
      .meterDefinition(MeterDefinition.HOT_WATER_METER)
      .location(new LocationBuilder().country("sverige")
        .city("Kungsbacka")
        .address("Stora vägen 24")
        .build())
      .build());

    physicalMeters.save(PhysicalMeter.builder()
      .logicalMeterId(meterId)
      .externalId("1234")
      .readIntervalMinutes(60)
      .medium(Medium.DISTRICT_HEATING.medium)
      .organisation(context().organisation())
      .address("123456")
      .build());

    Page<CityDto> response = asTestUser().getPage(
      "/selections/cities?q=ngsback",
      CityDto.class
    );

    assertThat(response).hasSize(1);
    assertThat(response.getContent().get(0).name).isEqualTo("kungsbacka");
    assertThat(asTestUser().getPage(
      "/selections/cities?q=tockholm",
      IdNamedDto.class
    )).hasSize(0);
  }

  @Test
  public void addressWildcardSearch() {
    UUID meterId = randomUUID();
    logicalMeters.save(LogicalMeter.builder()
      .id(meterId)
      .externalId("1234")
      .organisationId(context().organisationId())
      .meterDefinition(MeterDefinition.HOT_WATER_METER)
      .location(new LocationBuilder().country("sverige")
        .city("Kungsbacka")
        .address("Stora vägen 24")
        .build()
      )
      .build());

    physicalMeters.save(PhysicalMeter.builder()
      .logicalMeterId(meterId)
      .externalId("1234")
      .readIntervalMinutes(60)
      .medium(Medium.DISTRICT_HEATING.medium)
      .organisation(context().organisation())
      .address("123456")
      .build());

    Page<AddressDto> response = asTestUser().getPage(
      "/selections/addresses?q=tora",
      AddressDto.class
    );

    assertThat(response).hasSize(1);
    assertThat(response.getContent().get(0).street).isEqualTo("stora vägen 24");
    assertThat(asTestUser().getPage(
      "/selections/addresses?q=illa",
      AddressDto.class
    )).hasSize(0);
  }

  @Test
  public void getCities_duplicatesAreNotIncluded() {
    logicalMeters.save(LogicalMeter.builder()
      .id(randomUUID())
      .externalId("1")
      .organisationId(context().organisationId())
      .meterDefinition(MeterDefinition.HOT_WATER_METER)
      .location(new LocationBuilder().country("sverige").city("kungsbacka").build())
      .build()
    );

    logicalMeters.save(LogicalMeter.builder()
      .id(randomUUID())
      .externalId("2")
      .organisationId(context().organisationId())
      .meterDefinition(MeterDefinition.HOT_WATER_METER)
      .location(new LocationBuilder().country("sverige").city("kungsbacka").build())
      .build()
    );

    logicalMeters.save(LogicalMeter.builder()
      .id(randomUUID())
      .externalId("3")
      .organisationId(context().organisationId())
      .meterDefinition(MeterDefinition.HOT_WATER_METER)
      .location(new LocationBuilder().country("norge").city("kungsbacka").build())
      .build()
    );

    Page<CityDto> response = asTestUser().getPage(
      "/selections/cities",
      CityDto.class
    );

    assertThat(response.getContent()).containsExactlyInAnyOrder(
      new CityDto("kungsbacka", "sverige"),
      new CityDto("kungsbacka", "norge")
    );
  }

  @Test
  public void getAddresses_duplicatesAreNotIncluded() {
    logicalMeters.save(LogicalMeter.builder()
      .id(randomUUID())
      .externalId("1")
      .organisationId(context().organisationId())
      .meterDefinition(MeterDefinition.HOT_WATER_METER)
      .location(new LocationBuilder().country("sverige")
        .city("kungsbacka")
        .address("teknikgatan 2")
        .build())
      .build()
    );

    logicalMeters.save(LogicalMeter.builder()
      .id(randomUUID())
      .externalId("2")
      .organisationId(context().organisationId())
      .meterDefinition(MeterDefinition.HOT_WATER_METER)
      .location(new LocationBuilder().country("sverige")
        .city("kungsbacka")
        .address("teknikgatan 2")
        .build())
      .build()
    );

    Page<AddressDto> response = asTestUser().getPage(
      "/selections/addresses",
      AddressDto.class
    );

    assertThat(response.getContent()).containsExactly(
      new AddressDto("sverige", "kungsbacka", "teknikgatan 2")
    );
  }

  @Test
  public void getSecondaryAddresses_duplicatesAreNotIncluded() {
    UUID meterId = randomUUID();
    UUID meterIdTwo = randomUUID();
    logicalMeters.save(
      LogicalMeter.builder()
        .id(meterId)
        .externalId("1234")
        .organisationId(context().organisationId())
        .meterDefinition(MeterDefinition.HOT_WATER_METER)
        .build());

    physicalMeters.save(PhysicalMeter.builder()
      .logicalMeterId(meterId)
      .externalId("1234")
      .readIntervalMinutes(60)
      .medium(Medium.DISTRICT_HEATING.medium)
      .organisation(context().organisation())
      .address("123456")
      .build());

    logicalMeters.save(LogicalMeter.builder()
      .id(meterIdTwo)
      .externalId("5678")
      .organisationId(context().organisationId())
      .meterDefinition(MeterDefinition.HOT_WATER_METER).build()
    );

    physicalMeters.save(PhysicalMeter.builder()
      .logicalMeterId(meterIdTwo)
      .externalId("5678")
      .readIntervalMinutes(60)
      .medium(Medium.DISTRICT_HEATING.medium)
      .organisation(context().organisation())
      .address("123456")
      .build());

    Page<IdNamedDto> response = asTestUser().getPage(
      "/selections/secondary-addresses",
      IdNamedDto.class
    );

    assertThat(response).extracting("name").containsExactly("123456");
  }

  @Test
  public void getFacilities_duplicatesAreNotIncluded() {
    UUID meterId = randomUUID();
    logicalMeters.save(
      LogicalMeter.builder()
        .id(meterId)
        .externalId("1234")
        .organisationId(context().organisationId())
        .meterDefinition(MeterDefinition.HOT_WATER_METER)
        .build());

    physicalMeters.save(PhysicalMeter.builder()
      .logicalMeterId(meterId)
      .externalId("1234")
      .readIntervalMinutes(60)
      .medium(Medium.DISTRICT_HEATING.medium)
      .organisation(context().organisation())
      .address("123456")
      .build());

    physicalMeters.save(PhysicalMeter.builder()
      .logicalMeterId(meterId)
      .externalId("1234")
      .readIntervalMinutes(60)
      .medium(Medium.DISTRICT_HEATING.medium)
      .organisation(context().organisation())
      .address("78910")
      .build());

    Page<IdNamedDto> response = asTestUser().getPage(
      "/selections/facilities",
      IdNamedDto.class
    );

    assertThat(response).extracting("name").containsExactly("1234");
  }

  @Test
  public void getGatewaySerials_duplicatesAreNotIncluded() {
    Gateway.GatewayBuilder gateway = Gateway.builder().organisationId(context().organisationId());
    gateways.save(gateway.serial("1").productModel("3100").build());
    gateways.save(gateway.serial("1").productModel("2100").build());
    gateways.save(gateway.serial("2").productModel("3100").build());

    Page<IdNamedDto> response = asTestUser()
      .getPage("/selections/gateway-serials", IdNamedDto.class);

    assertThat(response).extracting("name").containsExactlyInAnyOrder("1", "2");
  }

  private void prepareGateways() {
    Gateway.GatewayBuilder gatewayBuilder = Gateway.builder()
      .organisationId(context().organisationId())
      .productModel("3100");
    gateways.save(gatewayBuilder.serial("1122").build());
    gateways.save(gatewayBuilder.serial("3344").build());
    gateways.save(gatewayBuilder.serial("5566").build());
  }

  private void prepareMeters() {
    LogicalMeter logicalMeter = createLogicalMeter(
      "sweden",
      "kungsbacka",
      "kabelgatan 1",
      "extId1"
    );
    createPhysicalMeter(context().organisation(), "111", logicalMeter);

    logicalMeter = createLogicalMeter("sweden", "kungsbacka", "kabelgatan 2", "extId2");
    createPhysicalMeter(context().organisation(), "222", logicalMeter);

    logicalMeter = createLogicalMeter("sweden", "gothenburg", "snabelgatan 3", "extId3");
    createPhysicalMeter(context().organisation(), "333", logicalMeter);

    logicalMeter = createLogicalMeter("finland", "helsinki", "joksigatan 2", "extId4");
    createPhysicalMeter(context().organisation(), "444", logicalMeter);
  }

  private PhysicalMeter createPhysicalMeter(
    Organisation organisation,
    String address,
    LogicalMeter logicalMeter
  ) {
    return physicalMeters.save(PhysicalMeter.builder()
      .organisation(organisation)
      .address(address)
      .externalId(logicalMeter.externalId)
      .medium("Gas")
      .manufacturer("elv")
      .logicalMeterId(logicalMeter.id)
      .readIntervalMinutes(60)
      .build());
  }

  private LogicalMeter createLogicalMeter(
    String country,
    String city,
    String address,
    String externalId
  ) {
    return createLogicalMeter(country, city, address, externalId, context().organisationId());
  }

  private LogicalMeter createLogicalMeter(
    String country,
    String city,
    String address,
    String externalId,
    UUID organisationId
  ) {
    return logicalMeters.save(LogicalMeter.builder()
      .externalId(externalId)
      .organisationId(organisationId)
      .location(new LocationBuilder()
        .country(country)
        .city(city)
        .address(address)
        .build())
      .build()
    );
  }
}
