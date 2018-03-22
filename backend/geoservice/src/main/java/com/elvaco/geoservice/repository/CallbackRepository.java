package com.elvaco.geoservice.repository;

import com.elvaco.geoservice.repository.entity.CallbackEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface CallbackRepository extends CrudRepository<CallbackEntity, Long> {
  @Query("from CallbackEntity where nextRetry <= CURRENT_TIMESTAMP order by nextRetry asc")
  public Iterable<CallbackEntity> findByNextRetryBeforeNowOrderByNextRetryAsc();

}
