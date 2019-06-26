package com.elvaco.mvp.consumers.rabbitmq;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMeasurementMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.ValueDto;
import com.elvaco.mvp.producers.rabbitmq.dto.FacilityDto;
import com.elvaco.mvp.producers.rabbitmq.dto.FacilityIdDto;
import com.elvaco.mvp.producers.rabbitmq.dto.GatewayIdDto;
import com.elvaco.mvp.producers.rabbitmq.dto.GatewayStatusDto;
import com.elvaco.mvp.producers.rabbitmq.dto.MeterDto;
import com.elvaco.mvp.producers.rabbitmq.dto.MeterIdDto;
import com.elvaco.mvp.producers.rabbitmq.dto.MeteringReferenceInfoMessageDto;
import com.elvaco.mvp.testdata.RabbitIntegrationTest;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static com.elvaco.mvp.producers.rabbitmq.MessageSerializer.toJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeTrue;

@SuppressWarnings("SameParameterValue")
public class RabbitMqConsumerTest extends RabbitIntegrationTest {

  @Before
  public void setUp() {
    assumeTrue(isRabbitConnected());
  }

  @Test
  public void messagesSentToRabbitAreReceivedAndProcessed() throws Exception {
    MeteringReferenceInfoMessageDto message = getMeteringReferenceInfoMessageDto();

    publishMessage(toJson(message).getBytes(StandardCharsets.UTF_8));

    assertOrganisationWithSlugWasCreated("some-organisation");

    UUID organisationId = organisationJpaRepository.findBySlug("some-organisation").get().id;
    assertLogicalMeterWasCreated(organisationId, "facility-id");
    assertPhysicalMeterIsCreated(organisationId, "1234", "facility-id");
    assertGatewayWasCreated(organisationId, "123987");
  }

  @Test
  public void processMessageWithMissingMeter() throws Exception {
    MeteringReferenceInfoMessageDto message = getMeteringReferenceInfoMessageDto();

    publishMessage(toJson(message).getBytes(StandardCharsets.UTF_8));

    assertOrganisationWithSlugWasCreated("some-organisation");
    UUID organisationId = organisationJpaRepository.findBySlug("some-organisation").get().id;
    assertLogicalMeterWasCreated(organisationId, "facility-id");

    MeteringReferenceInfoMessageDto newMessage = getMeteringReferenceInfoMessageDto()
      .withMeter(null)
      .withFacility(new FacilityDto(
        "facility-id",
        "Sweden",
        "Varberg",
        "Drottninggatan 1",
        "43445"
      ))
      .withGatewayStatus(new GatewayStatusDto("123987", "Gateway 3100", "OK", "8.8.8.8", ""));

    publishMessage(toJson(newMessage).getBytes(StandardCharsets.UTF_8));

    assertLogicalMeterLocation(
      organisationId,
      "facility-id",
      "Sweden",
      "Varberg",
      "Drottninggatan 1",
      "43445"
    );
    assertGatewayModel(organisationId, "123987", "Gateway 3100");
  }

  @Test
  public void processMessageWithMissingGatewayStatusAndShouldUpdateMeterManufacturer()
    throws Exception {
    MeteringReferenceInfoMessageDto newMessage = getMeteringReferenceInfoMessageDto()
      .withMeter(newMeterDto("Acme"))
      .withFacility(new FacilityDto(
        "facility-id",
        "Sweden",
        "Kungsbacka",
        "Kabelgatan 2T",
        "43427"
      ))
      .withGatewayStatus(null);

    publishMessage(toJson(newMessage).getBytes(StandardCharsets.UTF_8));

    assertOrganisationWithSlugWasCreated("some-organisation");

    UUID organisationId = organisationJpaRepository.findBySlug("some-organisation").get().id;
    assertPhysicalMeterManufacturer(organisationId, "facility-id", "1234", "Acme", "Some medium");
  }

  @Test
  public void responseMessagesForMeasurementMessagesArePublishedAndOrgIsCreated() throws Exception {
    MeteringMeasurementMessageDto message = new MeteringMeasurementMessageDto(
      new GatewayIdDto("gateway-123-456"),
      new MeterIdDto("meter-123-456"),
      new FacilityIdDto("facility-123-456"),
      "organisation-123-456",
      "test",
      List.of(new ValueDto(LocalDateTime.now(), 0.659, "°C", "Return temp."))
    );

    publishMessage(toJson(message).getBytes(StandardCharsets.UTF_8));

    assertOrganisationWithSlugWasCreated("organisation-123-456");
  }

  @Test
  public void meterWithSameStatus_ShouldNotCreateNewStatusWithCurrentTimestamp() throws Exception {
    MeteringReferenceInfoMessageDto message = getMeteringReferenceInfoMessageDto();

    publishMessage(toJson(message).getBytes(StandardCharsets.UTF_8));

    assertOrganisationWithSlugWasCreated("some-organisation");

    publishMessage(toJson(message).getBytes(StandardCharsets.UTF_8));

    waitFor(100);

    publishMessage(toJson(message).getBytes(StandardCharsets.UTF_8));

    assertThat(waitForCondition(() -> physicalMeterStatusLogJpaRepository.findAll().size() == 1))
      .as("Just one status log has been created").isTrue();
  }

