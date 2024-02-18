package com.elvaco.mvp.core.domainmodels;

import java.io.Serial;
import java.io.Serializable;
import java.time.ZonedDateTime;
import javax.annotation.Nullable;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class PeriodBound implements Serializable {

  @Serial
  private static final long serialVersionUID = -6358916138824239099L;

  public final boolean isInclusive;

  @Nullable
  public final ZonedDateTime dateTime;

  public PeriodBound(boolean isInclusive, @Nullable ZonedDateTime dateTime) {
    if (dateTime == null) {
      /*Ensure that we mirror the behaviour described in
    https://www.postgresql.org/docs/10/rangetypes.html#RANGETYPES-INFINITE
    to avoid confusion*/
      isInclusive = false;
    }
    this.isInclusive = isInclusive;
    this.dateTime = dateTime;
  }

  public static PeriodBound inclusiveOf(@Nullable ZonedDateTime dateTime) {
    return new PeriodBound(true, dateTime);
  }

  public static PeriodBound exclusiveOf(@Nullable ZonedDateTime dateTime) {
    return new PeriodBound(false, dateTime);
  }

  public static PeriodBound unboundedExclusive() {
    return new PeriodBound(false, null);
  }

}
