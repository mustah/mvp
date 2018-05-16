package com.elvaco.mvp.core.domainmodels;

import java.util.UUID;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class OrganisationTest {
  @Test
  public void nameIsUsedAsExternalId() {
    Organisation organisation = new Organisation(
      UUID.randomUUID(),
      "Räksmörgås C/O",
      "raksmorgas-co",
      "Räksmörgås C/O"
    );
    assertThat(organisation.externalId).isEqualTo("Räksmörgås C/O");
  }
}
