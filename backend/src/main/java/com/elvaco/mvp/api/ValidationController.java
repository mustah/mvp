package com.elvaco.mvp.api;

import com.elvaco.mvp.entity.validation.ValidationEntity;
import com.elvaco.mvp.repository.ValidationRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

@RestApi
public class ValidationController {

  private final ValidationRepository repository;

  @Autowired
  public ValidationController(ValidationRepository repository) {
    this.repository = repository;
  }

  @RequestMapping("/validations")
  public List<ValidationEntity> validations() {
    return repository.findAll();
  }
}
