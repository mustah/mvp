package com.elvaco.mvp.core.util;

import java.util.List;
import java.util.Optional;

import com.elvaco.mvp.core.domainmodels.StatusLogEntry;

import lombok.experimental.UtilityClass;

import static java.util.stream.Collectors.toList;

@UtilityClass
public class StatusLogEntryHelper {

  public static List<StatusLogEntry> replaceActiveStatus(
    List<StatusLogEntry> currentStatuses,
    StatusLogEntry newActiveStatus
  ) {
    Optional<StatusLogEntry> sameActiveStatus = currentStatuses.stream()
      .filter(StatusLogEntry::isActive)
      .filter(logEntry -> logEntry.status == newActiveStatus.status)
      .findAny();

    if (sameActiveStatus.isPresent()) {
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
