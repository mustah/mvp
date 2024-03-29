package com.elvaco.mvp.core.usecase;

import com.elvaco.mvp.core.domainmodels.Address;
import com.elvaco.mvp.core.domainmodels.City;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.Pageable;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.spi.repository.Locations;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LocationUseCases {

  private final AuthenticatedUser currentUser;
  private final Locations locations;

  public Page<City> findAllCities(RequestParameters parameters, Pageable pageable) {
    return locations.findAllCities(parameters.ensureOrganisationFilters(currentUser), pageable);
  }

  public Page<Address> findAllAddresses(RequestParameters parameters, Pageable pageable) {
    return locations.findAllAddresses(parameters.ensureOrganisationFilters(currentUser), pageable);
  }
}
