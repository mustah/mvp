package com.elvaco.mvp.core.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Slugify {

  private static final Pattern WHITESPACE_RE = Pattern.compile("[\\s]+");
  private static final Pattern NONE_LATIN_RE = Pattern.compile("[^\\w-]+");

  public static String slugify(String str) {
    String whitespaceReplaced = WHITESPACE_RE.matcher(str.trim()).replaceAll("-");
    String normalized = Normalizer.normalize(whitespaceReplaced, Normalizer.Form.NFKD);
    return NONE_LATIN_RE.matcher(normalized)
      .replaceAll("")
      .toLowerCase(Locale.ENGLISH);
  }
}
