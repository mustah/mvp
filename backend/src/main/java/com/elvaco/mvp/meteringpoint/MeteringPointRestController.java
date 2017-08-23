package com.elvaco.mvp.meteringpoint;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MeteringPointRestController {

  private final MeteringPointRepository repository;

  @Autowired
  MeteringPointRestController(MeteringPointRepository repository) {
    this.repository = repository;
  }

  /**
   * Get metering point object from MOID.
   *
   * @param moid the MOID
   *
   * @return the metering point object if it exists
   */
  @RequestMapping("/mps/{moid}")
  public MeteringPoint meteringPoint(@PathVariable String moid) {
    return repository.findByMoid(moid);
  }

  /**
   * Get a list of all users in system. <p> TODO : Do we need this endpoint when going to production? </p>
   *
   * @return a list of all defined users.
   */
  @RequestMapping("/mps")
  public Collection<MeteringPoint> meteringPoints() {
    return repository.findAll();
  }
}
