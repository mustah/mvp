package com.elvaco.mvp.core.usecase;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.LocationBuilder;
import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.MeterSummary;
import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.testing.fixture.UserBuilder;
import com.elvaco.mvp.testing.repository.MockLogicalMeters;
import com.elvaco.mvp.testing.security.MockAuthenticatedUser;
import org.junit.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class MeterLocationUseCasesTest {

  private final Organisation organisation = new Organisation(randomUUID(), "org ab", "org");

  @Test
  public void hasNoMeters() {
    MeterLocationUseCases useCases = useCasesWith(emptyList());

    MeterSummary summary = useCases.findAllForSummaryInfo(new MockRequestParameters());

    assertThat(summary.numMeters()).isEqualTo(0);
  }

  @Test
  public void hasOneMeter() {
    MeterLocationUseCases useCases = useCasesWith(singletonList(newMeter()));

    MeterSummary summary = useCases.findAllForSummaryInfo(new MockRequestParameters());

    assertThat(summary.numMeters()).isEqualTo(1);
  }

  @Test
  public void hasMoreThanOneMeters() {
    MeterLocationUseCases useCases = useCasesWith(asList(newMeter(), newMeter()));

    MeterSummary summary = useCases.findAllForSummaryInfo(new MockRequestParameters());

    assertThat(summary.numMeters()).isEqualTo(2);
  }

  @Test
  public void hasOneMeterAndOneCity() {
    MeterLocationUseCases useCases = useCasesWith(singletonList(newMeter()));

    MeterSummary summary = useCases.findAllForSummaryInfo(new MockRequestParameters());

    assertThat(summary.numMeters()).isEqualTo(1);
    assertThat(summary.numCities()).isEqualTo(1);
  }

  @Test
  public void hasTwoMetersTwoCitiesAndTwoAddresses() {
    MeterLocationUseCases useCases = useCasesWith(asList(
      newMeter(),
      newMeterWith(sweden()
                     .city("kungsbacka")
                     .streetAddress("drottninggatan 2"))
    ));

    MeterSummary summary = useCases.findAllForSummaryInfo(new MockRequestParameters());

    assertThat(summary.numMeters()).isEqualTo(2);
    assertThat(summary.numCities()).isEqualTo(2);
    assertThat(summary.numAddresses()).isEqualTo(2);
  }

  @Test
  public void haTwoMetersButOneLocation() {
    LocationBuilder drottningGatan = sweden()
      .city("kungsbacka")
      .streetAddress("drottninggatan 2");
    MeterLocationUseCases useCases = useCasesWith(asList(
      newMeterWith(drottningGatan),
      newMeterWith(drottningGatan)
    ));

    MeterSummary summary = useCases.findAllForSummaryInfo(new MockRequestParameters());

    assertThat(summary.numMeters()).isEqualTo(2);
    assertThat(summary.numCities()).isEqualTo(1);
    assertThat(summary.numAddresses()).isEqualTo(1);
  }

  @Test
  public void hasOneMeterAndOneKnownLocation() {
    MeterLocationUseCases useCases = useCasesWith(singletonList(
      newMeterWith(sweden()
                     .city("kungsbacka")
                     .streetAddress("drottinggatan 1"))
    ));

    MeterSummary summary = useCases.findAllForSummaryInfo(new MockRequestParameters());

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

  private static class MockRequestParameters implements RequestParameters {

    @Override
    public RequestParameters add(String name, String value) {
      return null;
    }

    @Override
    public RequestParameters setAll(Map<String, String> values) {
      return null;
    }

    @Override
    public RequestParameters setAll(String name, List<String> values) {
      return null;
    }

    @Override
    public RequestParameters replace(String name, String value) {
      return null;
    }

    @Override
    public List<String> getValues(String name) {
      return null;
    }

    @Override
    public Set<Entry<String, List<String>>> entrySet() {
      return null;
    }

    @Nullable
    @Override
    public String getFirst(String name) {
      return null;
    }

    @Override
    public boolean hasName(String name) {
      return false;
    }

    @Override
    public boolean isEmpty() {
      return false;
    }
  }
}
