package com.elvaco.mvp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.elvaco.mvp.entities.collection.CollectionEntity;

public interface CollectionRepository extends JpaRepository<CollectionEntity, Long> {
}
