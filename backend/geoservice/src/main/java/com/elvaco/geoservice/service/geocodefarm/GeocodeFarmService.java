package com.elvaco.geoservice.service.geocodefarm;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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

  @Value("${geocodeFarm.quota:250}")
  private Integer quota;

  @Value("${geocodeFarm.maxrate:4}")
  private Integer maxRate;

  private static final Map<String, String> COUNTRY_TO_CODE_MAP = new HashMap<>();

  static {
    //Add all contries to the map, ie sweden->se, sverige->se, tyskland->de, germany->de
    //TODO: some countries like US and GB can be named in more ways, like U.S.A. USA, United
    // Kingdom etc. We will prabably need to address this one way or another in future.
    String[] locales = Locale.getISOCountries();
    for (String countryCode : locales) {
      Locale locale = new Locale("", countryCode);
      COUNTRY_TO_CODE_MAP.put(
        locale.getDisplayCountry(Locale.ENGLISH).toLowerCase(),
        locale.getCountry().toLowerCase()
      );
      COUNTRY_TO_CODE_MAP.put(
        locale.getDisplayCountry(new Locale("sv", "SE")).toLowerCase(),
        locale.getCountry().toLowerCase()
      );
    }
  }

  public void setUrl(String url) {
    this.url = url;
  }

  @Override
  public GeoLocation getGeoByAddress(Address address) {
    String countryCode = COUNTRY_TO_CODE_MAP.get(address.getCountry().toLowerCase());
    GeocodingFarmResult result = new RestTemplate()
      .getForObject(
        url,
        GeocodingFarmResult.class,
        address.street + " " + address.city + " " + address.country,
        address,
        countryCode
      );

    return convert(result);
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

  private GeoLocation convert(GeocodingFarmResult source) {
    GeoLocation target = null;
    if (source.getGeocodingResults().getResults() != null
      && source.getGeocodingResults().getStatus().getResultCount() >= 1) {
      target = new GeoLocation();
      Result result = source.getGeocodingResults().getResults().get(0);
      target.setLatitude(result.getCoordinates().getLatitude());
      target.setLongitude(result.getCoordinates().getLongitude());
      target.setConfidence(Accuracy.from(result.getAccuracy()).value);
      target.setSource(getId());
    }
    this.quota = Integer.parseInt(source.getGeocodingResults().getAccount().getUsageLimit())
      - Integer.parseInt(source.getGeocodingResults().getAccount().getUsedToday());
    return target;
  }
}
