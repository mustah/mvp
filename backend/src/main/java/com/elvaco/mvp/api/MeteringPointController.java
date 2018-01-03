package com.elvaco.mvp.api;

import java.util.List;

import com.elvaco.mvp.dto.propertycollection.PropertyCollectionDto;
import com.elvaco.mvp.entity.meteringpoint.MeteringPointEntity;
import com.elvaco.mvp.repository.MeteringPointRepository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Slf4j
@RestApi("/api/mps")
public class MeteringPointController {

  private final MeteringPointRepository meteringPointRepository;

  @Autowired
  MeteringPointController(MeteringPointRepository meteringPointRepository) {
    this.meteringPointRepository = meteringPointRepository;
  }

  @RequestMapping("{id}")
  public MeteringPointEntity meteringPoint(@PathVariable Long id) {
    return meteringPointRepository.findOne(id);
  }

  @RequestMapping
  public List<MeteringPointEntity> meteringPoints() {
    return meteringPointRepository.findAll();
  }

  @RequestMapping(value = "/property-collections", method = RequestMethod.POST)
  public List<MeteringPointEntity> containsInPropertyCollections(
    @RequestBody PropertyCollectionDto requestModel
  ) {
    return meteringPointRepository.containsInPropertyCollection(requestModel);
  }
}
