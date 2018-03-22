package com.elvaco.mvp.database.repository.jpa;

import java.io.Serializable;
import javax.persistence.EntityManager;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.dsl.PathBuilder;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.QueryDslJpaRepository;
import org.springframework.data.jpa.repository.support.Querydsl;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
abstract class BaseQueryDslRepository<T, I extends Serializable>
  extends QueryDslJpaRepository<T, I> {

  final EntityManager entityManager;
  final EntityPath<T> path;
  final Querydsl querydsl;

  BaseQueryDslRepository(
    JpaEntityInformation<T, I> entityInformation,
    EntityManager entityManager
  ) {
    super(entityInformation, entityManager);
    this.entityManager = entityManager;
    this.path = SimpleEntityPathResolver.INSTANCE.createPath(entityInformation.getJavaType());
    this.querydsl = new Querydsl(
      entityManager,
      new PathBuilder<>(path.getType(), path.getMetadata())
    );
  }
}