  @Test
  @Ignore("Takes too long time to run for every day use...-")
  public void newOrganisationInParalell() throws Exception {
    for (int org = 0; org < 1000; org++) {
      for (int id = 0; id < 10; id++) {
        MeteringMeasurementMessageDto message = new MeteringMeasurementMessageDto(
          new GatewayIdDto("gateway-123-" + org + "-" + id),
          new MeterIdDto("meter-123-" + org + "-" + id),
          new FacilityIdDto("facility-123-" + org + "-" + id),
          "organisation-" + org,
          "test",
          List.of(new ValueDto(LocalDateTime.now(), 0.659, "°C", "Return temp."))
        );

        publishMessage(toJson(message).getBytes(StandardCharsets.UTF_8));
      }
    }
    waitFor(60000);

    for (int org = 0; org < 1000; org++) {
      assertOrganisationWithSlugWasCreated("organisation-" + org);
    }
  }

  private MeteringReferenceInfoMessageDto getMeteringReferenceInfoMessageDto() {
    return new MeteringReferenceInfoMessageDto(
      newMeterDto("A manufacturer"),
      new FacilityDto("facility-id", "Sweden", "Kungsbacka", "Kabelgatan 2T", "43437"),
      "test",
      "Some organisation",
      new GatewayStatusDto("123987", "Gateway 2000", "OK", "8.8.8.8", "070123123"),
      ""
    );
  }

  private void assertLogicalMeterWasCreated(
    UUID organisationId,
    String externalId
  ) throws InterruptedException {
    assertThat(waitForCondition(() ->
      logicalMeterJpaRepository.findBy(organisationId, externalId).isPresent()
    )).as("Logical meter '" + externalId + "' was created").isTrue();
  }

  private void assertPhysicalMeterManufacturer(
    UUID organisationId,
    String externalId,
    String meterId,
    String manufacturer,
    String medium
  ) throws InterruptedException {
    assertThat(waitForCondition(() -> physicalMeterJpaRepository
      .findByOrganisationIdAndExternalIdAndAddress(organisationId, externalId, meterId)
      .filter(meter -> meter.manufacturer.equalsIgnoreCase(manufacturer))
      .filter(meter -> meter.medium.equals(medium))
      .isPresent()
    )).as("Physical meter '" + externalId + "' has manufacturer '" + manufacturer + "'").isTrue();
  }

  private void assertLogicalMeterLocation(
    UUID organisationId,
    String externalId,
    String country,
    String city,
    String address,
    String zip
  ) throws InterruptedException {
    assertThat(waitForCondition(() -> hasMeterStreetAddress(
      organisationId,
      externalId,
      country,
      city,
      address,
      zip
    ))).as("Logical meter '" + externalId + "' has address").isTrue();
  }

  private boolean hasMeterStreetAddress(
    UUID organisationId,
    String externalId,
    String country,
    String city,
    String address,
    String zip
  ) {
    return logicalMeterJpaRepository.findBy(organisationId, externalId)
      .filter(meter ->
        meter.location.country.equalsIgnoreCase(country)
          && meter.location.city.equalsIgnoreCase(city)
          && meter.location.streetAddress.equalsIgnoreCase(address)
          && meter.location.zip.equals(zip))
      .isPresent();
  }

  private void assertGatewayWasCreated(
    UUID organisationId,
    String serial
  ) throws InterruptedException {
    assertThat(waitForCondition(() ->
      gatewayJpaRepository.findAllByOrganisationId(organisationId).stream()
        .anyMatch(gateway -> gateway.serial.equals(serial))
    )).as("Gateway '" + serial + "' was created").isTrue();
  }

  private void assertGatewayModel(UUID organisationId, String serial, String model)
    throws InterruptedException {
    assertThat(waitForCondition(() ->
      gatewayJpaRepository.findAllByOrganisationId(organisationId).stream()
        .anyMatch(gateway ->
          gateway.serial.equals(serial) && gateway.productModel.equalsIgnoreCase(model))
    )).as("Gateway '" + serial + "' has model" + model).isTrue();
  }

  private void assertPhysicalMeterIsCreated(UUID organisationId, String address, String externalId)
    throws InterruptedException {
    assertThat(waitForCondition(() ->
      physicalMeterJpaRepository.findByOrganisationIdAndExternalIdAndAddress(
        organisationId,
        externalId,
        address
      ).isPresent()
    )).as("Physical meter '" + externalId + "' was created").isTrue();
  }

  private void assertOrganisationWithSlugWasCreated(String slug) throws InterruptedException {
    assertThat(waitForCondition(() -> organisationJpaRepository.findBySlug(slug)
      .isPresent())).as("Organisation '" + slug + "' was created").isTrue();
  }

  private static MeterDto newMeterDto(String manufacturer) {
    return newMeterDto(manufacturer, "Some medium");
  }

  private static MeterDto newMeterDto(String manufacturer, String medium) {
    return new MeterDto("1234", medium, "OK", manufacturer, "*/15 * * * *", 1, 1);
  }

  private static void waitFor(int millis) throws InterruptedException {
    Thread.sleep(millis);
  }
}
