package com.elvaco.mvp.database.repository.queryfilters;

import java.util.ArrayList;
import java.util.List;

import com.elvaco.mvp.database.repository.queryfilters.LocationParametersParser.AddressParam;
import com.elvaco.mvp.database.repository.queryfilters.LocationParametersParser.CityParam;
import com.elvaco.mvp.database.repository.queryfilters.LocationParametersParser.Parameters;
import org.junit.Test;

import static com.elvaco.mvp.database.repository.queryfilters.LocationParametersParser.toAddressParameters;
import static com.elvaco.mvp.database.repository.queryfilters.LocationParametersParser.toAddressParams;
import static com.elvaco.mvp.database.repository.queryfilters.LocationParametersParser.toCityParameters;
import static com.elvaco.mvp.database.repository.queryfilters.LocationParametersParser.toCityParams;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class LocationParametersParserTest {

  @Test
  public void nullCityListShouldThrow() {
    assertThatThrownBy(() -> toCityParams(null)).isInstanceOf(NullPointerException.class);
  }

  @Test
  public void emptyCityListShouldReturnEmptyList() {
    assertThat(toCityParams(new ArrayList<>())).isEmpty();
  }

  @Test
  public void emptyCityListWhenTheStringItemsAreNotFormattedCorrect() {
    assertThat(toSingleCityParam("")).isEmpty();
    assertThat(toSingleCityParam("a,")).isEmpty();
    assertThat(toSingleCityParam("a, ")).isEmpty();
  }

  @Test
  public void cannotCreateCityParamsWithTooManyParameterArguments() {
    assertThat(toSingleCityParam("a,b,c")).isEmpty();
  }

  @Test
  public void canCreateSingleCityParam() {
    assertThat(toSingleCityParam("a,b")).containsExactly(new CityParam("a", "b"));
  }

  @Test
  public void canCreateSingleCityParamWithTrailingComma() {
    assertThat(toSingleCityParam("a,b,")).containsExactly(new CityParam("a", "b"));
    assertThat(toSingleCityParam("a,b, ")).containsExactly(new CityParam("a", "b"));
  }

  @Test
  public void shouldCreateCityParamsByTrimmingTheValues() {
    assertThat(toSingleCityParam(" a , b")).containsExactly(new CityParam("a", "b"));
    assertThat(toSingleCityParam(" usa  , new york ")).containsExactly(
      new CityParam("usa", "new york")
    );
  }

  @Test
  public void shouldCreateCityParams() {
    assertThat(toCityParams(asList("a,b", "c,d"))).containsExactly(
      new CityParam("a", "b"),
      new CityParam("c", "d")
    );
  }

  @Test
  public void canCreateSingleCityParamThatIgnoresCase() {
    assertThat(toSingleCityParam("aBc,bEER")).containsExactly(
      new CityParam("abc", "beer")
    );
  }

  @Test
  public void nullAddressListShouldThrow() {
    assertThatThrownBy(() -> toAddressParams(null)).isInstanceOf(NullPointerException.class);
  }

  @Test
  public void emptyAddressListShouldReturnEmptyList() {
    assertThat(toAddressParams(new ArrayList<>())).isEmpty();
  }

  @Test
  public void emptyAddressListWhenTheStringItemsAreNotFormattedCorrect() {
    assertThat(toSingleAddressParam("")).isEmpty();
    assertThat(toSingleAddressParam("a,")).isEmpty();
    assertThat(toSingleAddressParam("a,b")).isEmpty();
    assertThat(toSingleAddressParam("a,b,")).isEmpty();
    assertThat(toSingleAddressParam("a,b, ")).isEmpty();
  }

  @Test
  public void canCreateSingleAddressParam() {
    assertThat(toSingleAddressParam("a,b,c")).containsExactly(new AddressParam("a", "b", "c"));
  }

  @Test
  public void canCreateSingleAddressParamThatIgnoresCase() {
    assertThat(toSingleAddressParam("aBc,bEER,CaD")).containsExactly(
      new AddressParam("abc", "beer", "cad")
    );
  }

  @Test
  public void canCreateSingleAddressParamWithTrailingComma() {
    assertThat(toSingleAddressParam("a,b,c,")).containsExactly(new AddressParam("a", "b", "c"));
    assertThat(toSingleAddressParam("a,b,c, ")).containsExactly(new AddressParam("a", "b", "c"));
  }

  @Test
  public void shouldCreateAddressParamsByTrimmingTheValues() {
    assertThat(toSingleAddressParam(" a , b, c")).containsExactly(new AddressParam("a", "b", "c"));
    assertThat(toSingleAddressParam(" usa  , new york , wall street ")).containsExactly(
      new AddressParam("usa", "new york", "wall street")
    );
  }

  @Test
  public void shouldCreateAddressParams() {
    assertThat(toAddressParams(asList("a,b,c", "c,d,e"))).containsExactly(
      new AddressParam("a", "b", "c"),
      new AddressParam("c", "d", "e")
    );
  }

  @Test
  public void createCityParameters() {
    Parameters parameters = toCityParameters(singletonList("aBc,bEER"));
    assertThat(parameters.countries).containsExactly("abc");
    assertThat(parameters.cities).containsExactly("beer");
    assertThat(parameters.addresses).isEmpty();
    assertThat(parameters.hasCities()).isTrue();
  }

  @Test
  public void createCityParameters_IgnoresDuplicates() {
    Parameters parameters = toCityParameters(asList("aBc,bEER", "abc,def", "pepsi,bEer"));
    assertThat(parameters.countries).containsExactly("abc", "pepsi");
    assertThat(parameters.hasUnknownCountries).isFalse();
    assertThat(parameters.cities).containsExactly("def", "beer");
    assertThat(parameters.addresses).isEmpty();
  }

  @Test
  public void createCityParams_WithUnknownCountries() {
    Parameters parameters = toCityParameters(asList("unknown,bEER", " unknown ,def"));
    assertThat(parameters.cities).containsExactly("def", "beer");
    assertThat(parameters.hasUnknownCountries).isTrue();
    assertThat(parameters.hasUnknownAddresses).isFalse();
    assertThat(parameters.countries).isEmpty();
    assertThat(parameters.addresses).isEmpty();
  }

  @Test
  public void createCityParams_WithUnknownCountriesAndCities() {
    Parameters parameters = toCityParameters(asList("unknown,kungsbacka", " unknown , unknown"));
    assertThat(parameters.hasUnknownCountries).isTrue();
    assertThat(parameters.hasUnknownCities).isTrue();
    assertThat(parameters.cities).containsExactly("kungsbacka");
    assertThat(parameters.countries).isEmpty();
    assertThat(parameters.addresses).isEmpty();
  }

  @Test
  public void createAddressParameters() {
    Parameters parameters = toAddressParameters(singletonList("a, b , c "));
    assertThat(parameters.countries).containsExactly("a");
    assertThat(parameters.cities).containsExactly("b");
    assertThat(parameters.addresses).containsExactly("c");
    assertThat(parameters.hasAddresses()).isTrue();
  }

  @Test
  public void createAddressParameters_IgnoresDuplicates() {
    Parameters parameters = toAddressParameters(asList("a,b,c", "a,b,d", "a,a,c"));
    assertThat(parameters.hasUnknownAddresses).isFalse();
    assertThat(parameters.countries).containsExactly("a");
    assertThat(parameters.cities).containsExactly("a", "b");
    assertThat(parameters.addresses).containsExactly("c", "d");
    assertThat(parameters.hasAddresses()).isTrue();
  }

  @Test
  public void createUnknownAddressParameters() {
    Parameters parameters = toAddressParameters(singletonList("sweden,kungsbacka,unknown"));
    assertThat(parameters.hasUnknownAddresses).isTrue();
    assertThat(parameters.addresses).isEmpty();
    assertThat(parameters.countries).containsExactly("sweden");
    assertThat(parameters.cities).containsExactly("kungsbacka");
    assertThat(parameters.hasAddresses()).isFalse();
  }

  private static List<AddressParam> toSingleAddressParam(String s) {
    return toAddressParams(singletonList(s));
  }

  private static List<CityParam> toSingleCityParam(String s) {
    return toCityParams(singletonList(s));
  }
}
