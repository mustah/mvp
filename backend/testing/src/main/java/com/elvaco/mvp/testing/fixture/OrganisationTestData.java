package com.elvaco.mvp.testing.fixture;

import com.elvaco.mvp.core.domainmodels.Organisation;

import lombok.experimental.UtilityClass;

@UtilityClass
public class OrganisationTestData {

  public static final Organisation OTHER_ORGANISATION = Organisation.of("Other Organisation");

  public static final Organisation DAILY_PLANET = Organisation.of("Daily Planet");

  public static final Organisation MARVEL = Organisation.of("Marvel");

  public static final Organisation ELVACO = Organisation.of("Elvaco");
}
