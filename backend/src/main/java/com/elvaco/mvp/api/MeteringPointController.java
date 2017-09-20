package com.elvaco.mvp.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.elvaco.mvp.dto.properycollection.PropertyCollectionDTO;
import com.elvaco.mvp.entity.meteringpoint.MeteringPointEntity;
import com.elvaco.mvp.repository.MeteringPointRepository;
import com.fasterxml.jackson.core.JsonProcessingException;

@RestApi
public class MeteringPointController {

  private final MeteringPointRepository repository;

  @Autowired
  MeteringPointController(MeteringPointRepository repository) {
    this.repository = repository;
  }

  @RequestMapping("/mps/{moid}")
  public MeteringPointEntity meteringPoint(@PathVariable String moid) {
    return repository.findByMoid(moid);
  }

  @RequestMapping("/mps")
  public List<MeteringPointEntity> meteringPoints() {
    return repository.findAll();
  }

  @RequestMapping(value = "/mps/external-id", method = RequestMethod.POST)
  public List<MeteringPointEntity> mpsByExternalIds(@RequestBody PropertyCollectionDTO requestModel) throws JsonProcessingException {
    return repository.findByExternalId("{\"user\":{\"externalId\": \"abc\"}}");
  }
}
