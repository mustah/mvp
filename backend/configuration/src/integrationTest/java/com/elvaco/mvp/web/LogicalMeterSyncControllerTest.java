package com.elvaco.mvp.web;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.FeatureType;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.Property;
import com.elvaco.mvp.core.exception.PropertyNotFound;
import com.elvaco.mvp.core.spi.amqp.JobService;
import com.elvaco.mvp.core.usecase.PropertiesUseCases;
import com.elvaco.mvp.producers.rabbitmq.SyncRequestStatusType;
import com.elvaco.mvp.producers.rabbitmq.dto.FacilityDto;
import com.elvaco.mvp.producers.rabbitmq.dto.FacilityIdDto;
import com.elvaco.mvp.producers.rabbitmq.dto.GetReferenceInfoDto;
import com.elvaco.mvp.producers.rabbitmq.dto.MeterDto;
import com.elvaco.mvp.producers.rabbitmq.dto.MeteringReferenceInfoMessageDto;
import com.elvaco.mvp.testdata.RabbitIntegrationTest;
import com.elvaco.mvp.testdata.TestRabbitConsumer;
import com.elvaco.mvp.web.dto.ErrorMessageDto;
import com.elvaco.mvp.web.dto.SyncRequestResponseDto;
import com.elvaco.mvp.web.dto.SyncRequestStatusDto;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.junit.Assume.assumeTrue;

public class LogicalMeterSyncControllerTest extends RabbitIntegrationTest {

  @Autowired
  private PropertiesUseCases propertiesUseCases;

  @Autowired
  private JobService<MeteringReferenceInfoMessageDto> meterSyncJobService;

  @Autowired
  private RabbitTemplate rabbitTemplate;

  private Organisation otherOrganisation;

  @Before
  public void setUp() {
    meterSyncJobService.removeAllJobs();
    otherOrganisation = organisations.save(Organisation.of("Other Organisation"));
  }

  @After
  public void tearDown() {
    propertiesJpaRepository.deleteAll();
  }

