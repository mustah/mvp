package com.elvaco.mvp.producers.rabbitmq;

import java.time.LocalDateTime;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MessageSerializer {

  private static final Gson GSON = new GsonBuilder()
    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
    .registerTypeAdapter(
      LocalDateTime.class,
      (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) ->
        LocalDateTime.parse(json.getAsString())
    )
    .create();

  public static <T> T fromJson(String json, Class<T> classOfT) {
    return GSON.fromJson(json, classOfT);
  }

  public static String toJson(Object src) {
    return GSON.toJson(src);
  }
}
