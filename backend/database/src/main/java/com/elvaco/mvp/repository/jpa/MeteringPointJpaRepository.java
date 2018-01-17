package com.elvaco.mvp.repository.jpa;

import java.util.List;
import javax.persistence.EntityManager;

import com.elvaco.mvp.dto.propertycollection.PropertyCollectionDto;
import com.elvaco.mvp.entity.meteringpoint.MeteringPointEntity;
import com.elvaco.mvp.entity.meteringpoint.PropertyCollection;
import com.elvaco.mvp.entity.meteringpoint.QMeteringPointEntity;
import com.elvaco.mvp.util.Json;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaMetamodelEntityInformation;
import org.springframework.data.jpa.repository.support.QueryDslJpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public class MeteringPointJpaRepository extends
    QueryDslJpaRepository<MeteringPointEntity, Long> {

  private final EntityManager entityManager;

  @Autowired
  MeteringPointJpaRepository(EntityManager entityManager) {
    this(
        new JpaMetamodelEntityInformation<>(
            MeteringPointEntity.class,
            entityManager.getMetamodel()),
        entityManager
    );
  }

  private MeteringPointJpaRepository(
      JpaEntityInformation<MeteringPointEntity, Long> entityInformation,
      EntityManager entityManager
  ) {
    super(entityInformation, entityManager);
    this.entityManager = entityManager;
  }

  public List<MeteringPointEntity> containsInPropertyCollection(
      PropertyCollectionDto requestModel
  ) {
    JPQLQuery<MeteringPointEntity> query = new JPAQuery<>(entityManager);
    QMeteringPointEntity queryMeteringPoint = QMeteringPointEntity.meteringPointEntity;
    PropertyCollection propertyCollection = new PropertyCollection();
    if (requestModel.user != null) {
      propertyCollection.put("user", Json.toJsonNode(requestModel.user));
    }

    if (requestModel.system != null) {
      propertyCollection.put("system", Json.toJsonNode(requestModel.system));
    }

    Predicate predicate = Expressions
        .booleanTemplate("jsonb_contains({0}, {1})",
            queryMeteringPoint.propertyCollection,
            propertyCollection).eq(true);
    query.from(queryMeteringPoint).where(predicate);
    return query.fetch();
  }

  /**
   * Get all {@link MeteringPointEntity}s that has the given fieldName as a top level property.
   *
   * @param fieldName is the top-level json field name.
   * @return a list of entities that has <code>fieldName</code> in the top-level, otherwise an
   *   empty list.
   */
  public List<MeteringPointEntity> existsInPropertyCollection(String fieldName) {
    JPQLQuery<MeteringPointEntity> query = new JPAQuery<>(entityManager);
    QMeteringPointEntity queryMeteringPoint = QMeteringPointEntity.meteringPointEntity;
    Predicate predicate = Expressions
        .booleanTemplate("jsonb_exists({0}, {1})",
            queryMeteringPoint.propertyCollection, fieldName).eq(true);
    query.from(queryMeteringPoint).where(predicate);
    return query.fetch();
  }
}
