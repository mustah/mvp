package com.elvaco.mvp.core.spi.repository;

import java.util.Optional;
import javax.annotation.Nullable;

public interface AlarmDescriptions {
  Optional<String> descriptionFor(
    String manufacturer,
    @Nullable Integer deviceType,
    @Nullable Integer firmwareRevision,
    int mask
  );
}
