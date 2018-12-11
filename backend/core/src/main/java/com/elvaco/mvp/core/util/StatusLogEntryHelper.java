package com.elvaco.mvp.core.util;

import java.util.List;

import com.elvaco.mvp.core.domainmodels.StatusLogEntry;

import lombok.experimental.UtilityClass;

import static com.elvaco.mvp.core.util.CollectionUtils.isNotEmpty;
import static java.util.stream.Collectors.toList;

@UtilityClass
public class StatusLogEntryHelper {

  public static <T> List<StatusLogEntry> replaceActiveStatus(
    List<StatusLogEntry> currentStatuses,
    StatusLogEntry newActiveStatus
  ) {
    List<StatusLogEntry> activeSameStatuses = currentStatuses.stream()
      .filter(StatusLogEntry::isActive)
      .filter(logEntry -> logEntry.status == newActiveStatus.status)
      .collect(toList());

    if (isNotEmpty(activeSameStatuses)) {
      return currentStatuses;
    }

    List<StatusLogEntry> newStatuses = currentStatuses.stream()
      .map(entry -> entry.isActive()
        ? entry.toBuilder().stop(newActiveStatus.start).build()
        : entry)
      .collect(toList());

    newStatuses.add(newActiveStatus);
    return newStatuses;
  }
}
