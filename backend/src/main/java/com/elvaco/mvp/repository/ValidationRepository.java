package com.elvaco.mvp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.elvaco.mvp.entity.validation.ValidationEntity;

public interface ValidationRepository extends JpaRepository<ValidationEntity, Long> {
}
