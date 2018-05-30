package com.elvaco.mvp.consumers.rabbitmq;

import java.util.Optional;
import java.util.UUID;

import com.elvaco.mvp.consumers.rabbitmq.dto.FacilityDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.GatewayStatusDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeterDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeasurementMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringReferenceInfoMessageDto;
import com.elvaco.mvp.core.spi.repository.Gateways;
import com.elvaco.mvp.core.spi.repository.Organisations;
import com.elvaco.mvp.database.entity.meter.PhysicalMeterEntity;
import com.elvaco.mvp.database.repository.jpa.GatewayJpaRepository;
import com.elvaco.mvp.database.repository.jpa.GatewayStatusLogJpaRepository;
import com.elvaco.mvp.database.repository.jpa.LogicalMeterJpaRepository;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterJpaRepository;
import com.elvaco.mvp.database.repository.jpa.PhysicalMeterStatusLogJpaRepository;
import com.elvaco.mvp.producers.rabbitmq.dto.FacilityIdDto;
import com.elvaco.mvp.producers.rabbitmq.dto.GatewayIdDto;
import com.elvaco.mvp.producers.rabbitmq.dto.GetReferenceInfoDto;
import com.elvaco.mvp.producers.rabbitmq.dto.MeterIdDto;
import com.elvaco.mvp.testdata.RabbitIntegrationTest;
import com.elvaco.mvp.testdata.TestRabbitConsumer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.elvaco.mvp.producers.rabbitmq.MessageSerializer.toJson;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeTrue;

@SuppressWarnings("SameParameterValue")
public class RabbitMqConsumerTest extends RabbitIntegrationTest {

  @Autowired
  private Organisations organisations;

  @Autowired
  private Gateways gateways;

  @Autowired
  private PhysicalMeterJpaRepository physicalMeterJpaRepository;

  @Autowired
  private PhysicalMeterStatusLogJpaRepository physicalMeterStatusLogJpaRepository;

  @Autowired
  private LogicalMeterJpaRepository logicalMeterJpaRepository;

  @Autowired
  private GatewayJpaRepository gatewayJpaRepository;

  @Autowired
  private GatewayStatusLogJpaRepository gatewayStatusLogJpaRepository;

  @Before
  public void setUp() {
    assumeTrue(isRabbitConnected());
  }

  @After
  public void tearDown() {
    physicalMeterStatusLogJpaRepository.deleteAll();
    physicalMeterJpaRepository.deleteAll();
    logicalMeterJpaRepository.deleteAll();
    gatewayStatusLogJpaRepository.deleteAll();
    gatewayJpaRepository.deleteAll();
    organisations.findBySlug("some-organisation")
      .ifPresent(organisation -> organisations.deleteById(organisation.id));
    organisations.findBySlug("organisation-123")
      .ifPresent(organisation -> organisations.deleteById(organisation.id));
  }

  @Test
  public void messagesSentToRabbitAreReceivedAndProcessed() throws Exception {
    MeteringReferenceInfoMessageDto message = getMeteringReferenceInfoMessageDto();

    publishMessage(toJson(message).getBytes());

    assertOrganisationWithSlugWasCreated("some-organisation");

    UUID organisationId = organisations.findBySlug("some-organisation").get().id;
    assertLogicalMeterWasCreated(organisationId, "facility-id");
    assertPhysicalMeterIsCreated(organisationId, "1234", "facility-id");
    assertGatewayWasCreated(organisationId, "123987");
  }

  @Test
  public void processMessageWithMissingMeter() throws Exception {
    MeteringReferenceInfoMessageDto message = getMeteringReferenceInfoMessageDto();

    publishMessage(toJson(message).getBytes());

    MeteringReferenceInfoMessageDto newMessage = getMeteringReferenceInfoMessageDto()
      .withMeter(null)
      .withFacility(new FacilityDto("facility-id", "Sweden", "Varberg", "Drottninggatan 1"))
      .withGatewayStatus(new GatewayStatusDto("123987", "Gateway 3100", "OK"));

    publishMessage(toJson(newMessage).getBytes());

    assertOrganisationWithSlugWasCreated("some-organisation");

    UUID organisationId = organisations.findBySlug("some-organisation").get().id;
    assertLogicalMeterLocation(
      organisationId,
      "facility-id",
      "Sweden",
      "Varberg",
      "Drottninggatan 1"
    );

    assertGatewayModel(
      organisationId,
      "123987", "Gateway 3100"
    );
  }

  @Test
  public void processMessageWithMissingGateway() throws Exception {
    MeteringReferenceInfoMessageDto message = getMeteringReferenceInfoMessageDto();

    publishMessage(toJson(message).getBytes());

    MeteringReferenceInfoMessageDto newMessage = getMeteringReferenceInfoMessageDto()
      .withMeter(new MeterDto("1234", "Some medium", "OK", "Acme", "*/15 * * * *"))
      .withFacility(new FacilityDto("facility-id", "Sweden", "Kungsbacka", "Kabelgatan 2T"))
      .withGatewayStatus(null);

    publishMessage(toJson(newMessage).getBytes());

    assertOrganisationWithSlugWasCreated("some-organisation");

    UUID organisationId = organisations.findBySlug("some-organisation").get().id;
    assertPhysicalMeterManufacturer(
      organisationId,
      "facility-id",
      "1234",
      "Acme"
    );
  }

