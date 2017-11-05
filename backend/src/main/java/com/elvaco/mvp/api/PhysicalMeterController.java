package com.elvaco.mvp.api;

import com.elvaco.mvp.entity.measurement.MeasurementEntity;
import com.elvaco.mvp.entity.meter.PhysicalMeterEntity;
import com.elvaco.mvp.repository.MeasurementRepository;
import com.elvaco.mvp.repository.PhysicalMeterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@RestApi
@ExposesResourceFor(PhysicalMeterEntity.class)
@RequestMapping("/api/physical-meters")
public class PhysicalMeterController {
  private final PhysicalMeterRepository repository;

  @Autowired
  public PhysicalMeterController(PhysicalMeterRepository repository) {
    this.repository = repository;
  }

  @RequestMapping("{id}")
  public PhysicalMeterEntity physicalMeter(@PathVariable("id") Long id) {
    return repository.findOne(id);
  }

  @RequestMapping("")
  public Page<PhysicalMeterEntity> physicalMeters(Pageable pageable) {
    return repository.findAll(pageable);
  }
}
