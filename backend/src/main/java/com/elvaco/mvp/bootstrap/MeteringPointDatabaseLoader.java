package com.elvaco.mvp.bootstrap;

import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.elvaco.mvp.dto.propertycollection.UserPropertyDTO;
import com.elvaco.mvp.entity.meteringpoint.MeteringPointEntity;
import com.elvaco.mvp.entity.meteringpoint.MvpPropertyCollection;
import com.elvaco.mvp.repository.MeteringPointRepository;

import static java.util.Arrays.asList;

@Component
public class MeteringPointDatabaseLoader implements CommandLineRunner {

  private final MeteringPointRepository repository;

  @Autowired
  public MeteringPointDatabaseLoader(MeteringPointRepository repository) {
    this.repository = repository;
  }

  @Override
  public void run(String... args) throws Exception {
    Stream.of(
      new MeteringPointEntity("1"),
      new MeteringPointEntity("2"),
      new MeteringPointEntity("3"),
      new MeteringPointEntity("4"),
      new MeteringPointEntity("5"),
      new MeteringPointEntity("6"),
      new MeteringPointEntity("7"),
      new MeteringPointEntity("8"),
      new MeteringPointEntity("9"),
      new MeteringPointEntity("10")
    )
      .forEach(mp -> {
        switch (mp.moid) {
          case "3":
            mp.status = 200;
            mp.message = "Low battery.";
            mp.propertyCollection = new MvpPropertyCollection()
              .put("user", new UserPropertyDTO("abc123", "Under construction"));
            break;
          case "5":
            mp.status = 300;
            mp.message = "Failed to read meter.";
            mp.propertyCollection = new MvpPropertyCollection()
              .put("user", new UserPropertyDTO("123123", "Building under construction"))
              .putArray("numbers", asList(1, 2, 3, 17));
            break;
          default:
            mp.message = "";
        }
        repository.save(mp);
      });
  }
}
