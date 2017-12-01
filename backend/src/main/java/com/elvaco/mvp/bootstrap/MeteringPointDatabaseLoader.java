package com.elvaco.mvp.bootstrap;

import static java.util.Arrays.asList;

import com.elvaco.mvp.dto.propertycollection.UserPropertyDto;
import com.elvaco.mvp.entity.meteringpoint.MeteringPointEntity;
import com.elvaco.mvp.entity.meteringpoint.PropertyCollection;
import com.elvaco.mvp.repository.MeteringPointRepository;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class MeteringPointDatabaseLoader implements CommandLineRunner {

  private final MeteringPointRepository repository;

  @Autowired
  public MeteringPointDatabaseLoader(MeteringPointRepository repository) {
    this.repository = repository;
  }

  @Override
  public void run(String... args) throws Exception {
    Stream.of(
        new MeteringPointEntity(),
        new MeteringPointEntity(),
        new MeteringPointEntity(),
        new MeteringPointEntity(),
        new MeteringPointEntity(),
        new MeteringPointEntity(),
        new MeteringPointEntity(),
        new MeteringPointEntity(),
        new MeteringPointEntity(),
        new MeteringPointEntity()
    ).forEach(mp -> {
      mp.propertyCollection = new PropertyCollection()
          .put("user", new UserPropertyDto("123123", "Building under construction"))
          .putArray("numbers", asList(1, 2, 3, 17));
      repository.save(mp);
    });
  }
}
