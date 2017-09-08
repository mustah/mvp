package com.elvaco.mvp.bootstrap;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.elvaco.mvp.config.InMemory;
import com.elvaco.mvp.entity.meteringpoint.MeteringPointEntity;
import com.elvaco.mvp.repository.MeteringPointRepository;

@InMemory
@Component
public class MeteringPointDatabaseLoader implements CommandLineRunner {

  private final MeteringPointRepository repository;

  @Autowired
  public MeteringPointDatabaseLoader(MeteringPointRepository repository) {
    this.repository = repository;
  }

  @Override
  public void run(String... args) throws Exception {
    List<MeteringPointEntity> mps = new ArrayList<>();
    mps.add(new MeteringPointEntity("1"));
    mps.add(new MeteringPointEntity("2"));
    mps.add(new MeteringPointEntity("3"));
    mps.add(new MeteringPointEntity("4"));
    mps.add(new MeteringPointEntity("5"));
    mps.add(new MeteringPointEntity("6"));
    mps.add(new MeteringPointEntity("7"));
    mps.add(new MeteringPointEntity("8"));
    mps.add(new MeteringPointEntity("9"));
    mps.add(new MeteringPointEntity("10"));

    mps.forEach(mp -> {
      switch (mp.moid) {
        case "3":
          mp.status = 200;
          mp.message = "Low battery.";
          mp.latitude = 57.505267;
          mp.longitude = 12.069423;
          break;
        case "5":
          mp.status = 300;
          mp.message = "Failed to read meter.";
          mp.latitude = 57.49893;
          mp.longitude = 12.071531;
          break;
        default:
          mp.message = "";
          mp.latitude = 57.505267;
          mp.longitude = 12.069423;
      }
      repository.save(mp);
    });
  }
}