package com.elvaco.mvp.database.repository.jooq;

import java.time.OffsetDateTime;

import com.elvaco.mvp.core.domainmodels.PeriodRange;

import lombok.experimental.UtilityClass;
import org.jooq.Condition;
import org.jooq.Field;

import static org.jooq.impl.DSL.condition;
import static org.jooq.impl.DSL.val;

@UtilityClass
public class CustomConditions {

  public static Condition periodContains(Field<PeriodRange> field, OffsetDateTime time) {
    return condition("range_contains_elem({0}, {1})", field, val(time));
  }

  public static Condition periodOverlaps(Field<PeriodRange> field, PeriodRange range) {
    return condition("range_overlaps({0}, {1})", field, range);
  }
}
