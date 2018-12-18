package com.elvaco.mvp.database.repository.jooq;

import lombok.experimental.UtilityClass;
import org.jooq.Field;

import static org.jooq.impl.DSL.field;

@UtilityClass
public class JooqUtils {

  public static final Field<Long> MISSING_MEASUREMENT_COUNT = field(
    "missing_measurement_count",
    Long.class
  );

}
