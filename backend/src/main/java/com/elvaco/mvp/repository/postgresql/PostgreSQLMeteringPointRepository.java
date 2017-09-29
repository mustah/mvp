package com.elvaco.mvp.repository.postgresql;

import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import com.elvaco.mvp.dto.properycollection.PropertyCollectionDTO;
import com.elvaco.mvp.entity.meteringpoint.MeteringPointEntity;
import com.elvaco.mvp.repository.MeteringPointBaseRepository;

import static com.elvaco.mvp.utils.Json.toJson;

@Profile("compose")
@Repository
public class PostgreSQLMeteringPointRepository extends MeteringPointBaseRepository {

  @Autowired
  public PostgreSQLMeteringPointRepository(EntityManager entityManager) {
    super(entityManager);
  }

  @Override
  public List<MeteringPointEntity> containsInPropertyCollection(PropertyCollectionDTO requestModel) {
    String sqlString = "SELECT * FROM mps WHERE jsonb_contains(property_collection, CAST(:json AS jsonb))";
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
