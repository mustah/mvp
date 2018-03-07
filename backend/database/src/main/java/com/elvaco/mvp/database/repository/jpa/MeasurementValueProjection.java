package com.elvaco.mvp.database.repository.jpa;

import java.util.Date;


public interface MeasurementValueProjection {

  Date getWhen();

  Double getValue();
}
