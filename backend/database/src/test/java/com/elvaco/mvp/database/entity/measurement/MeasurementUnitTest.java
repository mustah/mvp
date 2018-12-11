package com.elvaco.mvp.database.entity.measurement;

import com.elvaco.mvp.core.domainmodels.MeasurementUnit;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class MeasurementUnitTest {

  @Test
  public void formatsValuesCorrectly() {
    assertThat(new MeasurementUnit("m", 12.0).toString()).isEqualTo("12 m");
    assertThat(new MeasurementUnit("m", 14.5).toString()).isEqualTo("14.5 m");
    assertThat(new MeasurementUnit("m", 14.1234).toString()).isEqualTo("14.1234 m");
  }

  @Test
  public void superscriptsExponents() {
    assertThat(new MeasurementUnit("m^0", 1.0).toString()).isEqualTo("1 m⁰");
    assertThat(new MeasurementUnit("m^1", 1.0).toString()).isEqualTo("1 m¹");
    assertThat(new MeasurementUnit("m^2", 1.0).toString()).isEqualTo("1 m²");
    assertThat(new MeasurementUnit("m^3", 1.0).toString()).isEqualTo("1 m³");
    assertThat(new MeasurementUnit("m^4", 1.0).toString()).isEqualTo("1 m⁴");
    assertThat(new MeasurementUnit("m^5", 1.0).toString()).isEqualTo("1 m⁵");
    assertThat(new MeasurementUnit("m^6", 1.0).toString()).isEqualTo("1 m⁶");
    assertThat(new MeasurementUnit("m^7", 1.0).toString()).isEqualTo("1 m⁷");
    assertThat(new MeasurementUnit("m^8", 1.0).toString()).isEqualTo("1 m⁸");
    assertThat(new MeasurementUnit("m^9", 1.0).toString()).isEqualTo("1 m⁹");
    assertThat(new MeasurementUnit("m^25", 14.1234).toString()).isEqualTo("14.1234 m²⁵");
  }

  @Test
  public void doesntSuperscriptNonExponent() {
    assertThat(new MeasurementUnit("^.^", 1.0).toString()).isEqualTo("1 ^.^");
    assertThat(new MeasurementUnit("^tak", 1.0).toString()).isEqualTo("1 ^tak");
  }

  @Test
  public void fromEmptyUnit() {
    assertThatThrownBy(() -> MeasurementUnit.from("1"))
      .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> MeasurementUnit.from("1 "))
      .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> MeasurementUnit.from("1.0"))
      .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> MeasurementUnit.from("1.0 "))
      .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> MeasurementUnit.from(" 1.0 "))
      .isInstanceOf(IllegalArgumentException.class);
  }
}
