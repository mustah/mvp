package com.elvaco.mvp.repository.inmemory;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.persistence.EntityManager;

import com.elvaco.mvp.config.InMemory;
import com.elvaco.mvp.dto.propertycollection.PropertyCollectionDto;
import com.elvaco.mvp.dto.propertycollection.UserPropertyDto;
import com.elvaco.mvp.entity.meteringpoint.MeteringPointEntity;
import com.elvaco.mvp.repository.MeteringPointBaseRepository;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import static java.util.stream.Collectors.toList;

@InMemory
@Repository
public class InMemoryMeteringPointRepository extends MeteringPointBaseRepository {

  @Autowired
  public InMemoryMeteringPointRepository(EntityManager entityManager) {
    super(entityManager);
  }

  @Override
  public List<MeteringPointEntity> containsInPropertyCollection(PropertyCollectionDto
                                                                  requestModel) {
    return findAllWithPropertyCollections()
      .filter(containsJson(requestModel))
      .collect(toList());
  }

  @Override
  public List<MeteringPointEntity> existsInPropertyCollection(String fieldName) {
    return findAllWithPropertyCollections()
      .filter(hasAtTopLevel(fieldName))
      .collect(toList());
  }

  private Stream<MeteringPointEntity> findAllWithPropertyCollections() {
    return findAll()
      .stream()
      .filter(this::hasPropertyCollection);
  }

  private Predicate<MeteringPointEntity> hasAtTopLevel(String fieldName) {
    return entity -> entity.propertyCollection.getJson().findValue(fieldName) != null;
  }

  private boolean hasPropertyCollection(MeteringPointEntity meteringPointEntity) {
    return meteringPointEntity.propertyCollection != null;
  }

  // TODO[!must!] add matcher of system part of the request model
  private Predicate<MeteringPointEntity> containsJson(PropertyCollectionDto requestModel) {
    return entity -> allPropertiesMatch(requestModel.user, entity.propertyCollection.getJson());
  }

  private boolean allPropertiesMatch(UserPropertyDto user, ObjectNode objectNode) {
    boolean allMatch = false;
    if (user != null) {
      if (user.externalId != null) {
        allMatch = objectNode.findValue("externalId").asText().equals(user.externalId);
      }
      if (user.project != null) {
        allMatch = objectNode.findValue("project").asText().equals(user.project);
      }
    }
    return allMatch;
  }
}
