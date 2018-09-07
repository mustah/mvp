package com.elvaco.mvp.core.spi.repository;

import java.util.Collection;

import com.elvaco.mvp.core.domainmodels.AlarmLogEntry;

public interface MeterAlarmLogs {

  AlarmLogEntry save(AlarmLogEntry alarm);

  void save(Collection<? extends AlarmLogEntry> alarms);
}
