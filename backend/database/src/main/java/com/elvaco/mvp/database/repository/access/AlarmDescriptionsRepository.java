package com.elvaco.mvp.database.repository.access;

import java.util.Map;

import com.elvaco.mvp.core.domainmodels.AlarmDescriptionMbusQuery;
import com.elvaco.mvp.core.domainmodels.AlarmDescriptionQuery;
import com.elvaco.mvp.core.spi.repository.AlarmDescriptions;
import com.elvaco.mvp.database.repository.jooq.AlarmDescriptionsJooqRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AlarmDescriptionsRepository implements AlarmDescriptions {
  private final AlarmDescriptionsJooqRepository alarmDescriptionsJooqRepository;

  @Override
  public String descriptionFor(AlarmDescriptionQuery query) {
    if (Integer.bitCount(query.mask()) != 1) {
      throw new IllegalArgumentException(String.format(
        "Invalid mask '%d', exactly one bit should" + " be set",
        query.mask()
      ));
    }

    if (query.deviceType() == null || query.firmwareVersion() == null || query.manufacturer() == null) {
      return null;
    }

    return alarmDescriptionsJooqRepository.descriptionFor(
      query.manufacturer(),
      query.deviceType(),
      query.firmwareVersion(),
      query.mask()
    ).orElse(null);
  }

  @Override
  public Map<Integer, String> descriptionsFor(AlarmDescriptionMbusQuery query) {
    return alarmDescriptionsJooqRepository.descriptionsFor(
      query.manufacturer(),
      query.deviceType(),
      query.version()
    );
  }
}
