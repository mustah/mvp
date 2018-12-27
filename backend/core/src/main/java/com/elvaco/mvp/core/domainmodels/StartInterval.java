package com.elvaco.mvp.core.domainmodels;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

@FunctionalInterface
public interface StartInterval {

  OffsetDateTime getStart(ZonedDateTime zonedDateTime);
}
