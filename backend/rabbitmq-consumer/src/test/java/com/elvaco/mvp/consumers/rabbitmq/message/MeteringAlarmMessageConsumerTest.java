package com.elvaco.mvp.consumers.rabbitmq.message;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Set;

import com.elvaco.mvp.consumers.rabbitmq.dto.AlarmDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringAlarmMessageDto;
import com.elvaco.mvp.core.domainmodels.AlarmLogEntry;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.PhysicalMeter;
import com.elvaco.mvp.core.security.OrganisationPermissions;
import com.elvaco.mvp.core.usecase.OrganisationUseCases;
import com.elvaco.mvp.core.usecase.PhysicalMeterUseCases;
import com.elvaco.mvp.producers.rabbitmq.dto.FacilityIdDto;
import com.elvaco.mvp.producers.rabbitmq.dto.MeterIdDto;
import com.elvaco.mvp.testing.repository.MockMeasurements;
import com.elvaco.mvp.testing.repository.MockMeterAlarmLogs;
import com.elvaco.mvp.testing.repository.MockMeterStatusLogs;
import com.elvaco.mvp.testing.repository.MockOrganisationAssets;
import com.elvaco.mvp.testing.repository.MockOrganisationThemes;
import com.elvaco.mvp.testing.repository.MockOrganisations;
import com.elvaco.mvp.testing.repository.MockPhysicalMeters;
import com.elvaco.mvp.testing.repository.MockUsers;
import com.elvaco.mvp.testing.security.MockAuthenticatedUser;

import org.junit.Before;
import org.junit.Test;

import static com.elvaco.mvp.consumers.rabbitmq.message.MeteringMessageMapper.METERING_TIMEZONE;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class MeteringAlarmMessageConsumerTest {

  private static final LocalDateTime START_TIME = LocalDateTime.parse("2017-09-22T08:45:49");

  private PhysicalMeterUseCases physicalMeterUseCases;
  private AlarmMessageConsumer messageConsumer;
  private MockMeterAlarmLogs meterAlarmLogs;
  private MockMeasurements mockMeasurements;
  private Organisation organisation;

  @Before
  public void setUp() {
    MockAuthenticatedUser authenticatedUser = MockAuthenticatedUser.superAdmin();
    mockMeasurements = new MockMeasurements();
    meterAlarmLogs = new MockMeterAlarmLogs(mockMeasurements);
    physicalMeterUseCases = new PhysicalMeterUseCases(
      authenticatedUser,
      new MockPhysicalMeters(),
      new MockMeterStatusLogs()
    );
    organisation = authenticatedUser.getOrganisation();
    messageConsumer = new MeteringAlarmMessageConsumer(
      physicalMeterUseCases,
      new OrganisationUseCases(
        authenticatedUser,
        new MockOrganisations(singletonList(organisation)),
        new OrganisationPermissions(new MockUsers(singletonList(authenticatedUser.getUser()))),
        new MockOrganisationAssets(),
        new MockOrganisationThemes()
      ),
      meterAlarmLogs
    );
  }

  @Test
  public void canHandleEmptyAlarms() {
    messageConsumer.accept(newAlarmMessage());

    Set<AlarmLogEntry> alarms = meterAlarmLogs.findAll();

    assertThat(alarms).isEmpty();
  }

  @Test
  public void hasConnectedPhysicalMeter() {
    PhysicalMeter physicalMeter = savePhysicalMeter();

    messageConsumer.accept(newAlarmMessage(newAlarmWithLowBattery()));

    assertThat(meterAlarmLogs.findAll()).containsExactly(AlarmLogEntry.builder()
      .id(1L)
      .primaryKey(physicalMeter.primaryKey())
      .mask(42)
      .start(toZonedDateTime())
      .lastSeen(toZonedDateTime())
      .build());
  }

  @Test
  public void cannotFindPhysicalMeter_ShouldHaveNoAlarms() {
    physicalMeterUseCases.save(PhysicalMeter.builder()
      .address("unknown-meter-address")
      .externalId("external-123")
      .organisationId(organisation.id)
      .build());

    messageConsumer.accept(newAlarmMessage(newAlarmWithLowBattery()));

    assertThat(meterAlarmLogs.findAll()).isEmpty();
  }

  private MeteringAlarmMessageDto newAlarmMessage(AlarmDto... alarms) {
    return new MeteringAlarmMessageDto(
      new MeterIdDto("meter-123"),
      new FacilityIdDto("external-123"),
      organisation.externalId,
      "Test source system",
      asList(alarms)
    );
  }

  private PhysicalMeter savePhysicalMeter() {
    return physicalMeterUseCases.save(PhysicalMeter.builder()
      .address("meter-123")
      .externalId("external-123")
      .organisationId(organisation.id)
      .build());
  }

  private ZonedDateTime toZonedDateTime() {
    return ZonedDateTime.of(START_TIME, METERING_TIMEZONE);
  }

  private AlarmDto newAlarmWithLowBattery() {
    return new AlarmDto(START_TIME, 42);
  }
}
