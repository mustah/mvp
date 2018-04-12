package com.elvaco.geoservice.service.geocodefarm;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AccuracyTest {

  @Test
  public void defaultsToZeroAccuracyCannotBeMatched() {
    assertThat(Accuracy.from("nothing").value).isEqualTo(0.0);
    assertThat(Accuracy.from("NO_MATCH").value).isEqualTo(0.0);
  }

  @Test
  public void findsMatchingAccuracy() {
    assertThat(Accuracy.from("EXACT_MATCH").value).isEqualTo(1.0);
    assertThat(Accuracy.from("HIGH_ACCURACY").value).isEqualTo(0.75);
    assertThat(Accuracy.from("MEDIUM_ACCURACY").value).isEqualTo(0.5);
  }
}
