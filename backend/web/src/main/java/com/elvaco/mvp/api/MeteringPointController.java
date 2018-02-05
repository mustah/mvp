package com.elvaco.mvp.api;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.elvaco.mvp.core.domainmodels.MeteringPoint;
import com.elvaco.mvp.core.spi.data.Page;
import com.elvaco.mvp.core.usecase.MeteringPointsUseCases;
import com.elvaco.mvp.dto.MapMarkerDto;
import com.elvaco.mvp.dto.MeteringPointDto;
import com.elvaco.mvp.mapper.MeteringPointMapper;
import com.elvaco.mvp.spring.PageableAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import static com.elvaco.mvp.util.ParametersHelper.combineParams;

@RestApi("/v1/api/meters")
public class MeteringPointController {

  private final MeteringPointsUseCases meteringPointsUseCases;
  private final MeteringPointMapper meteringPointMapper;

  @Autowired
  MeteringPointController(
    MeteringPointMapper meteringPointMapper,
    MeteringPointsUseCases meteringPointsUseCases
  ) {
    this.meteringPointMapper = meteringPointMapper;
    this.meteringPointsUseCases = meteringPointsUseCases;
  }

  @GetMapping("{id}")
  public MeteringPointDto meteringPoint(@PathVariable Long id) {
    return meteringPointMapper.toDto(meteringPointsUseCases.findById(id));
  }

  @GetMapping("/map-data")
  public List<MapMarkerDto> meteringPointsForMap() {
    return meteringPointsUseCases.findAll()
      .stream()
      .map(meteringPointMapper::toMapMarkerDto)
      .collect(Collectors.toList());
  }

  @GetMapping
  public org.springframework.data.domain.Page<MeteringPointDto> meteringPoints(
    @PathVariable Map<String, String> pathVars,
    @RequestParam MultiValueMap<String, String> requestParams,
    Pageable pageable
  ) {
    return filterMeteringPointDtos(combineParams(pathVars, requestParams), pageable);
  }

  private org.springframework.data.domain.Page<MeteringPointDto> filterMeteringPointDtos(
    Map<String, List<String>> filter,
    Pageable pageable
  ) {
    PageableAdapter adapter = new PageableAdapter(pageable);
    Page<MeteringPoint> page = meteringPointsUseCases.findAll(filter, adapter);
    return new PageImpl<>(page.getContent(), pageable, page.getTotalElements())
      .map(meteringPointMapper::toDto);
  }
}
