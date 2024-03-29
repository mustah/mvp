package com.elvaco.mvp.configuration.bootstrap.demo;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import com.elvaco.mvp.core.usecase.SettingUseCases;
import com.elvaco.mvp.database.entity.meter.JsonField;
import com.elvaco.mvp.database.entity.selection.UserSelectionEntity;
import com.elvaco.mvp.database.repository.jpa.UserJpaRepository;
import com.elvaco.mvp.database.repository.jpa.UserSelectionJpaRepository;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import static com.elvaco.mvp.core.util.Json.OBJECT_MAPPER;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;

@Order(6)
@Profile("demo")
@Component
@Slf4j
@RequiredArgsConstructor
public class UserSelectionDataLoader implements CommandLineRunner {

  private final SettingUseCases settingUseCases;
  private final UserSelectionJpaRepository userSelectionJpaRepository;
  private final UserJpaRepository userJpaRepository;

  @Override
  public void run(String... args) throws Exception {
    if (settingUseCases.isDemoUserSelectionDataLoaded()) {
      log.info("Demo user selections seems to already be loaded - skipping!");
      return;
    }

    log.info("Loading demo user selections");

    createSelections();

    settingUseCases.setDemoUserSelectionsLoaded();
  }

  private void createSelections() throws IOException {
    String unknownJson =
      "{\"cities\":[\"unknown,unknown\"],\"addresses\":[],"
        + "\"alarms\":[],\"manufacturers\":[],"
        + "\"productModels\":[],\"dateRange\":{\"period\":\"latest\"}}";

    JsonField selectionForUnknownCity = new JsonField(
      (ObjectNode) OBJECT_MAPPER.readTree(unknownJson)
    );

    String perstorpJson =
      "{\"cities\":[\"sweden,perstorp\"],\"addresses\":[],"
        + "\"alarms\":[],\"manufacturers\":[],"
        + "\"productModels\":[],\"dateRange\":{\"period\":\"latest\"}}";

    JsonField selectionForPerstorp = new JsonField(
      (ObjectNode) OBJECT_MAPPER.readTree(perstorpJson)
    );

    List<UserSelectionEntity> selectionEntities = userJpaRepository.findAll().stream()
      .flatMap((user) -> Stream.of(
        new UserSelectionEntity(
          randomUUID(),
          user.id,
          "Unknown city",
          selectionForUnknownCity,
          user.organisation.id
        ),
        new UserSelectionEntity(
          randomUUID(),
          user.id,
          "Perstorp",
          selectionForPerstorp,
          user.organisation.id
        )
      ))
      .collect(toList());

    userSelectionJpaRepository.saveAll(selectionEntities);

    log.info("Demo user selections saved: " + selectionEntities.size());
  }
}
