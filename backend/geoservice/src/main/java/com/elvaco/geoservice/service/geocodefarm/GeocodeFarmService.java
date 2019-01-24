package com.elvaco.geoservice.service.geocodefarm;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.elvaco.geoservice.repository.entity.Address;
import com.elvaco.geoservice.repository.entity.GeoLocation;
import com.elvaco.geoservice.service.AddressToGeoService;

import farm.geocoding.beans.Account;
import farm.geocoding.beans.GeocodingFarmResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class GeocodeFarmService implements AddressToGeoService {

  private static final String ID = "GeoCodeFarm";

  private static final Map<String, String> COUNTRY_TO_CODE_MAP = new HashMap<>();

  @Value("${geocodeFarm.url}")
  private String url;

  @Value("${geocodeFarm.quota:250}")
  private Integer quota;

  @Value("${geocodeFarm.maxrate:4}")
  private Integer maxRate;

  static {
    //Add all countries to the map, ie sweden -> se, sverige -> se, tyskland -> de, germany -> de
    // TODO: some countries like US and GB can be named in more ways, like U.S.A. USA, United
    // TODO: Kingdom etc. We will probably need to address this one way or another in future.
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
  @Nullable
  public GeoLocation getGeoByAddress(Address address) {
    String countryCode = COUNTRY_TO_CODE_MAP.get(address.getCountry().toLowerCase());
    GeocodingFarmResult result = new RestTemplate().getForObject(
      url,
      GeocodingFarmResult.class,
      address.street + " " + address.zip + " " + address.city + " " + address.country,
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

  @Nullable
  private GeoLocation convert(@Nullable GeocodingFarmResult source) {
    if (source == null) {
      return null;
    }

    adjustQuota(source.getGeocodingResults().getAccount());

    return Optional.of(source.getGeocodingResults())
      .filter(geocodingResults -> Objects.nonNull(geocodingResults.getStatus().getResultCount()))
      .filter(geocodingResults -> geocodingResults.getStatus().getResultCount() >= 1)
      .map(geocodingResults -> geocodingResults.getResults().get(0))
      .map(result -> {
        GeoLocation target = new GeoLocation();
        target.setLatitude(result.getCoordinates().getLatitude());
        target.setLongitude(result.getCoordinates().getLongitude());
        target.setConfidence(Accuracy.from(result.getAccuracy()).value);
        target.setSource(getId());
        return target;
      })
      .orElse(null);
  }

  private void adjustQuota(Account account) {
    if (account != null) {
      this.quota =
        Integer.parseInt(account.getUsageLimit()) - Integer.parseInt(account.getUsedToday());
    }
  }
}