  @Test
  public void syncMeterThatDoesNotExistReturns404() {
    ResponseEntity<ErrorMessageDto> response = asUser()
      .post(synchronizeUrl(randomUUID()), null, ErrorMessageDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  public void syncingMeterBelongingToOtherOrganisationReturns404() {
    LogicalMeter otherOrganisationsMeter =
      logicalMeters.save(newLogicalMeter(otherOrganisation.id));

    ResponseEntity<ErrorMessageDto> response = asUser().post(
      synchronizeUrl(otherOrganisationsMeter.id),
      null,
      ErrorMessageDto.class
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  public void regularUserSyncingMeterBelongingToSameOrganisationReturns403() {
    LogicalMeter logicalMeter = given(logicalMeter());

    ResponseEntity<ErrorMessageDto> responseEntity = asUser().post(
      synchronizeUrl(logicalMeter.id),
      null,
      ErrorMessageDto.class
    );
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(responseEntity.getBody().message)
      .contains("not allowed to publish synchronization requests");
  }

  @Test
  public void adminUserSyncingMeterBelongingToSameOrganisationReturns403() {
    LogicalMeter logicalMeter = given(logicalMeter());

    ResponseEntity<ErrorMessageDto> responseEntity = asAdmin().post(
      synchronizeUrl(logicalMeter.id),
      null,
      ErrorMessageDto.class
    );
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(responseEntity.getBody().message)
      .contains("not allowed to publish synchronization requests");
  }

  @Test
  public void superAdmin_UsersCanSyncMetersWithIds() {
    assumeTrue(isRabbitConnected());

    List<UUID> meterIds = Stream.of(
      given(logicalMeter()),
      given(logicalMeter()),
      given(logicalMeter())
    ).map(logicalMeter -> logicalMeter.id)
      .collect(toList());

    ResponseEntity<Void> responseEntity = asSuperAdmin()
      .post("/meters/sync", meterIds, Void.class);

    List<Property> properties = meterIds.stream()
      .map(this::getUpdateGeolocationWithEntityId)
      .collect(toList());

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    assertThat(properties).hasSize(3);
  }

  @Test
  public void meterIdsAndJobIdsArePresentInResponseBody() {
    assumeTrue(isRabbitConnected());

    List<UUID> meterIds = Stream.of(
      given(logicalMeter()),
      given(logicalMeter()),
      given(logicalMeter())
    )
      .map(logicalMeter -> logicalMeter.id)
      .collect(toList());

    ResponseEntity<List<SyncRequestResponseDto>> responseEntity = asSuperAdmin()
      .postList("/meters/sync", meterIds, SyncRequestResponseDto.class);

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    assertThat(responseEntity.getBody()).extracting("meterId")
      .containsExactlyInAnyOrder(meterIds.toArray());
    assertThat(responseEntity.getBody()).extracting("jobId").hasSameSizeAs(meterIds);
  }

  @Test
  public void sync_OnlySuperAdminCanCheckStatus() {
    assertThat(asAdmin()
      .get("/meters/sync?jobIds=12345", ErrorMessageDto.class).getStatusCode()
    ).isEqualTo(HttpStatus.FORBIDDEN);

    assertThat(asUser()
      .get("/meters/sync?jobIds=12345", ErrorMessageDto.class).getStatusCode()
    ).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  public void sync_UnknownStatusForUnknownJobIds() {
    List<SyncRequestStatusDto> syncDtos = asSuperAdmin()
      .getList("/meters/sync?jobIds=12345", SyncRequestStatusDto.class)
      .getBody();

    assertThat(syncDtos).containsExactly(
      new SyncRequestStatusDto("12345", null, SyncRequestStatusType.UNKNOWN)
    );
  }

  @Test
  public void sync_PendingStatusForPendingJob() {
    meterSyncJobService.newPendingJob("12345");

    List<SyncRequestStatusDto> syncDtos = asSuperAdmin()
      .getList("/meters/sync?jobIds=12345", SyncRequestStatusDto.class)
      .getBody();

    assertThat(syncDtos).containsExactly(
      new SyncRequestStatusDto("12345", null, SyncRequestStatusType.PENDING)
    );
  }

  @Test
  public void sync_CompletedStatusForCompletedJob() {
    MeteringReferenceInfoMessageDto response = newMeteringReferenceInfoMessageDto("12345");

    meterSyncJobService.update("12345", response);
    List<SyncRequestStatusDto> syncDtos = asSuperAdmin()
      .getList("/meters/sync?jobIds=12345", SyncRequestStatusDto.class)
      .getBody();

    assertThat(syncDtos).containsExactly(
      new SyncRequestStatusDto("12345", response.toString(), SyncRequestStatusType.COMPLETED)
    );
  }

  @Test
  public void sync_MessageIncludedForMultipleJobs() {
    MeteringReferenceInfoMessageDto response1 = newMeteringReferenceInfoMessageDto("12345");
    MeteringReferenceInfoMessageDto response2 = newMeteringReferenceInfoMessageDto("54321");
    meterSyncJobService.update("12345", response1);
    meterSyncJobService.update("54321", response2);
    List<SyncRequestStatusDto> syncDtos = asSuperAdmin()
      .getList("/meters/sync?jobIds=12345,54321,99999", SyncRequestStatusDto.class)
      .getBody();
    assertThat(syncDtos).containsExactlyInAnyOrder(
      new SyncRequestStatusDto("12345", response1.toString(), SyncRequestStatusType.COMPLETED),
      new SyncRequestStatusDto("54321", response2.toString(), SyncRequestStatusType.COMPLETED),
      new SyncRequestStatusDto("99999", null, SyncRequestStatusType.UNKNOWN)
    );
  }

  @Test
  public void userMustBeSuperUserToSyncMetersWithIds() {
    assumeTrue(isRabbitConnected());

    List<UUID> meterIds = Stream.of(
      given(logicalMeter()),
      given(logicalMeter())
    )
      .map(logicalMeter -> logicalMeter.id)
      .collect(toList());

    ResponseEntity<ErrorMessageDto> responseEntity = asUser()
      .post("/meters/sync", meterIds, ErrorMessageDto.class);

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(responseEntity.getBody().message)
      .contains("not allowed to publish synchronization requests");
  }

  @Test
  public void submittingRequestWhenQueueUnavailableReturns503() {
    LogicalMeter logicalMeter = given(logicalMeter());

    ConnectionFactory oldConnectionFactory = rabbitTemplate.getConnectionFactory();
    rabbitTemplate.setConnectionFactory(new BrokenConnectionFactory());

    try {
      ResponseEntity<ErrorMessageDto> responseEntity = asSuperAdmin().post(
        synchronizeUrl(logicalMeter.id),
        null,
        ErrorMessageDto.class
      );

      assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
      assertThat(responseEntity.getBody().message).contains("Ouch");
      assertThat(propertiesUseCases.shouldUpdateGeolocation(
        logicalMeter.id,
        logicalMeter.organisationId
      )).isFalse();
    } finally {
      rabbitTemplate.setConnectionFactory(oldConnectionFactory);
    }
  }

  @Test
  public void successfullySubmittedRequestShouldRespondWith_AcceptedStatusCode() {
    assumeTrue(isRabbitConnected());

    LogicalMeter logicalMeter = given(logicalMeter());

    ResponseEntity<List<SyncRequestResponseDto>> responseEntity = asSuperAdmin().postList(
      synchronizeUrl(logicalMeter.id),
      null,
      SyncRequestResponseDto.class
    );

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    assertThat(propertiesUseCases.shouldUpdateGeolocation(
      logicalMeter.id,
      logicalMeter.organisationId
    )).isTrue();
  }

  @Test
  public void requestIsEnqueuedWithFacilityId() throws InterruptedException, IOException {
    assumeTrue(isRabbitConnected());

    TestRabbitConsumer consumer = newResponseConsumer();
    LogicalMeter logicalMeter = given(logicalMeter());

    ResponseEntity<List<SyncRequestResponseDto>> responseEntity = asSuperAdmin().postList(
      synchronizeUrl(logicalMeter.id),
      null,
      SyncRequestResponseDto.class
    );

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    GetReferenceInfoDto enqueuedMessage = consumer.fromJson(GetReferenceInfoDto.class);

    assertSoftly(softly -> {
      softly.assertThat(enqueuedMessage.organisationId)
        .isEqualTo(context().defaultOrganisation().externalId);

      softly.assertThat(enqueuedMessage.gateway).isNull();

      softly.assertThat(enqueuedMessage.meter).isNotNull();

      softly.assertThat(enqueuedMessage.facility)
        .isEqualTo(new FacilityIdDto(logicalMeter.externalId));

      softly.assertThat(propertiesUseCases.shouldUpdateGeolocation(
        logicalMeter.id,
        logicalMeter.organisationId
      )).isTrue();
    });
  }

  @Test
  public void superAdmin_UsersCanSyncMetersByOrganisation() {
    assumeTrue(isRabbitConnected());

    UUID organisationId = given(organisation()).getId();

    logicalMeters.save(newLogicalMeter(organisationId));

    List<UUID> meterIds = Stream.of(
      given(logicalMeter()),
      given(logicalMeter()),
      given(logicalMeter())
    )
      .map(logicalMeter -> logicalMeter.id)
      .collect(toList());

    ResponseEntity<Void> responseEntity = asSuperAdmin()
      .post(
        "/meters/sync/organisation?id=" + context().organisationId().toString(),
        null,
        Void.class
      );

    List<Property> properties = meterIds.stream()
      .map(this::getUpdateGeolocationWithEntityId)
      .collect(toList());

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    assertThat(properties).hasSize(3);
  }

  @Test
  public void usersNotAllowedToSyncByOrganisation() {
    assumeTrue(isRabbitConnected());

    given(logicalMeter());
    given(logicalMeter());
    given(logicalMeter());

    ResponseEntity<ErrorMessageDto> responseEntity = asUser()
      .post(
        "/meters/sync/organisation?id=" + context().organisationId().toString(),
        null,
        ErrorMessageDto.class
      );

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(responseEntity.getBody().message)
      .contains("not allowed to publish synchronization requests");
  }

  @Override
  protected void afterRemoveEntitiesHook() {
    organisations.deleteById(otherOrganisation.id);
  }

  private MeteringReferenceInfoMessageDto newMeteringReferenceInfoMessageDto(String jobId) {
    return new MeteringReferenceInfoMessageDto(
      new MeterDto("meter-id", "some medium", "OK", "KAM", "0 * * * *", 1, 1),
      new FacilityDto("facility-id", "Sverige", "Kungsbacka", "Teknikgatan", "43437"),
      "Test system",
      "organisation-id",
      null,
      jobId
    );
  }

  private Property getUpdateGeolocationWithEntityId(UUID id) {
    return propertiesUseCases.findBy(
      FeatureType.UPDATE_GEOLOCATION,
      id,
      context().organisationId()
    ).orElseThrow(() -> new PropertyNotFound(FeatureType.UPDATE_GEOLOCATION, id));
  }

  private static LogicalMeter newLogicalMeter(UUID organisationId) {
    return LogicalMeter.builder()
      .externalId(randomUUID().toString())
      .organisationId(organisationId)
      .build();
  }

  private static String synchronizeUrl(UUID id) {
    return String.format("/meters/sync/%s", id);
  }

  private static class BrokenConnectionFactory implements ConnectionFactory {

    @Override
    public Connection createConnection() throws AmqpException {
      throw new AmqpException("Ouch");
    }

    @Nullable
    @Override
    public String getHost() {
      return null;
    }

    @Override
    public int getPort() {
      return 0;
    }

    @Nullable
    @Override
    public String getVirtualHost() {
      return null;
    }

    @Nullable
    @Override
    public String getUsername() {
      return null;
    }

    @Override
    public void addConnectionListener(ConnectionListener listener) {}

    @Override
    public boolean removeConnectionListener(ConnectionListener listener) {
      return false;
    }

    @Override
    public void clearConnectionListeners() {}
  }
}
