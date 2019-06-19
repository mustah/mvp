package com.elvaco.mvp.web;

import com.elvaco.mvp.core.domainmodels.AssetType;

public class OrganisationControllerAssetLoginBackgroundTest
  extends OrganisationControllerAssetTest {

  @Override
  protected AssetType assetUnderTest() {
    return AssetType.LOGIN_BACKGROUND;
  }
}
