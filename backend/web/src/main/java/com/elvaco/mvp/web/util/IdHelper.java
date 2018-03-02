package com.elvaco.mvp.web.util;

import java.util.UUID;
import javax.annotation.Nullable;

import static java.util.UUID.randomUUID;

public final class IdHelper {

  private IdHelper() { }

  public static UUID uuidOf(@Nullable String id) {
    return id != null ? UUID.fromString(id) : randomUUID();
  }
}
