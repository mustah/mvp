package com.elvaco.mvp.consumers.rabbitmq.message;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.access.MediumProvider;
import com.elvaco.mvp.core.access.QuantityProvider;
import com.elvaco.mvp.core.access.SystemMeterDefinitionProvider;
import com.elvaco.mvp.core.domainmodels.MeasurementUnit;
import com.elvaco.mvp.core.domainmodels.Medium;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.security.OrganisationPermissions;
import com.elvaco.mvp.core.spi.repository.Gateways;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.core.spi.repository.MeterDefinitions;
import com.elvaco.mvp.core.spi.repository.Organisations;
import com.elvaco.mvp.core.spi.repository.PhysicalMeters;
import com.elvaco.mvp.core.unitconverter.UnitConverter;
import com.elvaco.mvp.core.usecase.GatewayUseCases;
import com.elvaco.mvp.core.usecase.LogicalMeterUseCases;
import com.elvaco.mvp.core.usecase.MeterDefinitionUseCases;
import com.elvaco.mvp.core.usecase.OrganisationUseCases;
import com.elvaco.mvp.core.usecase.PhysicalMeterUseCases;
import com.elvaco.mvp.core.usecase.PropertiesUseCases;
import com.elvaco.mvp.producers.rabbitmq.dto.MeteringReferenceInfoMessageDto;
import com.elvaco.mvp.testing.amqp.MockJobService;
import com.elvaco.mvp.testing.fixture.UserBuilder;
import com.elvaco.mvp.testing.geocode.MockGeocodeService;
import com.elvaco.mvp.testing.repository.MockGateways;
import com.elvaco.mvp.testing.repository.MockLogicalMetersWithCascading;
import com.elvaco.mvp.testing.repository.MockMeterDefinitions;
import com.elvaco.mvp.testing.repository.MockMeterStatusLogs;
import com.elvaco.mvp.testing.repository.MockOrganisationAssets;
import com.elvaco.mvp.testing.repository.MockOrganisationThemes;
import com.elvaco.mvp.testing.repository.MockOrganisations;
import com.elvaco.mvp.testing.repository.MockPhysicalMeters;
import com.elvaco.mvp.testing.repository.MockProperties;
import com.elvaco.mvp.testing.repository.MockUsers;
import com.elvaco.mvp.testing.security.MockAuthenticatedUser;

import org.junit.Before;

import static com.elvaco.mvp.core.domainmodels.MeterDefinition.DEFAULT_DISTRICT_HEATING;
import static com.elvaco.mvp.core.domainmodels.MeterDefinition.DEFAULT_HOT_WATER;
import static com.elvaco.mvp.core.domainmodels.MeterDefinition.DEFAULT_ROOM_SENSOR;
import static com.elvaco.mvp.core.domainmodels.MeterDefinition.DEFAULT_WATER;
import static com.elvaco.mvp.core.domainmodels.MeterDefinition.UNKNOWN;
import static com.elvaco.mvp.core.domainmodels.Quantity.DIFFERENCE_TEMPERATURE;
import static com.elvaco.mvp.core.domainmodels.Quantity.ENERGY;
import static com.elvaco.mvp.core.domainmodels.Quantity.ENERGY_RETURN;
import static com.elvaco.mvp.core.domainmodels.Quantity.EXTERNAL_TEMPERATURE;
import static com.elvaco.mvp.core.domainmodels.Quantity.FORWARD_TEMPERATURE;
import static com.elvaco.mvp.core.domainmodels.Quantity.HUMIDITY;
import static com.elvaco.mvp.core.domainmodels.Quantity.POWER;
import static com.elvaco.mvp.core.domainmodels.Quantity.REACTIVE_ENERGY;
import static com.elvaco.mvp.core.domainmodels.Quantity.RETURN_TEMPERATURE;
import static com.elvaco.mvp.core.domainmodels.Quantity.TEMPERATURE;
import static com.elvaco.mvp.core.domainmodels.Quantity.VOLUME;
import static com.elvaco.mvp.core.domainmodels.Quantity.VOLUME_FLOW;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toMap;

public abstract class MessageConsumerTest {

  protected static final String ORGANISATION_EXTERNAL_ID = "Some Organisation";
  protected static final String ORGANISATION_SLUG = "some-organisation";
  protected static final Organisation ORGANISATION = Organisation.of(ORGANISATION_EXTERNAL_ID);

  private static final Map<String, Medium> MEDIUM_MAP = Map.of(
    UNKNOWN.medium.name, UNKNOWN.medium,
    DEFAULT_HOT_WATER.medium.name, DEFAULT_HOT_WATER.medium,
    DEFAULT_WATER.medium.name, DEFAULT_WATER.medium,
    DEFAULT_DISTRICT_HEATING.medium.name, DEFAULT_DISTRICT_HEATING.medium,
    DEFAULT_ROOM_SENSOR.medium.name, DEFAULT_ROOM_SENSOR.medium
  );

