package com.elvaco.mvp.database.repository.jpa;

import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;

import org.jooq.Param;
import org.jooq.Query;
import org.springframework.data.jpa.repository.support.JpaMetamodelEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
abstract class BaseJooqRepository<T, I extends Serializable>
  extends SimpleJpaRepository<T, I> {

  protected final EntityManager entityManager;

  private final Class<T> entityClass;

  protected BaseJooqRepository(EntityManager entityManager, Class<T> entityClass) {
    super(
      new JpaMetamodelEntityInformation<>(entityClass, entityManager.getMetamodel()),
      entityManager
    );
    this.entityManager = entityManager;
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
    @SuppressWarnings("unchecked") //Let's hope JPA does the right thing
    List<E> resultList = nativeQuery.getResultList();

    return resultList;
  }

  private static <T> Object convertToDatabaseType(Param<T> param) {
    return param.getBinding().converter().to(param.getValue());
  }
}
