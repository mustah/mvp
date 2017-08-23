package com.elvaco.mvp.meteringpoint;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * @author petjan
 */
@RepositoryRestResource
public interface MeteringPointRepository extends JpaRepository<MeteringPoint, Long> {

  MeteringPoint findByMoid(@Param("moid") String moid);
}
