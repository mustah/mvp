package com.elvaco.mvp.core.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public final class Slugify {

  private static final Pattern WHITESPACE_RE = Pattern.compile("[\\s]+");
  private static final Pattern NONLATIN_RE = Pattern.compile("[^\\w-]+");

  private Slugify() {}

  public static String slugify(String str) {

    String trimmed = str.trim();
    String whitespaceReplaced = WHITESPACE_RE.matcher(trimmed).replaceAll("-");
    String normalized = Normalizer.normalize(whitespaceReplaced, Normalizer.Form.NFKD);
    String nonlatinReplaced = NONLATIN_RE.matcher(normalized).replaceAll("");
    return nonlatinReplaced.toLowerCase(Locale.ENGLISH);
  }
}
