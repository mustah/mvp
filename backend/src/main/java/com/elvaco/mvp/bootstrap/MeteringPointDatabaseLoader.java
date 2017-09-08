package com.elvaco.mvp.bootstrap;

import com.elvaco.mvp.config.InMemory;
import com.elvaco.mvp.entity.meteringpoint.MvpPropertyCollection;
import com.elvaco.mvp.entity.meteringpoint.MeteringPointEntity;
import com.elvaco.mvp.repository.MeteringPointRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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
          mp.propertyCollection = new MvpPropertyCollection();
          mp.propertyCollection.put("foo", "bar");
          mp.propertyCollection.put("baz", "bop");
          break;
        case "5":
          mp.status = 300;
          mp.message = "Failed to read meter.";
          mp.propertyCollection = new MvpPropertyCollection();
          List<Integer> intList = new ArrayList<>();
          intList.add(12);
          intList.add(9999);

          mp.propertyCollection.put("numbers", intList);
          break;
        default:
          mp.message = "";
      }
      repository.save(mp);
    });
  }
}
