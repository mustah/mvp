package com.elvaco.mvp.database;

import java.util.UUID;

import com.elvaco.mvp.database.entity.property.PropertyEntity;
import com.elvaco.mvp.database.entity.property.PropertyPk;
import com.elvaco.mvp.database.repository.jpa.PropertiesJpaRepository;
import com.elvaco.mvp.testdata.IntegrationTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

public class PropertiesJpaRepositoryTest extends IntegrationTest {

  @Autowired
  private PropertiesJpaRepository propertiesJpaRepository;

  private UUID organisationId;

  @Before
  public void setUp() {
    organisationId = context().getOrganisationId();
  }

  @After
  public void tearDown() {
    propertiesJpaRepository.deleteAll();
  }

  @Test
  public void createNew() {
    PropertyEntity property = new PropertyEntity(
      randomUUID(),
      organisationId,
      "metering.forceUpdate",
      "true"
    );

    propertiesJpaRepository.save(property);

    assertThat(propertiesJpaRepository.findAll()).hasSize(1);
  }

  @Test
  public void createTwoProperties() {
    propertiesJpaRepository.save(asList(
      new PropertyEntity(randomUUID(), organisationId, "metering.forceUpdate", "true"),
      new PropertyEntity(randomUUID(), organisationId, "metering.forceUpdate", "false")
    ));

    assertThat(propertiesJpaRepository.findAll()).hasSize(2);
  }

  @Test
  public void findAllByKey() {
    propertiesJpaRepository.save(asList(
      new PropertyEntity(randomUUID(), organisationId, "metering.forceUpdate", "true"),
      new PropertyEntity(randomUUID(), organisationId, "metering.forceUpdate", "false"),
      new PropertyEntity(randomUUID(), organisationId, "metering.ignore", "false")
    ));

    assertThat(propertiesJpaRepository.findAllById_Key("metering.forceUpdate")).hasSize(2);
  }

  @Test
  public void findAllByKey_CannotBeNull_OnlyEmpty() {
    propertiesJpaRepository.save(asList(
      new PropertyEntity(randomUUID(), organisationId, "metering.forceUpdate", "true"),
      new PropertyEntity(randomUUID(), organisationId, "metering.forceUpdate", "false"),
      new PropertyEntity(randomUUID(), organisationId, "metering.ignore", "false")
    ));

    assertThat(propertiesJpaRepository.findAllById_Key("nonExisting.key")).isEmpty();
  }

  @Test
  public void deleteEntry() {
    String key = "metering.forceUpdate";
    UUID entityId1 = randomUUID();
    UUID entityId3 = randomUUID();
    UUID entityId2 = randomUUID();
    propertiesJpaRepository.save(asList(
      new PropertyEntity(entityId1, organisationId, key, "true"),
      new PropertyEntity(entityId2, organisationId, key, "false"),
      new PropertyEntity(entityId3, organisationId, "metering.ignore", "false")
    ));

    propertiesJpaRepository.delete(new PropertyPk(entityId1, organisationId, key));

    assertThat(propertiesJpaRepository.findAll())
      .containsExactlyInAnyOrder(
        new PropertyEntity(entityId2, organisationId, key, "false"),
        new PropertyEntity(entityId3, organisationId, "metering.ignore", "false")
      );
  }

  @Test
  public void findAndRemove() {
    UUID entityId = randomUUID();
    PropertyPk id = new PropertyPk(entityId, organisationId, "metering.forceUpdate");
    propertiesJpaRepository.save(new PropertyEntity(id, "true"));

    assertThat(propertiesJpaRepository.findById(id).isPresent()).isTrue();

    propertiesJpaRepository.delete(id);

    assertThat(propertiesJpaRepository.findById(id).isPresent()).isFalse();
  }
}
