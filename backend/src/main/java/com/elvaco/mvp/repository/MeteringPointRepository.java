package com.elvaco.mvp.repository;

import com.elvaco.mvp.dto.propertycollection.PropertyCollectionDto;
import com.elvaco.mvp.entity.meteringpoint.MeteringPointEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface MeteringPointRepository extends JpaRepository<MeteringPointEntity, Long> {

  List<MeteringPointEntity> containsInPropertyCollection(PropertyCollectionDto requestModel);

  /**
   * Get all {@link MeteringPointEntity}s that has the given fieldName as a top level property.
   *
   * @param fieldName is the top-level json field name.
   * @return a list of entities that has <code>fieldName</code> in the top-level, otherwise an
   *     empty list.
   */
  List<MeteringPointEntity> existsInPropertyCollection(String fieldName);
}
