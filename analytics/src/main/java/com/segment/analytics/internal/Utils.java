package com.segment.analytics.internal;

import java.io.UnsupportedEncodingException;

public final class Utils {
  private Utils() {
    throw new AssertionError("No instances");
  }

  /** Returns {@code true} if the given string is null or empty. */
  public static boolean isNullOrEmpty(String string) {
    return string == null || string.trim().length() == 0;
  }

  /** Returns an auth credential for the Basic scheme. */
  public static String basicCredentials(String userName, String password) {
    try {
      String usernameAndPassword = userName + ":" + password;
      byte[] bytes = usernameAndPassword.getBytes("ISO-8859-1");
      String encoded = Base64.encode(bytes);
      return "Basic " + encoded;
    } catch (UnsupportedEncodingException e) {
      throw new AssertionError();
    }
  }
}