  private static final Map<Medium, MeterDefinition> METER_DEFINITION_MAP = Map.of(
    UNKNOWN.medium, UNKNOWN,
    DEFAULT_HOT_WATER.medium, DEFAULT_HOT_WATER,
    DEFAULT_WATER.medium, DEFAULT_WATER,
    DEFAULT_DISTRICT_HEATING.medium, DEFAULT_DISTRICT_HEATING,
    DEFAULT_ROOM_SENSOR.medium, DEFAULT_ROOM_SENSOR
  );

  private static final Map<String, Quantity> QUANTITY_MAP = List.of(
    VOLUME,
    VOLUME_FLOW,
    TEMPERATURE,
    HUMIDITY,
    ENERGY,
    POWER,
    FORWARD_TEMPERATURE,
    RETURN_TEMPERATURE,
    DIFFERENCE_TEMPERATURE,
    ENERGY_RETURN,
    REACTIVE_ENERGY,
    EXTERNAL_TEMPERATURE
  ).stream().collect(toMap(q -> q.name, q -> q));

  AuthenticatedUser authenticatedUser;
  PhysicalMeters physicalMeters;
  Organisations organisations;
  LogicalMeters logicalMeters;
  Gateways gateways;
  MeterDefinitions meterDefinitions;
  LogicalMeterUseCases logicalMeterUseCases;
  PhysicalMeterUseCases physicalMeterUseCases;
  OrganisationUseCases organisationUseCases;
  GatewayUseCases gatewayUseCases;
  MeterDefinitionUseCases meterDefinitionUseCases;
  PropertiesUseCases propertiesUseCases;
  MockGeocodeService geocodeService;
  MockMeterStatusLogs meterStatusLogs;
  MockJobService<MeteringReferenceInfoMessageDto> jobService;

  MediumProvider mediumProvider = name -> Optional.ofNullable(MEDIUM_MAP.get(name));
  SystemMeterDefinitionProvider meterDefinitionProvider = medium -> Optional.ofNullable(
    METER_DEFINITION_MAP.get(medium));
  QuantityProvider quantityProvider = name -> Optional.ofNullable(QUANTITY_MAP.get(name));

  UnitConverter unitConverter = new UnitConverter() {
    @Nullable
    @Override
    public MeasurementUnit convert(
      MeasurementUnit measurementUnit, String targetUnit
    ) {
      return null;
    }

    @Override
    public boolean isSameDimension(String firstUnit, String secondUnit) {
      return true;
    }
  };

  @Before
  public void setUp() {
    authenticatedUser = new MockAuthenticatedUser(
      new UserBuilder()
        .name("mock user")
        .email("mock@somemail.nu")
        .password("P@$$w0rD")
        .organisation(Organisation.of(ORGANISATION_EXTERNAL_ID))
        .asSuperAdmin()
        .build(),
      randomUUID().toString()
    );
    physicalMeters = new MockPhysicalMeters();
    organisations = new MockOrganisations();
    logicalMeters = new MockLogicalMetersWithCascading(physicalMeters);
    gateways = new MockGateways();
    geocodeService = new MockGeocodeService();
    propertiesUseCases = new PropertiesUseCases(authenticatedUser, new MockProperties());
    meterDefinitions = new MockMeterDefinitions();

    meterStatusLogs = new MockMeterStatusLogs();
    jobService = new MockJobService<>();

    logicalMeterUseCases = new LogicalMeterUseCases(authenticatedUser, logicalMeters);
    physicalMeterUseCases = new PhysicalMeterUseCases(
      authenticatedUser,
      physicalMeters,
      meterStatusLogs
    );
    organisationUseCases = new OrganisationUseCases(
      authenticatedUser,
      organisations,
      new OrganisationPermissions(new MockUsers(singletonList(
        new UserBuilder()
          .name("super-admin")
          .email("super@admin.io")
          .password("password")
          .organisationElvaco()
          .asSuperAdmin()
          .build()
      ))),
      new MockOrganisationAssets(),
      new MockOrganisationThemes()
    );
    gatewayUseCases = new GatewayUseCases(gateways, authenticatedUser);

    meterDefinitionUseCases = new MeterDefinitionUseCases(
      authenticatedUser,
      meterDefinitions,
      unitConverter,
      organisations,
      quantityProvider,
      mediumProvider,
      meterDefinitionProvider,
      logicalMeters
    );
  }
}
