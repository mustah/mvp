package com.elvaco.mvp.repository;

import com.elvaco.mvp.entity.collection.CollectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CollectionRepository extends JpaRepository<CollectionEntity, Long> {
}
