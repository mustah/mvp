package com.elvaco.mvp.testing.repository;

import com.elvaco.mvp.core.domainmodels.MeterDefinition;
import com.elvaco.mvp.core.domainmodels.MeterDefinitionType;
import com.elvaco.mvp.core.spi.repository.MeterDefinitions;

public class MockMeterDefinitions extends MockRepository<MeterDefinitionType, MeterDefinition>
  implements MeterDefinitions {

  @Override
  public MeterDefinition save(MeterDefinition meterDefinition) {
    return saveMock(meterDefinition);
  }

  @Override
  protected MeterDefinition copyWithId(MeterDefinitionType id, MeterDefinition entity) {
    return null;
  }

  @Override
  protected MeterDefinitionType generateId(MeterDefinition entity) {
    return null;
  }
}
