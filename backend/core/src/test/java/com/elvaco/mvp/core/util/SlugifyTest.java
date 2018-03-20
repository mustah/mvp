package com.elvaco.mvp.core.util;

import org.junit.Test;

import static com.elvaco.mvp.core.util.Slugify.slugify;
import static org.assertj.core.api.Assertions.assertThat;

public class SlugifyTest {

  @Test
  public void empty() {
    assertThat(slugify("")).isEqualTo("");
  }

  @Test
  public void upperCaseIsLowerCased() {
    assertThat(slugify("AAAA")).isEqualTo("aaaa");
  }

  @Test
  public void upperCaseIsLowerCasedNonAscii() {
    assertThat(slugify("ÅÄÖ")).isEqualTo("aao");
  }

  @Test
  public void whitespace() {
    assertThat(slugify(" ")).isEqualTo("");
    assertThat(slugify("\t")).isEqualTo("");
    assertThat(slugify("\t\n\t   \r")).isEqualTo("");
  }

  @Test
  public void whitespaceAndText() {
    assertThat(slugify(" a ")).isEqualTo("a");
    assertThat(slugify("a\ta")).isEqualTo("a-a");
    assertThat(slugify("\ta\n\ta  a \r")).isEqualTo("a-a-a");
  }

  @Test
  public void stripsNonAscii() {
    assertThat(slugify("A $tring \\/\\/ith\t funny ch@racterß"))
      .isEqualTo("a-tring-ith-funny-chracter");
  }

}
