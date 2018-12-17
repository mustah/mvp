package com.elvaco.mvp.database.repository.jooq;

import java.time.OffsetDateTime;

import com.elvaco.mvp.core.domainmodels.PeriodRange;

import lombok.experimental.UtilityClass;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.impl.DSL;

import static org.jooq.impl.DSL.val;

@UtilityClass
public class CustomConditions {
  public static <T extends Comparable<PeriodRange>> Condition periodContains(
    Field<PeriodRange> f1,
    OffsetDateTime e
  ) {
    return DSL.condition("range_contains_elem({0}, {1})", f1, val(e));
  }

  public static <T extends Comparable<PeriodRange>> Condition periodOverlaps(
    Field<PeriodRange> f1,
    PeriodRange range
  ) {
    return DSL.condition("range_overlaps({0}, {1})", f1, range);
  }
}
