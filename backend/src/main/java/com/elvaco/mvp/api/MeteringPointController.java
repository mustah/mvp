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

@RestApi
@Slf4j
public class MeteringPointController {

  private final MeteringPointRepository repository;

  @Autowired
  MeteringPointController(MeteringPointRepository repository) {
    this.repository = repository;
  }

  @RequestMapping("/mps/{id}")
  public MeteringPointEntity meteringPoint(@PathVariable Long id) {
    return repository.findOne(id);
  }

  @RequestMapping("/mps")
  public List<MeteringPointEntity> meteringPoints() {
    return repository.findAll();
  }

  @RequestMapping(value = "/mps/property-collections", method = RequestMethod.POST)
  public List<MeteringPointEntity> containsInPropertyCollections(
    @RequestBody PropertyCollectionDto requestModel
  ) {
    return repository.containsInPropertyCollection(requestModel);
  }
}
