package com.elvaco.mvp.database.repository.jooq;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import static com.elvaco.mvp.database.entity.jooq.Tables.ALARM_DESCRIPTION;

@Repository
@RequiredArgsConstructor
public class AlarmDescriptionsJooqRepository {
  private final DSLContext dsl;

  public Optional<String> descriptionFor(
    String manufacturer,
    int deviceType,
    int firmwareVersion,
    int mask
  ) {
    return dsl.select(ALARM_DESCRIPTION.DESCRIPTION)
      .from(ALARM_DESCRIPTION)
      .where(ALARM_DESCRIPTION.MANUFACTURER.eq(manufacturer))
      .and(ALARM_DESCRIPTION.FIRMWARE_VERSION.eq(firmwareVersion))
      .and(ALARM_DESCRIPTION.DEVICE_TYPE.eq(deviceType))
      .and(ALARM_DESCRIPTION.MASK.eq(mask))
      .fetchOptional(ALARM_DESCRIPTION.DESCRIPTION);
  }
}
