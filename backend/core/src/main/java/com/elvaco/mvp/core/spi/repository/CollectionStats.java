package com.elvaco.mvp.core.spi.repository;

import java.util.List;

import com.elvaco.mvp.core.dto.CollectionStatsDto;
import com.elvaco.mvp.core.dto.CollectionStatsPerDateDto;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.spi.data.Pageable;
import com.elvaco.mvp.core.spi.data.RequestParameters;

public interface CollectionStats {

  List<CollectionStatsDto> findAll(RequestParameters parameters, Pageable pageable);

  Page<CollectionStatsDto> findAllPaged(RequestParameters parameters, Pageable pageable);

  List<CollectionStatsPerDateDto> findAllPerDate(RequestParameters parameters);
}
