package com.elvaco.mvp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import com.elvaco.mvp.dto.propertycollection.PropertyCollectionDTO;
import com.elvaco.mvp.entity.meteringpoint.MeteringPointEntity;

@NoRepositoryBean
public interface MeteringPointRepository extends JpaRepository<MeteringPointEntity, Long> {

  MeteringPointEntity findByMoid(String moid);

  List<MeteringPointEntity> containsInPropertyCollection(PropertyCollectionDTO requestModel);

  /**
   * @param fieldName is the top-level json field name.
   *
   * @return a list of entities that has <code>fieldName</code> in the top-level, otherwise an empty list.
   */
  List<MeteringPointEntity> existsInPropertyCollection(String fieldName);
}
