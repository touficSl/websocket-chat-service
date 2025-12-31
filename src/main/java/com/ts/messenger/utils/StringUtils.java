package com.ts.messenger.utils;

import org.springframework.util.Assert;

import com.ts.messenger.utils.regexp.RegExpConstants;
import com.ts.messenger.utils.regexp.RegExpUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * A string utilities class.
 * @author bbarhoumi
 */
public final class StringUtils implements StringConstants {
  /**
   * A private constructor to avoid instantiating class from exterior.
   */
  private StringUtils() {
  }

  /**
   * Translate camelCase String to UpperCase with '_' as separator.
   * @param camelCase String for input
   * @return UpperCase with '_' as separator
   */
  public static String camelCaseToUpperWithUnderScoreSep(
      final String camelCase) {
    final String camelCaseTrim;
    final String upperCase;
    if (camelCase == null) {
      return null;
    } else {
      camelCaseTrim = camelCase.trim();
      upperCase = StringUtils.characterwiseCaseNormalizeU(camelCaseTrim);
    }
    final StringBuilder sb = new StringBuilder();
    final int length = upperCase.length();
    for (int index = 0; index < length; index++) {
      final char charAt = upperCase.charAt(index);
      if (RegExpUtils.isMatchFound(String.valueOf(charAt), "\\s")) {
        continue;
      }
      if ((index > 0) && RegExpUtils.isMatchFound(
          String.valueOf(camelCaseTrim.charAt(index - 1)), "\\s")) {
        sb.append("_");
      }
      final boolean currentIsUpper = charAt == camelCaseTrim.charAt(index);
      if (currentIsUpper && (sb.length() != 0)) {
        final boolean previewIsLower = camelCaseTrim
            .charAt(index - 1) != upperCase.charAt(index - 1);

        final boolean nextIsLower = ((index + 1) < length)
            && (camelCase.charAt(index + 1) != upperCase.charAt(index + 1));
        if (previewIsLower || nextIsLower) {
          if (!sb.toString().endsWith("_")
              && !"_".equals(String.valueOf(charAt))) {
            sb.append("_");
          }
        }
      }
      sb.append(charAt);
    }
    return sb.toString();
  }

  /**
   * @param object object to check if it's empty.
   * @return true if object is null or empty
   */
  public static boolean isEmpty(final Object object) {
    final boolean ingnoreWhiteSpace = true;
    return StringUtils.isEmpty(object, ingnoreWhiteSpace);
  }

  /**
   * @param object object to check if it's empty.
   * @param ingnoreWhiteSpace if true remove Whitespace and check if String is
   *          empty.
   * @return true if object is null or empty
   */
  @SuppressWarnings("rawtypes")
  public static boolean isEmpty(final Object object,
      final boolean ingnoreWhiteSpace) {
    if (object == null) {
      return true;
    }
    if (object instanceof String) {
      final String string;
      if (ingnoreWhiteSpace) {
        string = ((String) object).trim();
      } else {
        string = (String) object;
      }
      return EMPTY.equals(string);
    } else if (object instanceof Collection) {
      return ((Collection) object).isEmpty();
    } else {
      final String string;
      if (ingnoreWhiteSpace) {
        string = object.toString().trim();
      } else {
        string = object.toString();
      }
      return EMPTY.equals(string);
    }
  }

  /**
   * @deprecated Use {@link #isEmpty(Object)}
   * @param line String to check
   * @return true if empty
   */
  @Deprecated
  public static boolean isEmptyLine(final String line) {
    return isEmpty(line);
  }

  /**
   * Convert an object to String.
   * @param obj Object to parse
   * @return return Object.toString if not null
   */
  public static String parseString(final Object obj) {
    if (obj != null) {
      return obj.toString();
    }

    return EMPTY;
  }

  /**
   * Convert a string to double.
   * @param text Text to convert
   * @return String
   */
  public static double strToDbl(final String text) {
    if ((text != null) && (text.length() > 0)) {
      return Double.parseDouble(text);
    } else {
      return 0;
    }
  }

