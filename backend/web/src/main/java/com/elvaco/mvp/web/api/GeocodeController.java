package com.elvaco.mvp.web.api;

import java.util.UUID;

import com.elvaco.mvp.core.domainmodels.Pk;
import com.elvaco.mvp.core.spi.repository.Locations;
import com.elvaco.mvp.core.spi.repository.LogicalMeters;
import com.elvaco.mvp.web.dto.geoservice.GeoResponseDto;
import com.elvaco.mvp.web.dto.geoservice.GeoResponseErrorDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import static com.elvaco.mvp.web.mapper.LocationDtoMapper.toLocationWithId;

@Slf4j
@RequiredArgsConstructor
@RestApi("/api/v1/geocodes")
public class GeocodeController {

  private final Locations locations;
  private final LogicalMeters logicalMeters;

  @PostMapping("/callback/{id}")
  public void callback(@PathVariable UUID id, @RequestBody GeoResponseDto geoResponse) {
    logicalMeters.findById(id)
      .map(logicalMeter -> toLocationWithId(geoResponse, new Pk(id, logicalMeter.organisationId)))
      .map(locations::save);
  }

  @PostMapping("/error/{id}")
  public void error(@PathVariable UUID id, @RequestBody GeoResponseErrorDto payload) {
    // TODO[!must!] Just log for now!
    // TODO[!must!] We'll see what we should do with all these error responses later.
    log.warn("/api/v1/geocodes/error/{}: {}", id, payload);
  }
}
