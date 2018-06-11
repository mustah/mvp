package com.elvaco.mvp.web;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import com.elvaco.mvp.core.domainmodels.Gateway;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.core.spi.repository.Organisations;
import com.elvaco.mvp.database.repository.jpa.LogicalMeterJpaRepository;
import com.elvaco.mvp.producers.rabbitmq.dto.FacilityIdDto;
import com.elvaco.mvp.producers.rabbitmq.dto.GetReferenceInfoDto;
import com.elvaco.mvp.testdata.RabbitIntegrationTest;
import com.elvaco.mvp.testdata.TestRabbitConsumer;
import com.elvaco.mvp.web.dto.ErrorMessageDto;
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

import static com.elvaco.mvp.core.domainmodels.Location.UNKNOWN_LOCATION;
import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeTrue;

public class LogicalMeterControllerSyncTest extends RabbitIntegrationTest {

  @Autowired
  private Organisations organisations;

  @Autowired
  private LogicalMeters logicalMeters;

  @Autowired
  private LogicalMeterJpaRepository logicalMeterJpaRepository;

  @Autowired
  private RabbitTemplate rabbitTemplate;

  private Organisation otherOrganisation;

  @Before
  public void setUp() {
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
    LogicalMeter logicalMeter = logicalMeters.save(newLogicalMeter(context().getOrganisationId()));

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
    LogicalMeter logicalMeter = logicalMeters.save(newLogicalMeter(context().getOrganisationId()));

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
      logicalMeters.save(newLogicalMeter(context().getOrganisationId())),
      logicalMeters.save(newLogicalMeter(context().getOrganisationId())),
      logicalMeters.save(newLogicalMeter(context().getOrganisationId()))
    ).map(logicalMeter -> logicalMeter.id)
      .collect(toList());

    ResponseEntity<Void> responseEntity = asTestSuperAdmin().post(
      "/meters/synchronize",
      meterIds,
      Void.class
    );

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
  }

  @Test
  public void userMustBeSuperUserToSyncMetersWithIds() {
    assumeTrue(isRabbitConnected());

    List<UUID> meterIds = Stream.of(
      logicalMeters.save(newLogicalMeter(context().getOrganisationId())),
      logicalMeters.save(newLogicalMeter(context().getOrganisationId()))
    ).map(logicalMeter -> logicalMeter.id)
      .collect(toList());

    ResponseEntity<ErrorMessageDto> responseEntity = asTestUser().post(
      "/meters/synchronize",
      meterIds,
      ErrorMessageDto.class
    );

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(responseEntity.getBody().message)
      .contains("not allowed to publish synchronization requests");
  }

  @Test
  public void submittingRequestWhenQueueUnavailableReturns503() {
    LogicalMeter logicalMeter = logicalMeters.save(newLogicalMeter(context().getOrganisationId()));

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
    } finally {
      rabbitTemplate.setConnectionFactory(oldConnectionFactory);
    }
  }

  @Test
  public void successfullySubmittedRequestShouldRespondWith_AcceptedStatusCode() {
    assumeTrue(isRabbitConnected());

    LogicalMeter logicalMeter = logicalMeters.save(newLogicalMeter(context().getOrganisationId()));

    ResponseEntity<ErrorMessageDto> responseEntity = asTestSuperAdmin().post(
      synchronizeUrl(logicalMeter.id),
      null,
      ErrorMessageDto.class
    );

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
  }

  @Test
  public void requestIsEnqueuedWithFacilityId() throws InterruptedException, IOException {
    assumeTrue(isRabbitConnected());

    TestRabbitConsumer consumer = newResponseConsumer();
    LogicalMeter logicalMeter = logicalMeters.save(newLogicalMeter(context().getOrganisationId()));

    ResponseEntity<Void> responseEntity = asTestSuperAdmin().post(
      synchronizeUrl(logicalMeter.id),
      null,
      Void.class
    );

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    GetReferenceInfoDto enqueuedMessage = consumer.fromJson(GetReferenceInfoDto.class);
    assertThat(enqueuedMessage.organisationId).isEqualTo(context().organisation().externalId);
    assertThat(enqueuedMessage.gateway).isNull();
    assertThat(enqueuedMessage.meter).isNull();
    assertThat(enqueuedMessage.facility).isEqualTo(new FacilityIdDto(logicalMeter.externalId));
  }

  private static LogicalMeter newLogicalMeter(UUID organisationId) {
    return newLogicalMeter(organisationId, emptyList(), emptyList());
  }

  private static LogicalMeter newLogicalMeter(
    UUID organisationId,
    List<PhysicalMeter> physicalMeters,
    List<Gateway> gateways
  ) {
    UUID logicalMeterId = randomUUID();
    return new LogicalMeter(
      logicalMeterId,
      logicalMeterId.toString(),
      organisationId,
      MeterDefinition.UNKNOWN_METER,
      ZonedDateTime.now(),
      physicalMeters,
      gateways,
      UNKNOWN_LOCATION
    );
  }

  private static String synchronizeUrl(UUID id) {
    return String.format("/meters/%s/synchronize", id);
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
