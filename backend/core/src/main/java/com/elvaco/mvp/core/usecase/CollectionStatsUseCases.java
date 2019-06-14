package com.elvaco.mvp.core.usecase;

import java.util.List;

import com.elvaco.mvp.core.dto.CollectionStatsDto;
import com.elvaco.mvp.core.dto.CollectionStatsPerDateDto;
import com.elvaco.mvp.core.security.AuthenticatedUser;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.Pageable;
import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.core.spi.repository.CollectionStats;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CollectionStatsUseCases {

  private final AuthenticatedUser currentUser;
  private final CollectionStats collectionStats;

  public List<CollectionStatsDto> findAll(RequestParameters parameters, Pageable pageable) {
    return collectionStats.findAll(parameters.ensureOrganisationFilters(currentUser), pageable);
  }

  public Page<CollectionStatsDto> findAllPaged(RequestParameters parameters, Pageable pageable) {
    return collectionStats.findAllPaged(
      parameters.ensureOrganisationFilters(currentUser),
      pageable
    );
  }

  public List<CollectionStatsPerDateDto> findAllPerDate(RequestParameters parameters) {
    return collectionStats.findAllPerDate(parameters.ensureOrganisationFilters(currentUser));
  }
}
