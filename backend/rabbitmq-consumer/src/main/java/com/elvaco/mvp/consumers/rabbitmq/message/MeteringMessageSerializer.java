package com.elvaco.mvp.consumers.rabbitmq.message;

import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMessageDto;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MeteringMessageSerializer {

  private static final Gson GSON = new GsonBuilder()
    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

  public <T extends MeteringMessageDto> T deserialize(String json, Class<T> classOfT) {
    return GSON.fromJson(json, classOfT);
  }

  public <T extends MeteringMessageDto> String serialize(T obj) {
    return GSON.toJson(obj);
  }
}
