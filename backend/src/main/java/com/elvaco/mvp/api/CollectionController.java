package com.elvaco.mvp.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

import com.elvaco.mvp.entities.collection.CollectionEntity;
import com.elvaco.mvp.repositories.CollectionRepository;

@RestApi
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
