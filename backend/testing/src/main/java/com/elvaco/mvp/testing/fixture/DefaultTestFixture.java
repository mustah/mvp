package com.elvaco.mvp.testing.fixture;

import com.elvaco.mvp.core.domainmodels.Organisation;

import static com.elvaco.mvp.testing.fixture.OrganisationTestData.ELVACO;

public class DefaultTestFixture implements TestFixtures {
  @Override
  public Organisation defaultOrganisation() {
    return ELVACO;
  }
}
