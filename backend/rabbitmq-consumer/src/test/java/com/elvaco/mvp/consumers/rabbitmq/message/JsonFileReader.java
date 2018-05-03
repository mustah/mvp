package com.elvaco.mvp.consumers.rabbitmq.message;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

final class JsonFileReader {

  private JsonFileReader() {}

  static String parseJsonFile(String file) {
    return new JsonParser()
      .parse(new JsonReader(new InputStreamReader(loadResource(file), Charset.forName("UTF-8"))))
      .getAsJsonObject()
      .toString();
  }

  private static InputStream loadResource(String file) {
    return JsonFileReader.class.getClassLoader().getResourceAsStream(file);
  }
}