  @Test
  public void responseMessagesForMeasurementMessagesArePublished() throws Exception {
    MeteringMeasurementMessageDto message = new MeteringMeasurementMessageDto(
      new GatewayIdDto("GATEWAY-123"),
      new MeterIdDto("METER-123"),
      new FacilityIdDto("FACILITY-123"),
      "ORGANISATION-123",
      "test",
      emptyList()
    );
    TestRabbitConsumer consumer = newResponseConsumer();

    publishMessage(toJson(message).getBytes());

    GetReferenceInfoDto response = consumer.fromJson(GetReferenceInfoDto.class);

    assertThat(response)
      .isEqualTo(
        new GetReferenceInfoDto(
          "ORGANISATION-123",
          null, new MeterIdDto("METER-123"),
          new GatewayIdDto("GATEWAY-123"),
          new FacilityIdDto("FACILITY-123")
        ));
  }

  private MeteringReferenceInfoMessageDto getMeteringReferenceInfoMessageDto() {
    return new MeteringReferenceInfoMessageDto(
      new MeterDto("1234", "Some medium", "OK", "A manufacturer", "*/15 * * * *"),
      new FacilityDto("facility-id", "Sweden", "Kungsbacka", "Kabelgatan 2T"),
      "test",
      "Some organisation",
      new GatewayStatusDto("123987", "Gateway 2000", "OK")
    );
  }

  private void assertLogicalMeterWasCreated(
    UUID organisationId,
    String externalId
  ) throws InterruptedException {
    assertThat(waitForCondition(() -> logicalMeterJpaRepository.findOneBy(
      organisationId,
      externalId
    ).isPresent())).as("Logical meter '" + externalId + "' was created").isTrue();
  }

  private void assertPhysicalMeterManufacturer(
    UUID organisationId,
    String externalId,
    String meterId,
    String manufacturer
  ) throws InterruptedException {
    assertThat(waitForCondition(() -> physicalMeterJpaRepository
      .findByOrganisationIdAndExternalIdAndAddress(organisationId, externalId, meterId)
      .filter(meter -> meter.manufacturer.equalsIgnoreCase(manufacturer))
      .isPresent()
    )).as("Physical meter '" + externalId + "' has manufacturer" + manufacturer)
      .isTrue();
  }

  private void assertLogicalMeterLocation(
    UUID organisationId,
    String externalId,
    String country,
    String city,
    String address
  ) throws InterruptedException {
    assertThat(waitForCondition(() -> hasMeterAddress(
      organisationId,
      externalId,
      country,
      city,
      address
    ))).as("Logical meter '" + externalId + "' has address").isTrue();
  }

  private boolean hasMeterAddress(
    UUID organisationId,
    String externalId,
    String country,
    String city,
    String address
  ) {
    return logicalMeterJpaRepository.findOneBy(
      organisationId,
      externalId
    )
      .filter(meter ->
        meter.location.country.equalsIgnoreCase(country)
        && meter.location.city.equalsIgnoreCase(city)
        && meter.location.streetAddress.equalsIgnoreCase(address))
      .isPresent();
  }

  private void assertGatewayWasCreated(
    UUID organisationId,
    String serial
  ) throws InterruptedException {
    assertThat(waitForCondition(() -> gateways.findAllByOrganisationId(organisationId)
      .stream()
      .anyMatch(gateway -> gateway.serial.equals(serial))
    )).as("Gateway '" + serial + "' was created").isTrue();
  }

  private void assertGatewayModel(
    UUID organisationId,
    String serial,
    String model
  ) throws InterruptedException {
    assertThat(waitForCondition(() -> gateways.findAllByOrganisationId(organisationId)
      .stream()
      .anyMatch(gateway -> gateway.serial.equals(serial) && gateway.productModel.equalsIgnoreCase(
        model))
    )).as("Gateway '" + serial + "' has model" + model).isTrue();
  }

  private void assertPhysicalMeterIsCreated(
    UUID organisationId,
    String address,
    String externalId
  ) throws InterruptedException {
    assertThat(waitForCondition(
      () -> {
        Optional<PhysicalMeterEntity> meter = physicalMeterJpaRepository
          .findByOrganisationIdAndExternalIdAndAddress(
            organisationId,
            externalId,
            address
          );
        return meter.isPresent();
      }
    )).as("Physical meter '" + externalId + "' was created").isTrue();
  }

  private void assertOrganisationWithSlugWasCreated(String slug) throws InterruptedException {
    assertThat(waitForCondition(() -> organisations.findBySlug(slug)
      .isPresent())).as("Organisation '" + slug + "' was created").isTrue();
  }
}
