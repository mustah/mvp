package com.elvaco.mvp.testing.fixture;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.User;

import static com.elvaco.mvp.core.domainmodels.Role.ADMIN;
import static java.util.Collections.singletonList;

public final class UserTestData {

  public static final Organisation DAILY_PLANET =
    new Organisation(
      3L,
      "Daily Planet",
      "daily-planet"
    );

  public static final User CLARK_KENT = new User(
    "Clark Kent",
    "clark@dailyplanet.org",
    "KalEl",
    DAILY_PLANET,
    singletonList(ADMIN)
  );

  private UserTestData() {}
}
