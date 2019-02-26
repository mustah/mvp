package com.elvaco.mvp.core.domainmodels;

import org.junit.Test;

import static com.elvaco.mvp.testing.fixture.OrganisationTestData.ELVACO;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;

public class MeterDefinitionTest {

  @Test
  public void belongsTo_systemDefinition() {
    assertThat(newMeterDefinition(null).belongsTo(ELVACO.getId())).isFalse();
  }

  @Test
  public void belongsTo_owningOrganisation() {
    assertThat(newMeterDefinition(ELVACO).belongsTo(ELVACO.getId())).isTrue();
  }

  @Test
  public void isDefault() {
    assertThat(newMeterDefinition(null).isDefault()).isTrue();
  }

  private MeterDefinition newMeterDefinition(Organisation organisation) {
    return new MeterDefinition(
      null,
      organisation,
      "test",
      new Medium(0L, "test medium"),
      true,
      emptySet()
    );
  }
}
