package com.elvaco.mvp.api;

import java.util.List;

import com.elvaco.mvp.entity.collection.CollectionEntity;
import com.elvaco.mvp.repository.CollectionRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

@RestApi("/api/collections")
public class CollectionController {

  private final CollectionRepository collectionRepository;

  @Autowired
  public CollectionController(CollectionRepository collectionRepository) {
    this.collectionRepository = collectionRepository;
  }

  @RequestMapping
  public List<CollectionEntity> collections() {
    return collectionRepository.findAll();
  }
}
