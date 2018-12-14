package com.elvaco.mvp.producers.rabbitmq;

import java.lang.reflect.Modifier;
import java.time.LocalDateTime;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MessageSerializer {

  private static final Gson GSON = new GsonBuilder()
    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
    .excludeFieldsWithModifiers(Modifier.TRANSIENT) // override to include static fields
    .registerTypeAdapter(
      LocalDateTime.class,
      (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) ->
        LocalDateTime.parse(json.getAsString())
    )
    .registerTypeAdapter(
      LocalDateTime.class,
      (JsonSerializer<LocalDateTime>) (localDateTime, typeOfT, context) ->
        new JsonPrimitive(localDateTime.toString())
    )
    .create();

  public static <T> T fromJson(String json, Class<T> classOfT) {
    return GSON.fromJson(json, classOfT);
  }

  public static String toJson(Object src) {
    return GSON.toJson(src);
  }
}
