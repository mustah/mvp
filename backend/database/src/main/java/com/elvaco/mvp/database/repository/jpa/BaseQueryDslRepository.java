package com.elvaco.mvp.database.repository.jpa;

import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.JPQLQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.JpaMetamodelEntityInformation;
import org.springframework.data.jpa.repository.support.QueryDslJpaRepository;
import org.springframework.data.jpa.repository.support.Querydsl;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.repository.NoRepositoryBean;

import static org.springframework.data.repository.support.PageableExecutionUtils.getPage;

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

  protected Page<String> findDistinctProperties(
    Path<String> propertyPath,
    Predicate predicate,
    Pageable pageable
  ) {
    JPQLQuery<String> query = createQuery(predicate).select(propertyPath).distinct();
    JPQLQuery<String> countQuery = createCountQuery(predicate).select(propertyPath).distinct();
    List<String> all = querydsl.applyPagination(pageable, query).fetch();
    return getPage(all, pageable, countQuery::fetchCount);
  }
}
