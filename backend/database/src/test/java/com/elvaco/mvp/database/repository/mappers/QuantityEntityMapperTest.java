package com.elvaco.mvp.database.repository.mappers;

import java.util.List;

import com.elvaco.mvp.core.access.QuantityAccess;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.core.domainmodels.QuantityPresentationInformation;
import com.elvaco.mvp.core.domainmodels.SeriesDisplayMode;
import com.elvaco.mvp.database.entity.meter.QuantityEntity;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.elvaco.mvp.core.domainmodels.Quantity.VOLUME;
import static com.elvaco.mvp.database.repository.mappers.QuantityEntityMapper.toDomainModel;
import static com.elvaco.mvp.database.repository.mappers.QuantityEntityMapper.toEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class QuantityEntityMapperTest {

  @BeforeClass
  public static void setup() {
    QuantityAccess.singleton().loadAll(List.of(
      new Quantity(1, "Volume", VOLUME.getPresentationInformation(), "m3")
    ));
  }

  @AfterClass
  public static void tearDown() {
    QuantityAccess.singleton().clear();
  }

  @Test
  public void toEntity_QuantityNotInCache() {
    QuantityEntity entity = toEntity(new Quantity(
      2,
      "Calle",
      new QuantityPresentationInformation("m3", SeriesDisplayMode.CONSUMPTION)
    ));
    assertThat(entity.name).isEqualTo("Calle");
    assertThat(entity.id).isEqualTo(2);
    assertThat(entity.storageUnit).isEqualTo("m3");
    assertThat(entity.seriesDisplayMode).isEqualTo(SeriesDisplayMode.CONSUMPTION);
  }

  @Test
  public void toEntity_QuantityInCache() {
    QuantityEntity entity = toEntity(new Quantity(
      1,
      "Volume",
      new QuantityPresentationInformation("m3", SeriesDisplayMode.CONSUMPTION)
    ));
    assertThat(entity.name).isEqualTo("Volume");
    assertThat(entity.id).isEqualTo(1);
    assertThat(entity.storageUnit).isEqualTo("m3");
    assertThat(entity.seriesDisplayMode).isEqualTo(SeriesDisplayMode.CONSUMPTION);
    assertThat(entity.displayUnit).isEqualTo("m3");
  }

  @Test
  public void toEntity_QuantityWithDifferentIdInCache() {
    var volumeWithDifferentId = new Quantity(
      2,
      "Volume",
      new QuantityPresentationInformation("m3", SeriesDisplayMode.CONSUMPTION)
    );

    assertThatThrownBy(() -> toEntity(volumeWithDifferentId))
      .hasMessage("Supplied Qunatity.Id does not match previously stored Id");
  }

  @Test
  public void mappingIsIdempotentForAlreadySavedQuantity() {
    var volume = QuantityAccess.singleton().getByName("Volume");
    assertThat(toDomainModel(toEntity(volume))).isEqualTo(volume);
  }
}
