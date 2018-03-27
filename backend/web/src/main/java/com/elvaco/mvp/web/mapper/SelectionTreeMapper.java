package com.elvaco.mvp.web.mapper;

import com.elvaco.mvp.core.domainmodels.LogicalMeter;
import com.elvaco.mvp.core.domainmodels.SelectionTree;
import com.elvaco.mvp.web.dto.SelectionTreeDto;
import com.elvaco.mvp.web.dto.SelectionTreeDto.AddressDto;
import com.elvaco.mvp.web.dto.SelectionTreeDto.CityDto;
import com.elvaco.mvp.web.dto.SelectionTreeDto.MeterDto;

import static com.elvaco.mvp.core.domainmodels.SelectionTree.Address;
import static com.elvaco.mvp.core.domainmodels.SelectionTree.City;
import static com.elvaco.mvp.core.domainmodels.SelectionTree.Meter;
import static java.util.stream.Collectors.toList;

public class SelectionTreeMapper {

  public void addToDto(LogicalMeter logicalMeter, SelectionTree selectionTree) {
    selectionTree.addToSelectionTree(logicalMeter);
  }

  public SelectionTreeDto toDto(SelectionTree selectionTree) {
    return new SelectionTreeDto(selectionTree.getCities()
                                  .stream()
                                  .map(SelectionTreeMapper::toCityDto)
                                  .collect(toList()));
  }

  private static CityDto toCityDto(City city) {
    return new CityDto(city.id, city.name, city.getAddresses()
      .stream()
      .map(SelectionTreeMapper::toAddressDto)
      .collect(toList()));
  }

  private static AddressDto toAddressDto(Address address) {
    return new AddressDto(address.name, address.getMeters().stream()
      .map(SelectionTreeMapper::toMeterDto)
      .collect(toList()));
  }

  private static MeterDto toMeterDto(Meter meter) {
    return new MeterDto(meter.id, meter.name);
  }
}
