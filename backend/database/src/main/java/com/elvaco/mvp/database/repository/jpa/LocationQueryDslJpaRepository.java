package com.elvaco.mvp.database.repository.jpa;

import java.util.List;
import java.util.UUID;
import javax.persistence.EntityManager;

import com.elvaco.mvp.core.spi.data.RequestParameters;
import com.elvaco.mvp.database.entity.meter.LocationEntity;
import com.elvaco.mvp.database.entity.meter.QLocationEntity;
import com.elvaco.mvp.database.entity.meter.QLogicalMeterEntity;
import com.elvaco.mvp.database.repository.queryfilters.FilterUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.JpaMetamodelEntityInformation;
import org.springframework.stereotype.Repository;

import static org.springframework.data.repository.support.PageableExecutionUtils.getPage;

@Repository
public class LocationQueryDslJpaRepository extends BaseQueryDslRepository<LocationEntity, UUID>
  implements LocationJpaRepository {

  private static final QLocationEntity LOCATION = QLocationEntity.locationEntity;
  private static final QLogicalMeterEntity LOGICAL_METER = QLogicalMeterEntity.logicalMeterEntity;

  @Autowired
  public LocationQueryDslJpaRepository(EntityManager entityManager) {
    super(
      new JpaMetamodelEntityInformation<>(LocationEntity.class, entityManager.getMetamodel()),
      entityManager
    );
  }

  @Override
  public Page<LocationEntity> findAll(
    RequestParameters parameters,
    Predicate predicate,
    Pageable pageable
  ) {

    JPQLQuery<LocationEntity> query = applyJoins(
      createQuery(predicate).select(LOCATION),
      parameters
    );

    List<LocationEntity> all = querydsl.applyPagination(
      pageable,
      query
    ).fetch();

    JPQLQuery<LocationEntity> countQuery = applyJoins(
      createCountQuery(predicate).select(path),
      parameters
    );

    return getPage(all, pageable, countQuery::fetchCount);
  }

  @Override
  public LocationEntity findByLogicalMeterId(UUID logicalMeterId) {
    return createQuery(LOCATION.logicalMeterId.eq(logicalMeterId)).select(path).fetchOne();
  }

  private JPQLQuery<LocationEntity> applyJoins(
    JPQLQuery<LocationEntity> query,
    RequestParameters parameters
  ) {
    if (FilterUtils.isOrganisationQuery(parameters)) {
      query.innerJoin(LOCATION.logicalMeter, LOGICAL_METER);
    }
    return query;
  }
}
