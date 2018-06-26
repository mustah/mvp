package com.elvaco.mvp.database.repository.jpa;

import java.io.Serializable;
import javax.persistence.EntityManager;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.dsl.PathBuilder;
import org.springframework.data.jpa.repository.support.JpaMetamodelEntityInformation;
import org.springframework.data.jpa.repository.support.QueryDslJpaRepository;
import org.springframework.data.jpa.repository.support.Querydsl;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
abstract class BaseQueryDslRepository<T, I extends Serializable>
  extends QueryDslJpaRepository<T, I> {

  protected final EntityManager entityManager;
  protected final EntityPath<T> path;
  protected final Querydsl querydsl;

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
  }
}
