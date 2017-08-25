package com.elvaco.mvp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.elvaco.mvp.entities.validation.ValidationEntity;

public interface ValidationRepository extends JpaRepository<ValidationEntity, Long> {
}
