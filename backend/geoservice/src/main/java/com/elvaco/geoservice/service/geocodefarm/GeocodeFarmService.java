package com.elvaco.geoservice.service.geocodefarm;

import com.elvaco.geoservice.repository.entity.Address;
import com.elvaco.geoservice.repository.entity.GeoLocation;
import com.elvaco.geoservice.service.AddressToGeoService;
import farm.geocoding.beans.GeocodingFarmResult;
import farm.geocoding.beans.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class GeocodeFarmService implements AddressToGeoService {

  private static final String ID = "GeoCodeFarm";

  @Value("${geocodeFarm.url}")
  private String url;

  public void setUrl(String url) {
    this.url = url;
  }

  @Value("${geocodeFarm.quota:250}")
  private Integer quota;

  @Value("${geocodeFarm.maxrate:4}")
  private Integer maxRate;

  @Override
  public GeoLocation getGeoByAddress(Address address) {
    GeocodingFarmResult result = new RestTemplate()
      .getForObject(
        url,
        GeocodingFarmResult.class,
        address.street + "," + address.city + "," + address.country
      );

    return convert(result);
  }

  private GeoLocation convert(GeocodingFarmResult source) {
    GeoLocation target = null;
    if (source.getGeocodingResults().getResults() != null
        && source.getGeocodingResults().getStatus().getResultCount() >= 1) {
      target = new GeoLocation();
      Result result = source.getGeocodingResults().getResults().get(0);
      target.setLatitude(result.getCoordinates().getLatitude());
      target.setLongitude(result.getCoordinates().getLongitude());
      target.setConfidence(Accuracy.from(result.getAccuracy()));
      target.setSource(getId());
    }
    this.quota = Integer.parseInt(source.getGeocodingResults().getAccount().getUsageLimit())
                 - Integer.parseInt(source.getGeocodingResults().getAccount().getUsedToday());
    return target;
  }

  @Override
  public String getId() {
    return ID;
  }

  /**
   * Returns quota left. Note that the quota is for now reset at every restart and
   * will be correct after first call request to getGeoByAddress
   */
  @Override
  public Integer getQuota() {
    return quota;
  }

  @Override
  public Integer getMaxRate() {
    return maxRate;
  }

  @Scheduled(zone = "GMT-5", cron = "0 00 00 * * *")
  public void resetQuota() {
    this.quota = 250;
  }
}
