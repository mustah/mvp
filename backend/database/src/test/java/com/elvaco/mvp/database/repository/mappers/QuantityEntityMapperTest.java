package com.elvaco.mvp.database.repository.mappers;

import java.util.List;

import com.elvaco.mvp.core.access.QuantityProvider;
import com.elvaco.mvp.core.domainmodels.Quantity;
import com.elvaco.mvp.database.entity.meter.QuantityEntity;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class QuantityEntityMapperTest {

  private static final List<Quantity> FIXTURE_QUANTITIES = List.of(
    new Quantity(1, "Volume", "m3")
  );

  private static final QuantityProvider QUANTITY_PROVIDER = name -> FIXTURE_QUANTITIES
    .stream()
    .filter(quantity -> quantity.name.equals(name))
    .findAny();

  private static final QuantityEntityMapper QUANTITY_ENTITY_MAPPER = new QuantityEntityMapper(
    QUANTITY_PROVIDER
  );

  @Test
  public void toEntity_QuantityNotInCache() {
    QuantityEntity entity = QUANTITY_ENTITY_MAPPER.toEntity(new Quantity(
      2,
      "Calle",
      "m3"
    ));
    assertThat(entity.name).isEqualTo("Calle");
    assertThat(entity.id).isEqualTo(2);
    assertThat(entity.storageUnit).isEqualTo("m3");
  }

  @Test
  public void toEntity_QuantityInCache() {
    QuantityEntity entity = QUANTITY_ENTITY_MAPPER.toEntity(new Quantity(
      null,
      "Volume",
      "m3"
    ));
    assertThat(entity.name).isEqualTo("Volume");
    assertThat(entity.id).isEqualTo(1);
    assertThat(entity.storageUnit).isEqualTo("m3");
  }

  @Test
  public void toEntity_QuantityWithDifferentIdInCache() {
    var volumeWithDifferentId = new Quantity(
      2,
      "Volume",
      "m3"
    );

    assertThatThrownBy(() -> QUANTITY_ENTITY_MAPPER.toEntity(volumeWithDifferentId))
      .hasMessage("Supplied Quantity.Id '2' does not match previously stored Id '1'");
  }

  @Test
  public void mappingIsIdempotentForAlreadySavedQuantity() {
    var volume = QUANTITY_PROVIDER.getByName("Volume").get();
    assertThat(QUANTITY_ENTITY_MAPPER.toDomainModel(
      QUANTITY_ENTITY_MAPPER.toEntity(volume)
    )).isEqualTo(volume);
  }
}
