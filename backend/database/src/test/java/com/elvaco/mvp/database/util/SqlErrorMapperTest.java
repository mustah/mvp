package com.elvaco.mvp.database.util;

import java.sql.SQLException;

import com.elvaco.mvp.core.exception.MixedDimensionForMeterQuantity;
import com.elvaco.mvp.core.exception.UnitConversionError;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.Test;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("ConstantConditions")
public class SqlErrorMapperTest {

  @Test
  public void mapsDataIntegrityViolationException_unmapped() {
    DataIntegrityViolationException exc = newDataIntegrityViolationException("blah");
    assertThat(SqlErrorMapper.mapDataIntegrityViolation(exc)).isEqualTo(exc);
  }

  @Test
  public void mapsDataIntegrityViolationException_mixedDimensionError_multiline() {
    String message = "ERROR: Mixed dimensions for same quantity/meter combination is not allowed "
      + "(have "
      + "1 J, got 1 m^3)\n"
      + "  Where: PL/pgSQL function ensure_no_mixed_dimensions() line 16 at RAISE";

    DataIntegrityViolationException exc = newDataIntegrityViolationException(message);

    assertThat(SqlErrorMapper.mapDataIntegrityViolation(exc))
      .isInstanceOf(MixedDimensionForMeterQuantity.class);
  }

  @Test
  public void mapsDataIntegrityViolationException_mixedDimensionError() {
    String message = "ERROR: Mixed dimensions for same quantity/meter combination is not allowed "
      + "(have 1 J, got 1 m^3).*";
    DataIntegrityViolationException exc = newDataIntegrityViolationException(message);

    assertThat(SqlErrorMapper.mapDataIntegrityViolation(exc))
      .isInstanceOf(MixedDimensionForMeterQuantity.class);
  }

  @Test
  public void mapsDataIntegrityViolationException_scalingError() {
    String message = "ERROR: dimension mismatch in \"@\" operation: \"285.589999999999975 "
      + "K\", \"3.59999999999999964 MJ\"";
    DataIntegrityViolationException exc = newDataIntegrityViolationException(message);

    assertThat(SqlErrorMapper.mapDataIntegrityViolation(exc))
      .isInstanceOf(UnitConversionError.class);
  }

  @Test
  public void mapsDataIntegrityViolationException_unknownUnitError() {
    String message = "ERROR:  unit \"Whatever\" is not known";
    DataIntegrityViolationException exc = newDataIntegrityViolationException(message);

    assertThat(SqlErrorMapper.mapDataIntegrityViolation(exc))
      .isInstanceOf(UnitConversionError.class);
  }

  private DataIntegrityViolationException newDataIntegrityViolationException(
    String sqlExceptionMessage
  ) {
    return new DataIntegrityViolationException(
      "test",
      new ConstraintViolationException("test", new SQLException(sqlExceptionMessage), "test")
    );
  }
}
