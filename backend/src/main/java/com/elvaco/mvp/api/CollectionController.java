package com.elvaco.mvp.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.elvaco.mvp.entities.collection.CollectionEntity;
import com.elvaco.mvp.repositories.CollectionRepository;

@RestController
@RequestMapping("/api")
public class CollectionController {

  private final CollectionRepository collectionRepository;

  @Autowired
  public CollectionController(CollectionRepository collectionRepository) {
    this.collectionRepository = collectionRepository;
  }

  @RequestMapping("/collections")
  public List<CollectionEntity> collections() {
    return collectionRepository.findAll();
  }
}
