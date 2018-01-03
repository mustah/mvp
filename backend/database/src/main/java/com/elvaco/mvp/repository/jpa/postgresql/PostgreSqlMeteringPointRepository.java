package com.elvaco.mvp.repository.jpa.postgresql;

import java.util.List;
import javax.persistence.EntityManager;

import com.elvaco.mvp.dto.propertycollection.PropertyCollectionDto;
import com.elvaco.mvp.entity.meteringpoint.MeteringPointEntity;
import com.elvaco.mvp.repository.jpa.MeteringPointBaseRepository;

import static com.elvaco.mvp.util.Json.toJson;

public class PostgreSqlMeteringPointRepository extends MeteringPointBaseRepository {

  public PostgreSqlMeteringPointRepository(EntityManager entityManager) {
    super(entityManager);
  }

  @Override
  public List<MeteringPointEntity> containsInPropertyCollection(
    PropertyCollectionDto requestModel
  ) {
    String sqlString = "SELECT * FROM mps WHERE jsonb_contains(property_collection, CAST(:json AS"
                       + " jsonb))";
    return (List<MeteringPointEntity>) entityManager
      .createNativeQuery(sqlString, getDomainClass())
      .setParameter("json", toJson(requestModel))
      .getResultList();
  }

  @Override
  public List<MeteringPointEntity> existsInPropertyCollection(String fieldName) {
    String sqlString = "SELECT * FROM mps WHERE jsonb_exists(property_collection, :fieldName)";
    return (List<MeteringPointEntity>) entityManager
      .createNativeQuery(sqlString, getDomainClass())
      .setParameter("fieldName", fieldName)
      .getResultList();
  }
}
