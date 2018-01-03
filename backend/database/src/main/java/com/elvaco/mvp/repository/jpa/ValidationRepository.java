package com.elvaco.mvp.repository.jpa;

import com.elvaco.mvp.entity.validation.ValidationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ValidationRepository extends JpaRepository<ValidationEntity, Long> {
}
