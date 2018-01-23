package com.elvaco.mvp.api;

import java.util.List;

import com.elvaco.mvp.dto.propertycollection.PropertyCollectionDto;
import com.elvaco.mvp.entity.meteringpoint.MeteringPointEntity;
import com.elvaco.mvp.repository.jpa.MeteringPointRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestApi("/v1/api/mps")
public class MeteringPointController {

  private final MeteringPointRepository meteringPointRepository;

  @Autowired
  MeteringPointController(MeteringPointRepository meteringPointRepository) {
    this.meteringPointRepository = meteringPointRepository;
  }

  @GetMapping("{id}")
  public MeteringPointEntity meteringPoint(@PathVariable Long id) {
    return meteringPointRepository.findOne(id);
  }

  @GetMapping
  public List<MeteringPointEntity> meteringPoints() {
    return meteringPointRepository.findAll();
  }

  @PostMapping(value = "/property-collections")
  public List<MeteringPointEntity> containsInPropertyCollections(
    @RequestBody PropertyCollectionDto requestModel
  ) {
    return meteringPointRepository.containsInPropertyCollection(requestModel);
  }
}
