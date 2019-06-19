package com.elvaco.mvp.web;

import com.elvaco.mvp.core.domainmodels.AssetType;

public class OrganisationControllerAssetLogotypeTest extends OrganisationControllerAssetTest {

  @Override
  protected AssetType assetUnderTest() {
    return AssetType.LOGOTYPE;
  }
}
