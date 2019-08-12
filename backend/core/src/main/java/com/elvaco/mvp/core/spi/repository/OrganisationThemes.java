package com.elvaco.mvp.core.spi.repository;

import com.elvaco.mvp.core.domainmodels.Organisation;
import com.elvaco.mvp.core.domainmodels.Theme;

public interface OrganisationThemes {

  Theme save(Theme theme);

  Theme findBy(Organisation organisation);

  void deleteTheme(Organisation organisation);
}
