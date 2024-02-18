package com.elvaco.mvp.core.spi.repository;

import java.util.Map;

import javax.annotation.Nullable;

import com.elvaco.mvp.core.domainmodels.AlarmDescriptionMbusQuery;
import com.elvaco.mvp.core.domainmodels.AlarmDescriptionQuery;

public interface AlarmDescriptions {

  @Nullable
  String descriptionFor(AlarmDescriptionQuery query);

  Map<Integer, String> descriptionsFor(AlarmDescriptionMbusQuery query);
}
