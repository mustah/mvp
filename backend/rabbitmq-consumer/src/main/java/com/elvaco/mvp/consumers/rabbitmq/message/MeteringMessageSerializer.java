package com.elvaco.mvp.consumers.rabbitmq.message;

import java.time.LocalDateTime;

import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringMessageDto;
import com.elvaco.mvp.consumers.rabbitmq.dto.MeteringResponseDto;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;

public final class MeteringMessageSerializer {

  private static final Gson GSON = new GsonBuilder()
    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
    .registerTypeAdapter(
      LocalDateTime.class,
      (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) ->
        LocalDateTime.parse(json.getAsString())
    ).create();

  public static <T extends MeteringMessageDto> String serialize(T obj) {
    return GSON.toJson(obj);
  }

  public <T extends MeteringResponseDto> String serialize(T obj) {
    return GSON.toJson(obj);
  }
}
