package com.elvaco.mvp.meteringpoint;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class MeteringPointDatabaseLoader implements CommandLineRunner {

  private final MeteringPointRepository repository;

  @Autowired
  public MeteringPointDatabaseLoader(MeteringPointRepository repository) {
    this.repository = repository;
  }

  @Override
  public void run(String... args) throws Exception {
    List<MeteringPoint> mps = new ArrayList<>();
    mps.add(new MeteringPoint("1"));
    mps.add(new MeteringPoint("2"));
    mps.add(new MeteringPoint("3"));
    mps.add(new MeteringPoint("4"));
    mps.add(new MeteringPoint("5"));
    mps.add(new MeteringPoint("6"));
    mps.add(new MeteringPoint("7"));
    mps.add(new MeteringPoint("8"));
    mps.add(new MeteringPoint("9"));
    mps.add(new MeteringPoint("10"));

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
