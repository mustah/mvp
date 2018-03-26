package com.elvaco.mvp.database.util;

import java.util.Optional;

import com.elvaco.mvp.core.exception.UnitConversionError;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SqlErrorMapperTest {

  @SuppressWarnings("ConstantConditions")
  @Test
  public void mapsDimensionMismatchError() {
    Optional<RuntimeException> exc = SqlErrorMapper.mapScalingError("K", "ERROR: "
      + "dimension mismatch in \"@\" operation: \"285.589999999999975 K\", \"3.59999999999999964 "
      + "MJ\"");

    assertThat(exc).isPresent();
    assertThat(exc.get()).isInstanceOf(UnitConversionError.class);
  }

  @SuppressWarnings("ConstantConditions")
  @Test
  public void mapsUnknownUnitError() {
    Optional<RuntimeException> exc = SqlErrorMapper.mapScalingError("K", "ERROR:  unit "
      + "\"UnscalableUnit\" is not known");

    assertThat(exc).isPresent();
    assertThat(exc.get()).isInstanceOf(UnitConversionError.class);
  }

  @Test
  public void unknownSqlErrorIsMappedToEmptyOptional() {
    assertThat(
      SqlErrorMapper.mapScalingError("", "ERROR: Terror error!")
    ).isEmpty();
  }
}
