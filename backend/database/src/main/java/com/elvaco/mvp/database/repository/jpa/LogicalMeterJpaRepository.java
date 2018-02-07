package com.elvaco.mvp.database.repository.jpa;

import java.util.List;
import javax.persistence.EntityManager;

import com.elvaco.mvp.database.dto.propertycollection.PropertyCollectionDto;
import com.elvaco.mvp.database.entity.meter.LogicalMeterEntity;
import com.elvaco.mvp.database.entity.meter.PropertyCollection;
import com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity;
import com.elvaco.mvp.database.util.Json;

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
public class LogicalMeterJpaRepository extends QueryDslJpaRepository<LogicalMeterEntity, Long> {

  private final EntityManager entityManager;

  @Autowired
  LogicalMeterJpaRepository(EntityManager entityManager) {
    this(
      new JpaMetamodelEntityInformation<>(
        LogicalMeterEntity.class,
        entityManager.getMetamodel()
      ),
      entityManager
    );
  }

  private LogicalMeterJpaRepository(
    JpaEntityInformation<LogicalMeterEntity, Long> entityInformation,
    EntityManager entityManager
  ) {
    super(entityInformation, entityManager);
    this.entityManager = entityManager;
  }

  public List<LogicalMeterEntity> containsInPropertyCollection(
    PropertyCollectionDto requestModel
  ) {
    JPQLQuery<LogicalMeterEntity> query = new JPAQuery<>(entityManager);
    QLogicalMeterEntity queryLogicalMeter = QLogicalMeterEntity.logicalMeterEntity;
    PropertyCollection propertyCollection = new PropertyCollection();
    if (requestModel.user != null) {
      propertyCollection.put("user", Json.toJsonNode(requestModel.user));
    }

    if (requestModel.system != null) {
      propertyCollection.put("system", Json.toJsonNode(requestModel.system));
    }

    Predicate predicate = Expressions
      .booleanTemplate(
        "jsonb_contains({0}, {1})",
        queryLogicalMeter.propertyCollection,
        propertyCollection
      ).eq(true);
    query.from(queryLogicalMeter).where(predicate);
    return query.fetch();
  }

  /**
   * Get all {@link LogicalMeterEntity}s that has the given fieldName as a top level property.
   *
   * @param fieldName is the top-level json field name.
   * @return a list of entities that has <code>fieldName</code> in the top-level, otherwise an
   * empty list.
   */
  public List<LogicalMeterEntity> existsInPropertyCollection(String fieldName) {
    JPQLQuery<LogicalMeterEntity> query = new JPAQuery<>(entityManager);
    QLogicalMeterEntity queryLogicalMeter = QLogicalMeterEntity.logicalMeterEntity;
    Predicate predicate = Expressions
      .booleanTemplate("jsonb_exists({0}, {1})",
        queryLogicalMeter.propertyCollection, fieldName
      ).eq(true);
    query.from(queryLogicalMeter).where(predicate);
    return query.fetch();
  }
}
