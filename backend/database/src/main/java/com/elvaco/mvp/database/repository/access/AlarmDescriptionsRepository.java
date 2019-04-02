package com.elvaco.mvp.database.repository.access;

import java.util.Optional;
import javax.annotation.Nullable;

import com.elvaco.mvp.core.spi.repository.AlarmDescriptions;
import com.elvaco.mvp.database.repository.jooq.AlarmDescriptionsJooqRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AlarmDescriptionsRepository implements AlarmDescriptions {
  private final AlarmDescriptionsJooqRepository alarmDescriptionsJooqRepository;

  @Override
  public Optional<String> descriptionFor(
    String manufacturer, @Nullable Integer deviceType, @Nullable Integer firmwareRevision, int mask
  ) {
    if (Integer.bitCount(mask) != 1) {
      throw new IllegalArgumentException(String.format(
        "Invalid mask '%d', exactly one bit should be set",
        mask
      ));
    }

    if (deviceType == null || firmwareRevision == null || manufacturer == null) {
      return Optional.empty();
    }

    return alarmDescriptionsJooqRepository.descriptionFor(
      manufacturer,
      deviceType,
      firmwareRevision,
      mask
    );
  }
}
