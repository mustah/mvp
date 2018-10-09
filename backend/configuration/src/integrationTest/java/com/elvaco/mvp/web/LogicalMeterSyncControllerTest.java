package com.elvaco.mvp.web;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import com.elvaco.mvp.core.domainmodels.FeatureType;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.Property;
import com.elvaco.mvp.core.exception.PropertyNotFound;
import com.elvaco.mvp.core.spi.amqp.JobService;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.core.spi.repository.Organisations;
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
import static org.junit.Assume.assumeTrue;

public class LogicalMeterSyncControllerTest extends RabbitIntegrationTest {

  @Autowired
  private Organisations organisations;

  @Autowired
  private LogicalMeters logicalMeters;

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
    otherOrganisation = organisations.save(new Organisation(
      randomUUID(),
      "Other Organisation",
      "other-organisation",
      "Other Organisation"
    ));
  }

  @After
  public void tearDown() {
    logicalMeterJpaRepository.deleteAll();
    propertiesJpaRepository.deleteAll();
    organisations.deleteById(otherOrganisation.id);
  }

  @Test
  public void syncMeterThatDoesNotExistReturns404() {
    ResponseEntity<ErrorMessageDto> response = asTestUser()
      .post(synchronizeUrl(randomUUID()), null, ErrorMessageDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  public void syncingMeterBelongingToOtherOrganisationReturns404() {
    LogicalMeter otherOrganisationsMeter =
      logicalMeters.save(newLogicalMeter(otherOrganisation.id));

    ResponseEntity<ErrorMessageDto> response = asTestUser().post(
      synchronizeUrl(otherOrganisationsMeter.id),
      null,
      ErrorMessageDto.class
    );

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  public void regularUserSyncingMeterBelongingToSameOrganisationReturns403() {
    LogicalMeter logicalMeter = logicalMeters.save(newLogicalMeter(context().organisationId()));

    ResponseEntity<ErrorMessageDto> responseEntity = asTestUser().post(
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
    LogicalMeter logicalMeter = logicalMeters.save(newLogicalMeter(context().organisationId()));

    ResponseEntity<ErrorMessageDto> responseEntity = asTestAdmin().post(
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
      logicalMeters.save(newLogicalMeter(context().organisationId())),
      logicalMeters.save(newLogicalMeter(context().organisationId())),
      logicalMeters.save(newLogicalMeter(context().organisationId()))
    ).map(logicalMeter -> logicalMeter.id)
      .collect(toList());

    ResponseEntity<Void> responseEntity = asTestSuperAdmin()
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
      logicalMeters.save(newLogicalMeter(context().organisationId())),
      logicalMeters.save(newLogicalMeter(context().organisationId())),
      logicalMeters.save(newLogicalMeter(context().organisationId()))
    ).map(logicalMeter -> logicalMeter.id)
      .collect(toList());

    ResponseEntity<List<SyncRequestResponseDto>> responseEntity = asTestSuperAdmin()
      .postList("/meters/sync", meterIds, SyncRequestResponseDto.class);

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    assertThat(responseEntity.getBody()).extracting("meterId")
      .containsExactlyInAnyOrder(meterIds.toArray());
    assertThat(responseEntity.getBody()).extracting("jobId").hasSameSizeAs(meterIds);
  }

  @Test
  public void sync_OnlySuperAdminCanCheckStatus() {
    assertThat(asTestAdmin()
      .get("/meters/sync?jobIds=12345", ErrorMessageDto.class).getStatusCode()
    ).isEqualTo(HttpStatus.FORBIDDEN);

    assertThat(asTestUser()
      .get("/meters/sync?jobIds=12345", ErrorMessageDto.class).getStatusCode()
    ).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  public void sync_UnknownStatusForUnknownJobIds() {
    List<SyncRequestStatusDto> syncDtos = asTestSuperAdmin()
      .getList("/meters/sync?jobIds=12345", SyncRequestStatusDto.class)
      .getBody();

    assertThat(syncDtos).containsExactly(
      new SyncRequestStatusDto("12345", null, SyncRequestStatusType.UNKNOWN)
    );
  }

  @Test
  public void sync_PendingStatusForPendingJob() {
    meterSyncJobService.newPendingJob("12345");

    List<SyncRequestStatusDto> syncDtos = asTestSuperAdmin()
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
    List<SyncRequestStatusDto> syncDtos = asTestSuperAdmin()
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
    List<SyncRequestStatusDto> syncDtos = asTestSuperAdmin()
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
      logicalMeters.save(newLogicalMeter(context().organisationId())),
      logicalMeters.save(newLogicalMeter(context().organisationId()))
    ).map(logicalMeter -> logicalMeter.id)
      .collect(toList());

    ResponseEntity<ErrorMessageDto> responseEntity = asTestUser()
      .post("/meters/sync", meterIds, ErrorMessageDto.class);

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(responseEntity.getBody().message)
      .contains("not allowed to publish synchronization requests");
  }

  @Test
  public void submittingRequestWhenQueueUnavailableReturns503() {
    LogicalMeter logicalMeter = logicalMeters.save(newLogicalMeter(context().organisationId()));

    ConnectionFactory oldConnectionFactory = rabbitTemplate.getConnectionFactory();
    rabbitTemplate.setConnectionFactory(new BrokenConnectionFactory());

    try {
      ResponseEntity<ErrorMessageDto> responseEntity = asTestSuperAdmin().post(
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

    LogicalMeter logicalMeter = logicalMeters.save(newLogicalMeter(context().organisationId()));

    ResponseEntity<List<SyncRequestResponseDto>> responseEntity = asTestSuperAdmin().postList(
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
    LogicalMeter logicalMeter = logicalMeters.save(newLogicalMeter(context().organisationId()));

    ResponseEntity<List<SyncRequestResponseDto>> responseEntity = asTestSuperAdmin().postList(
      synchronizeUrl(logicalMeter.id),
      null,
      SyncRequestResponseDto.class
    );

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    GetReferenceInfoDto enqueuedMessage = consumer.fromJson(GetReferenceInfoDto.class);
    assertThat(enqueuedMessage.organisationId).isEqualTo(context().organisation().externalId);
    assertThat(enqueuedMessage.gateway).isNull();
    assertThat(enqueuedMessage.meter).isNull();
    assertThat(enqueuedMessage.facility).isEqualTo(new FacilityIdDto(logicalMeter.externalId));
    assertThat(propertiesUseCases.shouldUpdateGeolocation(
      logicalMeter.id,
      logicalMeter.organisationId
    )).isTrue();
  }

  private MeteringReferenceInfoMessageDto newMeteringReferenceInfoMessageDto(String jobId) {
    return new MeteringReferenceInfoMessageDto(
      new MeterDto("meter-id", "some medium", "OK", "KAM", "0 * * * *", 1, 1),
      new FacilityDto("facility-id", "Sverige", "Kungsbacka", "Teknikgatan"),
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

    @Override
    public String getHost() {
      return null;
    }

    @Override
    public int getPort() {
      return 0;
    }

    @Override
    public String getVirtualHost() {
      return null;
    }

    @Override
    public String getUsername() {
      return null;
    }

    @Override
    public void addConnectionListener(ConnectionListener listener) {

    }

    @Override
    public boolean removeConnectionListener(ConnectionListener listener) {
      return false;
    }

    @Override
    public void clearConnectionListeners() {

    }
  }
}
