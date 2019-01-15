package com.elvaco.mvp.database.repository.jpa;

import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.dsl.PathBuilder;
import org.jooq.Param;
import org.jooq.Query;
import org.springframework.data.jpa.repository.support.JpaMetamodelEntityInformation;
import org.springframework.data.jpa.repository.support.Querydsl;
import org.springframework.data.jpa.repository.support.QuerydslJpaRepository;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
abstract class BaseQueryDslRepository<T, I extends Serializable>
  extends QuerydslJpaRepository<T, I> {

  protected final EntityManager entityManager;
  protected final EntityPath<T> path;
  protected final Querydsl querydsl;

  private final Class<T> entityClass;

  protected BaseQueryDslRepository(EntityManager entityManager, Class<T> entityClass) {
    super(
      new JpaMetamodelEntityInformation<>(entityClass, entityManager.getMetamodel()),
      entityManager
    );
    this.entityManager = entityManager;
    this.path = SimpleEntityPathResolver.INSTANCE.createPath(entityClass);
    this.querydsl = new Querydsl(
      entityManager,
      new PathBuilder<>(path.getType(), path.getMetadata())
    );
    this.entityClass = entityClass;
  }

  List<T> nativeQuery(Query query) {
    return nativeQuery(query, entityClass);
  }

  private <E> List<E> nativeQuery(Query query, Class<E> type) {
    var nativeQuery = entityManager.createNativeQuery(query.getSQL(), type);

    int i = 0;
    for (Param<?> param : query.getParams().values()) {
      if (!param.isInline()) {
        nativeQuery.setParameter(i + 1, convertToDatabaseType(param));
        i++;
      }
    }

    return nativeQuery.getResultList();
  }

  private static <T> Object convertToDatabaseType(Param<T> param) {
    return param.getBinding().converter().to(param.getValue());
  }
}
