package com.elvaco.mvp.api;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.elvaco.mvp.core.dto.MapMarkerDto;
import com.elvaco.mvp.core.dto.MeteringPointDto;
import com.elvaco.mvp.core.usecase.MeteringPointsUseCases;
import com.elvaco.mvp.dto.propertycollection.PropertyCollectionDto;
import com.elvaco.mvp.entity.meteringpoint.MeteringPointEntity;
import com.elvaco.mvp.mapper.MeteringPointMapper;
import com.elvaco.mvp.repository.jpa.MeteringPointJpaRepository;
import com.elvaco.mvp.repository.jpa.mappers.FilterToPredicateMapper;
import com.querydsl.core.types.Predicate;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static java.util.Collections.singletonList;

@RestApi("/v1/api/meters")
@ExposesResourceFor(MeteringPointDto.class)
public class MeteringPointController {

  private final MeteringPointJpaRepository meteringPointJpaRepository;
  private final MeteringPointsUseCases meteringPointsUseCases;

  private final ModelMapper modelMapper;
  private final FilterToPredicateMapper predicateMapper;
  private final MeteringPointMapper meteringPointMapper;

  @Autowired
  MeteringPointController(
    MeteringPointJpaRepository meteringPointJpaRepository,
    ModelMapper modelMapper,
    FilterToPredicateMapper predicateMapper,
    MeteringPointMapper meteringPointMapper,
    MeteringPointsUseCases meteringPointsUseCases) {
    this.meteringPointJpaRepository = meteringPointJpaRepository;
    this.modelMapper = modelMapper;
    this.predicateMapper = predicateMapper;
    this.meteringPointMapper = meteringPointMapper;
    this.meteringPointsUseCases = meteringPointsUseCases;
  }

  @GetMapping("{id}")
  public MeteringPointEntity meteringPoint(@PathVariable Long id) {
    return meteringPointJpaRepository.findOne(id);
  }

  @RequestMapping("/map-data")
  public List<MapMarkerDto> meteringPointsForMap() {
    return meteringPointsUseCases.findAll()
      .stream()
      .map(meteringPointMapper::toMapMarkerDto)
      .collect(Collectors.toList());
  }

  @GetMapping
  public Page<MeteringPointDto> meteringPoints(
    @PathVariable Map<String, String> pathVars,
    @RequestParam MultiValueMap<String, String> requestParams,
    Pageable pageable
  ) {
    return filterMeteringPointDtos(combineParams(pathVars, requestParams), pageable);
  }

  @PostMapping(value = "/property-collections")
  public List<MeteringPointEntity> containsInPropertyCollections(
    @RequestBody PropertyCollectionDto requestModel
  ) {
    return meteringPointJpaRepository.containsInPropertyCollection(requestModel);
  }

  private Page<MeteringPointDto> filterMeteringPointDtos(
    Map<String, List<String>> filter,
    Pageable pageable
  ) {
    Predicate predicate = predicateMapper.map(filter);
    return meteringPointJpaRepository
      .findAll(predicate, pageable)
      .map(source -> modelMapper.map(source, MeteringPointDto.class));
  }

  private Map<String, List<String>> combineParams(
    Map<String, String> pathVars,
    Map<String, List<String>> requestParams
  ) {
    Map<String, List<String>> filter = new HashMap<>();
    filter.putAll(requestParams);
    filter.putAll(
      pathVars
        .entrySet()
        .stream()
        .map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), singletonList(e.getValue())))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
    );
    return filter;
  }
}
