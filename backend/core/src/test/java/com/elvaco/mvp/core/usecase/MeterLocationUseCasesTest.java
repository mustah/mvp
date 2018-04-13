package com.elvaco.mvp.core.usecase;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.MeterSummary;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.testing.fixture.MockRequestParameters;
import com.elvaco.mvp.testing.fixture.UserBuilder;
import com.elvaco.mvp.testing.repository.MockLogicalMeters;
import com.elvaco.mvp.testing.security.MockAuthenticatedUser;
import org.junit.Before;
import org.junit.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class MeterLocationUseCasesTest {

  private final Organisation organisation = new Organisation(randomUUID(), "org ab", "org");

  private MockRequestParameters parameters;

  @Before
  public void setUp() {
    parameters = new MockRequestParameters();
  }

  @Test
  public void hasNoMeters() {
    MeterLocationUseCases useCases = useCasesWith(emptyList());

    MeterSummary summary = useCases.findAllForSummaryInfo(parameters);

    assertThat(summary.numMeters()).isEqualTo(0);
  }

  @Test
  public void hasOneMeter() {
    MeterLocationUseCases useCases = useCasesWith(singletonList(newMeter()));

    MeterSummary summary = useCases.findAllForSummaryInfo(parameters);

    assertThat(summary.numMeters()).isEqualTo(1);
  }

  @Test
  public void hasMoreThanOneMeters() {
    MeterLocationUseCases useCases = useCasesWith(asList(newMeter(), newMeter()));

    MeterSummary summary = useCases.findAllForSummaryInfo(parameters);

    assertThat(summary.numMeters()).isEqualTo(2);
  }

  @Test
  public void hasOneMeterAndOneCity() {
    MeterLocationUseCases useCases = useCasesWith(singletonList(newMeter()));

    MeterSummary summary = useCases.findAllForSummaryInfo(parameters);

    assertThat(summary.numMeters()).isEqualTo(1);
    assertThat(summary.numCities()).isEqualTo(0);
    assertThat(summary.numAddresses()).isEqualTo(0);
  }

  @Test
  public void hasTwoMetersTwoCitiesAndTwoAddresses() {
    MeterLocationUseCases useCases = useCasesWith(asList(
      newMeter(),
      newMeterWith(sweden()
                     .city("kungsbacka")
                     .address("drottninggatan 2"))
    ));

    MeterSummary summary = useCases.findAllForSummaryInfo(parameters);

    assertThat(summary.numMeters()).isEqualTo(2);
    assertThat(summary.numCities()).isEqualTo(1);
    assertThat(summary.numAddresses()).isEqualTo(1);
  }

  @Test
  public void haTwoMetersButOneLocation() {
    LocationBuilder drottningGatan = sweden()
      .city("kungsbacka")
      .address("drottninggatan 2");
    MeterLocationUseCases useCases = useCasesWith(asList(
      newMeterWith(drottningGatan),
      newMeterWith(drottningGatan)
    ));

    MeterSummary summary = useCases.findAllForSummaryInfo(parameters);

    assertThat(summary.numMeters()).isEqualTo(2);
    assertThat(summary.numCities()).isEqualTo(1);
    assertThat(summary.numAddresses()).isEqualTo(1);
  }

  @Test
  public void hasOneMeterAndOneKnownLocation() {
    MeterLocationUseCases useCases = useCasesWith(singletonList(
      newMeterWith(sweden()
                     .city("kungsbacka")
                     .address("drottinggatan 1"))
    ));

    MeterSummary summary = useCases.findAllForSummaryInfo(parameters);

    assertThat(summary.numMeters()).isEqualTo(1);
    assertThat(summary.numCities()).isEqualTo(1);
    assertThat(summary.numAddresses()).isEqualTo(1);
  }

  private LogicalMeter newMeter() {
    return newMeterWith(new LocationBuilder());
  }

  private LogicalMeter newMeterWith(LocationBuilder locationBuilder) {
    UUID meterId = UUID.randomUUID();
    return new LogicalMeter(
      meterId,
      "meter-" + meterId,
      organisation.id,
      locationBuilder.build(),
      ZonedDateTime.now()
    );
  }

  private MeterLocationUseCases useCasesWith(List<LogicalMeter> logicalMeters) {
    return new MeterLocationUseCases(
      new MockLogicalMeters(logicalMeters),
      new MockAuthenticatedUser(
        new UserBuilder()
          .name("mocked user")
          .email("mock@mock.net")
          .password("password")
          .organisation(organisation)
          .asSuperAdmin()
          .build(),
        "some-token"
      )
    );
  }

  private static LocationBuilder sweden() {
    return new LocationBuilder().country("sweden");
  }
}
