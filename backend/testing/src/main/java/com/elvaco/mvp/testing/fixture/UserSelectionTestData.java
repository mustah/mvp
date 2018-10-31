package com.elvaco.mvp.testing.fixture;

import lombok.experimental.UtilityClass;

@UtilityClass
public class UserSelectionTestData {

  public static final String FACILITIES_JSON_STRING =
    "{\"facilities\": [{\"id\": \"demo1\", \"name\": \"demo1\"}, "
      + "{\"id\": \"demo2\", \"name\": \"demo2\"}]}";

  public static final String CITIES_JSON_STRING =
    "{\"cities\": [{\"id\": \"sverige,kungsbacka\", \"name\": \"kungsbacka\", "
      + "\"country\": {\"id\": \"sverige\", \"name\": \"sverige\"}, \"selected\": true}, "
      + "{\"id\": \"sverige,stockholm\", \"name\": \"stockholm\", "
      + "\"country\": {\"id\": \"sverige\", \"name\": \"sverige\"}, \"selected\": true}]}";
}
