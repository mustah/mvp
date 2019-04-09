package com.elvaco.mvp.core.domainmodels;

import java.time.ZonedDateTime;

@FunctionalInterface
public interface StartInterval {

  ZonedDateTime getStart(ZonedDateTime zonedDateTime);
}
