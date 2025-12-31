package com.ts.messenger.utils;

public interface StringConstants {
  
  String CHARSET_NAME = "UTF-8";
  /**
   * Byte order mark for UTF-8.
   * zero-width no-break space
   */
  String UTF8_BOM = "\uFEFF";
  /**
   * Line separator character.
   */
  String CRLF     = System.getProperty("line.separator");
  /**
   * Empty String.
   */
  String EMPTY    = "";
  /**
   * Tabulation character.
   */
  String TAB      = "\t";
  /**
   * Comma character.
   */
  String COMMA    = ",";
  /**
   * JavaScript line separator.
   */
  String CRLF_JS  = "\n";

  /**
   * Get line separator and as many tab characters as amount.
   * @param amount number of tab characters
   * @return line separator + amount of tab characters
   */
  String getLineSeparatorWithTabs(int amount);
}
