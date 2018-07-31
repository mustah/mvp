package com.elvaco.mvp.web;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.Location;
import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.spi.repository.Gateways;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.core.spi.repository.PhysicalMeters;
import com.elvaco.mvp.database.entity.user.OrganisationEntity;
import com.elvaco.mvp.database.repository.jpa.GatewayJpaRepository;
import com.elvaco.mvp.database.repository.jpa.LogicalMeterJpaRepository;
import com.elvaco.mvp.database.repository.jpa.OrganisationJpaRepository;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterJpaRepository;
import com.elvaco.mvp.database.repository.mappers.OrganisationEntityMapper;
import com.elvaco.mvp.testdata.IntegrationTest;
import com.elvaco.mvp.testdata.RestClient;
import com.elvaco.mvp.web.dto.IdNamedDto;
import com.elvaco.mvp.web.dto.LocationDto;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class SelectionControllerTest extends IntegrationTest {

  @Autowired
  private GatewayJpaRepository gatewayJpaRepository;

  @Autowired
  private LogicalMeterJpaRepository logicalMeterJpaRepository;

  @Autowired
  private OrganisationJpaRepository organisationJpaRepository;

  @Autowired
  private PhysicalMeterJpaRepository physicalMeterJpaRepository;

  @Autowired
  private LogicalMeters logicalMeters;

  @Autowired
  private PhysicalMeters physicalMeters;

  @Autowired
  private Gateways gateways;

  private OrganisationEntity anotherOrganisation = null;

  @After
  public void tearDown() {
    physicalMeterJpaRepository.deleteAll();
    logicalMeterJpaRepository.deleteAll();
    gatewayJpaRepository.deleteAll();

    if (anotherOrganisation != null) {
      organisationJpaRepository.delete(anotherOrganisation);
      anotherOrganisation = null;
    }
  }

  @Test
  public void getLocation() {
    createLogicalMeter("sweden", "kungsbacka", "kabelgatan 1", "extId1");
    createLogicalMeter("sweden", "kungsbacka", "kabelgatan 2", "extId2");
    createLogicalMeter("sweden", "gothenburg", "kabelgatan 3", "extId3");
    createLogicalMeter("finland", "helsinki", "joksigatan 2", "extId4");

    Page<LocationDto> response = asTestUser()
      .getPage("/selections/locations", LocationDto.class);

    assertThat(response.getTotalElements()).isEqualTo(4);
    assertThat(response.getTotalPages()).isEqualTo(1);
  }

  @Test
  public void getLocationFilteredOnCity() {
    createLogicalMeter("sweden", "kungsbacka", "kabelgatan 1", "extId1");
    createLogicalMeter("sweden", "kungsbacka", "kabelgatan 2", "extId2");
    createLogicalMeter("sweden", "gothenburg", "kabelgatan 3", "extId3");
    createLogicalMeter("finland", "helsinki", "joksigatan 2", "extId4");

    Page<LocationDto> response = asTestUser()
      .getPage("/selections/locations?city=Kungsbacka", LocationDto.class);

    assertThat(response.getTotalElements()).isEqualTo(2);
    assertThat(response.getTotalPages()).isEqualTo(1);
    assertThat(response.getContent().get(0).city.name).isEqualTo("kungsbacka");
    assertThat(response.getContent().get(1).city.name).isEqualTo("kungsbacka");
  }

  @Test
  public void getLocationFilteredOnAddress() {
    createLogicalMeter("sweden", "kungsbacka", "kabelgatan 1", "extId1");
    createLogicalMeter("sweden", "kungsbacka", "kabelgatan 2", "extId2");
    createLogicalMeter("sweden", "gothenburg", "snabelgatan 3", "extId3");
    createLogicalMeter("finland", "helsinki", "joksigatan 2", "extId4");

    Page<LocationDto> response = asTestUser()
      .getPage("/selections/locations?address=abel&sort=address", LocationDto.class);

    List<String> addresses = asList("kabelgatan 1", "kabelgatan 2", "snabelgatan 3");

    assertThat(response.getTotalElements()).isEqualTo(3);
    assertThat(response.getTotalPages()).isEqualTo(1);
    assertThat(response.getContent().get(0).address.name).isIn(addresses);
    assertThat(response.getContent().get(1).address.name).isIn(addresses);
    assertThat(response.getContent().get(2).address.name).isIn(addresses);
  }

  @Test
  public void userCanNotAccessOtherOrganisationsLocations() {
    RestClient userClient = restAsUser(context().user);

    anotherOrganisation = organisationJpaRepository.save(
      new OrganisationEntity(
        randomUUID(),
        "Another Organisation",
        "another-organisation",
        "another-organisation"
      ));

    createLogicalMeter(
      "sweden",
      "kungsbacka",
      "kabelgatan 1",
      "extId1",
      context().user.organisation.id
    );
    createLogicalMeter(
      "finland",
      "helsinki",
      "joksigatan 2",
      "extId4",
      context().user.organisation.id
    );
    createLogicalMeter(
      "sweden",
      "kungsbacka",
      "kabelgatan 2",
      "extId2",
      anotherOrganisation.id
    );
    createLogicalMeter(
      "sweden",
      "gothenburg",
      "snabelgatan 3",
      "extId3",
      anotherOrganisation.id
    );

    Page<LocationDto> response = userClient.getPage(
      "/selections/locations?address=abel",
      LocationDto.class
    );

    assertThat(response.getTotalElements()).isEqualTo(1);
    assertThat(response.getTotalPages()).isEqualTo(1);
    assertThat(response.getContent().get(0).address.name).isEqualTo("kabelgatan 1");
  }

  @Test
  public void getFacility() {
    LogicalMeter logicalMeter = createLogicalMeter(
      "sweden",
      "kungsbacka",
      "kabelgatan 1",
      "extId1"
    );
    createPhysicalMeter(context().organisation(), "", logicalMeter);

    logicalMeter = createLogicalMeter("sweden", "kungsbacka", "kabelgatan 2", "extId2");
    createPhysicalMeter(context().organisation(), "", logicalMeter);

    logicalMeter = createLogicalMeter("sweden", "gothenburg", "snabelgatan 3", "extId3");
    createPhysicalMeter(context().organisation(), "", logicalMeter);

    logicalMeter = createLogicalMeter("finland", "helsinki", "joksigatan 2", "extId4");
    createPhysicalMeter(context().organisation(), "", logicalMeter);

    RestClient userClient = restAsUser(context().user);

    Page<IdNamedDto> response = userClient.getPage(
      "/selections/facilities",
      IdNamedDto.class
    );

    assertThat(response.getTotalElements()).isEqualTo(4);
    assertThat(response.getTotalPages()).isEqualTo(1);
  }

  @Test
  public void getFacilityFiltered() {
    LogicalMeter logicalMeter = logicalMeters.save(createLogicalMeter(
      "sweden",
      "kungsbacka",
      "kabelgatan 1",
      "extId1"
    ));
    createPhysicalMeter(context().organisation(), "", logicalMeter);

    logicalMeter = logicalMeters.save(createLogicalMeter(
      "sweden",
      "kungsbacka",
      "kabelgatan 2",
      "extId2"
    ));
    createPhysicalMeter(context().organisation(), "", logicalMeter);

    logicalMeter = logicalMeters.save(createLogicalMeter(
      "sweden",
      "gothenburg",
      "snabelgatan 3",
      "extId3"
    ));
    createPhysicalMeter(context().organisation(), "", logicalMeter);

    logicalMeter = logicalMeters.save(createLogicalMeter(
      "finland",
      "helsinki",
      "joksigatan 2",
      "extId4"
    ));
    createPhysicalMeter(context().organisation(), "", logicalMeter);

    RestClient userClient = restAsUser(context().user);

    Page<IdNamedDto> response = userClient.getPage(
      "/selections/facilities?containsFacility=3",
      IdNamedDto.class
    );

    assertThat(response.getTotalElements()).isEqualTo(1);
    assertThat(response.getTotalPages()).isEqualTo(1);
    assertThat(response.getContent().get(0).name).isEqualTo("extId3");
  }

  @Test
  public void userCanNotAccessOtherOrganisationsFacilities() {
    anotherOrganisation = organisationJpaRepository.save(
      new OrganisationEntity(
        randomUUID(),
        "Another Organisation",
        "another-organisation",
        "another-organisation"
      ));

    Organisation anotherOrganisationModel = OrganisationEntityMapper.toDomainModel(
      anotherOrganisation);

    LogicalMeter logicalMeter = createLogicalMeter(
      "sweden",
      "kungsbacka",
      "kabelgatan 1",
      "extId1",
      context().user.organisation.id
    );
    createPhysicalMeter(context().organisation(), "", logicalMeter);

    logicalMeter = createLogicalMeter(
      "finland",
      "helsinki",
      "joksigatan 2",
      "extId4",
      context().user.organisation.id
    );
    createPhysicalMeter(context().organisation(), "", logicalMeter);

    logicalMeter = createLogicalMeter(
      "sweden",
      "kungsbacka",
      "kabelgatan 2",
      "extId2",
      anotherOrganisation.id
    );
    createPhysicalMeter(anotherOrganisationModel, "", logicalMeter);

    logicalMeter = createLogicalMeter(
      "sweden",
      "gothenburg",
      "snabelgatan 3",
      "extId3",
      anotherOrganisation.id
    );
    createPhysicalMeter(anotherOrganisationModel, "", logicalMeter);

    RestClient userClient = restAsUser(context().user);
    Page<LocationDto> response = userClient.getPage(
      "/selections/facilities?containsFacility=extId3",
      LocationDto.class
    );

    assertThat(response.getTotalElements()).isEqualTo(0);
    assertThat(response.getTotalPages()).isEqualTo(0);
  }

  @Test
  public void getSecondaryAddress() {
    LogicalMeter logicalMeter = createLogicalMeter(
      "sweden",
      "kungsbacka",
      "kabelgatan 1",
      "extId1"
    );
    physicalMeters.save(createPhysicalMeter(context().organisation(), "1234", logicalMeter));

    logicalMeter = createLogicalMeter(
      "sweden",
      "kungsbacka",
      "kabelgatan 2",
      "extId2"
    );
    createPhysicalMeter(context().organisation(), "666", logicalMeter);

    logicalMeter = createLogicalMeter(
      "sweden",
      "gothenburg",
      "snabelgatan 3",
      "extId3"
    );
    createPhysicalMeter(context().organisation(), "4321", logicalMeter);

    logicalMeter = createLogicalMeter(
      "finland",
      "helsinki",
      "joksigatan 2",
      "extId4"
    );
    createPhysicalMeter(context().organisation(), "777", logicalMeter);

    RestClient userClient = restAsUser(context().user);

    Page<IdNamedDto> response = userClient.getPage(
      "/selections/secondaryAddresses",
      IdNamedDto.class
    );

    assertThat(response.getTotalElements()).isEqualTo(4);
    assertThat(response.getTotalPages()).isEqualTo(1);
  }

  @Test
  public void getSecondaryAddressFiltered() {
    LogicalMeter logicalMeter = logicalMeters.save(createLogicalMeter(
      "sweden",
      "kungsbacka",
      "kabelgatan 1",
      "extId1"
    ));
    physicalMeters.save(createPhysicalMeter(context().organisation(), "1234", logicalMeter));

    logicalMeter = logicalMeters.save(createLogicalMeter(
      "sweden",
      "kungsbacka",
      "kabelgatan 2",
      "extId2"
    ));
    physicalMeters.save(createPhysicalMeter(context().organisation(), "666", logicalMeter));

    logicalMeter = logicalMeters.save(createLogicalMeter(
      "sweden",
      "gothenburg",
      "snabelgatan 3",
      "extId3"
    ));
    physicalMeters.save(createPhysicalMeter(context().organisation(), "4321", logicalMeter));

    logicalMeter = logicalMeters.save(createLogicalMeter(
      "finland",
      "helsinki",
      "joksigatan 2",
      "extId4"
    ));
    physicalMeters.save(createPhysicalMeter(context().organisation(), "777", logicalMeter));

    RestClient userClient = restAsUser(context().user);

    Page<IdNamedDto> response = userClient.getPage(
      "/selections/secondaryAddresses?containsSecondaryAddress=777",
      IdNamedDto.class
    );

    assertThat(response.getTotalElements()).isEqualTo(1);
    assertThat(response.getTotalPages()).isEqualTo(1);
    assertThat(response.getContent().get(0).name).isEqualTo("777");
  }

  @Test
  public void userCanNotAccessOtherOrganisationsSecondaryAddresses() {
    anotherOrganisation = organisationJpaRepository.save(
      new OrganisationEntity(
        randomUUID(),
        "Another Organisation",
        "another-organisation",
        "another-organisation"
      ));

    Organisation anotherOrganisationModel = OrganisationEntityMapper.toDomainModel(
      anotherOrganisation);

    LogicalMeter logicalMeter = logicalMeters.save(createLogicalMeter(
      "sweden",
      "kungsbacka",
      "kabelgatan 1",
      "extId1"
    ));
    physicalMeters.save(createPhysicalMeter(context().organisation(), "1234", logicalMeter));

    logicalMeter = logicalMeters.save(createLogicalMeter(
      "sweden",
      "kungsbacka",
      "kabelgatan 2",
      "extId2"
    ));
    physicalMeters.save(createPhysicalMeter(context().organisation(), "666", logicalMeter));

    logicalMeter = logicalMeters.save(createLogicalMeter(
      "sweden",
      "gothenburg",
      "snabelgatan 3",
      "extId3",
      anotherOrganisationModel.id
    ));
    physicalMeters.save(createPhysicalMeter(anotherOrganisationModel, "4321", logicalMeter));

    logicalMeter = logicalMeters.save(createLogicalMeter(
      "finland",
      "helsinki",
      "joksigatan 2",
      "extId4",
      anotherOrganisationModel.id
    ));
    physicalMeters.save(createPhysicalMeter(anotherOrganisationModel, "777", logicalMeter));

    RestClient userClient = restAsUser(context().user);
    Page<LocationDto> response = userClient.getPage(
      "/selections/secondaryAddresses?containsSecondaryAddress=777",
      LocationDto.class
    );

    assertThat(response.getTotalElements()).isEqualTo(0);
    assertThat(response.getTotalPages()).isEqualTo(0);
  }

  @Test
  public void getGatewaySerials() {
    createGateway(context().user.organisation.id, "0016000458");
    createGateway(context().user.organisation.id, "0016000666");
    createGateway(context().user.organisation.id, "0016002100");

    RestClient userClient = restAsUser(context().user);

    Page<IdNamedDto> response = userClient.getPage(
      "/selections/gateway/serials",
      IdNamedDto.class
    );

    assertThat(response.getTotalElements()).isEqualTo(3);
    assertThat(response.getTotalPages()).isEqualTo(1);
  }

  @Test
  public void getGatewaySerialsFiltered() {
    createGateway(context().user.organisation.id, "0016000458");
    createGateway(context().user.organisation.id, "0016000466");
    createGateway(context().user.organisation.id, "0016002100");

    RestClient userClient = restAsUser(context().user);

    Page<IdNamedDto> response = userClient.getPage(
      "/selections/gateway/serials?containsGatewaySerial=46",
      IdNamedDto.class
    );

    assertThat(response.getTotalElements()).isEqualTo(1);
    assertThat(response.getTotalPages()).isEqualTo(1);
    assertThat(response.getContent().get(0).name).isEqualTo("0016000466");
  }

  @Test
  public void userCanNotAccessOtherOrganisationsGatewaySerials() {
    anotherOrganisation = organisationJpaRepository.save(
      new OrganisationEntity(
        randomUUID(),
        "Another Organisation",
        "another-organisation",
        "another-organisation"
      ));

    createGateway(context().user.organisation.id, "0016000458");
    createGateway(context().user.organisation.id, "0016000466");
    createGateway(anotherOrganisation.id, "0016002100");

    RestClient userClient = restAsUser(context().user);

    Page<IdNamedDto> response = userClient.getPage(
      "/selections/gateway/serials?containsGatewaySerial",
      IdNamedDto.class
    );

    List<String> serials = asList("0016000458", "0016000466");

    assertThat(response.getTotalElements()).isEqualTo(2);
    assertThat(response.getTotalPages()).isEqualTo(1);
    assertThat(response.getContent().get(0).name).isIn(serials);
    assertThat(response.getContent().get(1).name).isIn(serials);
  }

  private Gateway createGateway(UUID organisationId, String serial) {
    return gateways.save(new Gateway(randomUUID(), organisationId, serial, "3100"));
  }

  private PhysicalMeter createPhysicalMeter(
    Organisation organisation,
    String address,
    LogicalMeter logicalMeter
  ) {
    return physicalMeters.save(new PhysicalMeter(
      randomUUID(),
      organisation,
      address,
      logicalMeter.externalId,
      "Gas",
      "elv",
      logicalMeter.id,
      60,
      emptyList()
    ));
  }

  private LogicalMeter createLogicalMeter(
    String country,
    String city,
    String address,
    String externalId
  ) {
    return createLogicalMeter(country, city, address, externalId, context().getOrganisationId());
  }

  private LogicalMeter createLogicalMeter(
    String country,
    String city,
    String address,
    String externalId,
    UUID organisationId
  ) {
    Location location = new LocationBuilder()
      .country(country)
      .city(city)
      .address(address)
      .build();
    return logicalMeters.save(new LogicalMeter(
      randomUUID(),
      externalId,
      organisationId,
      MeterDefinition.UNKNOWN_METER,
      ZonedDateTime.now(),
      emptyList(),
      emptyList(),
      location
    ));
  }
}
