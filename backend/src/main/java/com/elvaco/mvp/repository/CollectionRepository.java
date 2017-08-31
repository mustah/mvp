package com.elvaco.mvp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.elvaco.mvp.entity.collection.CollectionEntity;

public interface CollectionRepository extends JpaRepository<CollectionEntity, Long> {
}
