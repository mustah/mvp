package com.elvaco.mvp.testing.repository;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.Theme;
import com.elvaco.mvp.core.spi.repository.OrganisationThemes;

public class MockOrganisationThemes implements OrganisationThemes {

  @Override
  public Theme save(Theme theme) {
    return theme;
  }

  @Override
  public Theme findByOrganisation(Organisation organisation) {
    return null;
  }

  @Override
  public void deleteThemeForOrganisation(Organisation organisation) {}
}