  /**
   * Convert a string to double.
   * @param text Text to convert
   * @return String
   */
  public static float strToFloat(final String text) {
    if ((text != null) && (text.length() > 0)) {
      return Float.parseFloat(text);
    } else {
      return 0;
    }
  }

  /**
   * Convert a string to long.
   * @param text Text to convert
   * @return String
   */
  public static long strToLng(final String text) {
    if ((text != null) && (text.length() > 0)) {
      return Long.parseLong(text);
    } else {
      return 0;
    }
  }

  /**
   * @param count : number of TAB after returning to a new line
   * @return line separator + amount of tab characters
   */
  public static String getCrlf(final int count) {
    return new StringUtils().getLineSeparatorWithTabs(count);
  }

  /**
   * @param sequence sequence to repeat
   * @param amount number of sequences to be added
   * @param sb to wrap result.
   */
  static void repeat(final String sequence, final long amount,
      final StringBuilder sb) {
    for (int i = 0; i < amount; i++) {
      sb.append(sequence);
    }
  }

  /**
   * Split into list.
   * @param source the string to be splitted
   * @param delimiter the delimiter which will used in split
   * @return a list or an empty list
   */
  public static List<String> splitIntoList(final String source, final String delimiter) {
    if ((source == null) || (delimiter == null) || (source.length() == 0)) {
      return new ArrayList<>();
    }
    final String[] split = source.split(delimiter);
    return new ArrayList<>(Arrays.asList(split));

  }

  /**
   * @param enm Enumeration to work on it.
   * @return String
   */
  public static String toStringEnumValues(final Class<? extends Enum<?>> enm) {
    Assert.notNull(enm);
    final String[] split = Arrays.toString(enm.getEnumConstants())
        .replaceAll("^.|.$", "").split(", ");
    final String string = Arrays.toString(split);
    if ("[]".equals(string)) {
      return StringConstants.EMPTY;
    }
    return string;
  }

  /**
   * @param string input
   * @return the input trimmed at end.
   */
  public static String trimEnd(final String string) {
    if (isEmpty(string)) {
      return EMPTY;
    }
    return string.replaceAll(RegExpConstants.REGX_ENDS_WITH_SPACE, EMPTY);
  }

  /**
   * @param string String.toLowerCase.
   * @return toLowerCase String
   */
  public static String characterwiseCaseNormalize(final String string) {
    if (string == null) {
      throw new NullPointerException();
    }
    final StringBuilder sb = new StringBuilder(string);
    for (int i = 0; i < sb.length(); i++) {
      sb.setCharAt(i,
          Character.toLowerCase(Character.toUpperCase(sb.charAt(i))));
    }
    return sb.toString();
  }

  /**
   * @param string String.toUpperCase.
   * @return toUpperCase String
   */
  public static String characterwiseCaseNormalizeU(final String string) {
    if (string == null) {
      throw new NullPointerException();
    }
    final StringBuilder sb = new StringBuilder(string);
    for (int i = 0; i < sb.length(); i++) {
      sb.setCharAt(i,
          Character.toUpperCase(Character.toLowerCase(sb.charAt(i))));
    }
    return sb.toString();
  }

  /**
   * Remove UTF-8 BOM if exists.<br>
   * http://www.rgagnon.com/javadetails/java-handle-utf8-file-with-bom.html
   * @param input string to verify
   * @return string without UTF8BOM
   */
  public static String removeUTF8BOM(final String input) {
    Assert.hasText(input);
    final String result;
    if (input.startsWith(StringConstants.UTF8_BOM)) {
      result = input.substring(1);
    } else {
      result = input;
    }
    return result;
  }

  @Override
  public String getLineSeparatorWithTabs(final int amount) {
    final StringBuilder sb = new StringBuilder();
    sb.append(CRLF);
    repeat(TAB, amount, sb);
    return sb.toString();
  }
}
