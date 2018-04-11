package com.elvaco.mvp.core.util;

import java.util.List;
import java.util.stream.Collectors;

import com.elvaco.mvp.core.domainmodels.StatusLogEntry;
import lombok.experimental.UtilityClass;

@UtilityClass
public class StatusLogEntryHelper {

  public static <T> List<StatusLogEntry<T>> replaceActiveStatus(
    List<StatusLogEntry<T>> currentStatuses,
    StatusLogEntry<T> newActiveStatus
  ) {
    List<StatusLogEntry<T>> activeSameStatuses = currentStatuses.stream()
      .filter(StatusLogEntry::isActive)
      .filter(logEntry -> logEntry.status.equals(newActiveStatus.status))
      .collect(Collectors.toList());

    if (activeSameStatuses.size() > 0) {
      return currentStatuses;
    }

    List<StatusLogEntry<T>> newStatuses = currentStatuses.stream()
      .map(entry -> entry.isActive() ? entry.withStop(newActiveStatus.start) : entry)
      .collect(Collectors.toList());

    newStatuses.add(newActiveStatus);
    return newStatuses;
  }
}
