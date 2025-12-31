package com.ts.messenger.utils.regexp;

public interface RegExpConstants {
    String REGX_DATE_TIME                                    = "^(([0-9]{2}[/][0-9]{2}[/][0-9]{4}){1} ([0-9]{2}[:][0-9]{2}[:][0-9]{2}){1})";
    String REGX_DATE_TIME_INFO                               = RegExpConstants.REGX_DATE_TIME + " INFO : ";
    // Log file name filter
    String REGX_USER_LOG_FILE                                = "^[_A-Za-z0-9]+[.]*[_A-Za-z0-9]+[_]{1}[0-9]{6}[\\-][0-9]{6}[\\-][0-9]{3}\\.[lL][oO][gG]$";
    String REGX_USER_LOG_FILE_NAME_SEP1                      = "[_]";
    String REGX_USER_LOG_FILE_NAME_SEP2                      = "[-]";
    // Log file database Transactions
    String REGX_TRANSACTION_SESSION_ID_BEGIN                 = ".*SessionId\\[";
    String REGX_TRANSACTION_X_END                            = "\\].*";
    String REGX_TRANSACTION_ID_BEGIN                         = ".*(Begin|Commit|Rollback) transaction \\[";
    String REGX_BEGIN_COMMIT_ROLLBACK_TRANSACTION            = RegExpConstants.REGX_DATE_TIME_INFO + "(Begin|Commit|Rollback) transaction";
    String REGX_BEGIN_TRANSACTION                            = RegExpConstants.REGX_DATE_TIME_INFO + "Begin transaction";
    String REGX_COMMIT_TRANSACTION                           = RegExpConstants.REGX_DATE_TIME_INFO + "Commit transaction";
    String REGX_ROLL_BACK_TRANSACTION                        = RegExpConstants.REGX_DATE_TIME_INFO + "Rollback transaction";

    String REGX_REPLACE_SQL_EXECUTION_ELAPSED_TIME           = ".*[execution|Elapsed]{1} [t|T]{1}ime\\[";
    String REGX_REPLACE_SQL_TIME_END                         = "s\\].*";
    String REGX_REPLACE_SQL_CONSTRUCTION_TIME_BEG            = "- SQL [: ]*construction time\\[";
    String REGX_REPLACE_ROWS_NUMBER_AFFECTED_END             = "\\].*";
    String REGX_REPLACE_ELAPSED_TIME_BEG                     = "^(\\p{Space}*)-Elapsed Time[\\[]";
    String REGX_REPLACE_ELAPSED_TIME_FULL                    = "[\\]](, Elapsed Time[\\[]([0-9]+)[\\.]([0-9]+)s\\]){0,1}\\.";
    String REGX_REPLACE_ROWS_NUMBER_AFFECTED_FULL            = RegExpConstants.REGX_DATE_TIME_INFO + "Rows (number|affected)[\\[]([0-9]+)[\\]], Elapsed Time[\\[]";
    String REGX_REPLACE_ROWS_NUMBER_AFFECTED_BEGIN           = ".*[R|r]{1}ows (number|affected)\\[";
    String REGX_ROWS_NUMBER_AFFECTED                         = "^(\\p{Space}*)-Rows (number|affected)[\\[]";
    String REGX_FILE_NAME                                    = "([\\[][_A-Za-z0-9]+[\\.]{1})";
    String REGX_QUERY_NAME                                   = "([\\.]{1}[_A-Za-z0-9]+[\\]])";

    String REGX_ROWS_AFFECTED_2                              = RegExpConstants.REGX_DATE_TIME_INFO + "Rows affected";

    String REGX_ROWS_NUMBER_ELAPSED_TIME                     = RegExpConstants.REGX_DATE_TIME_INFO + "Rows (number|affected)[\\[][0-9]+[\\]], Elapsed Time[\\[]([0-9]+[\\.]{0,1}[0-9]*s)[\\]][\\.]";
    String REGX_ROWS_NUMBER_2                                = "-Rows number[\\[]([0-9]+)[\\]]";
    String REGX_REQUEST_PREPARE_QUERY_NAME                   = RegExpConstants.REGX_DATE_TIME_INFO + "REQUEST PREPARE QUERY NAME: [\\[][_A-Za-z0-9]+[\\.]{1}[_A-Za-z0-9]+[\\]]";
    String REGX_INFO_REQUEST_EXECUTE_QUERY                   = RegExpConstants.REGX_DATE_TIME_INFO + "REQUEST EXECUTE QUERY : $";
    String REGX_INFO_REQUEST_EXECUTE_QUERY_NAME              = RegExpConstants.REGX_DATE_TIME_INFO + "REQUEST EXECUTE QUERY \\[([_A-Za-z0-9]+[\\.]{1}[_A-Za-z0-9]+)\\]: $";
    String REGX_CONSTRUCTION_TIME                            = "^[\\-]{1} SQL construction time";
    String REGX_ELAPSED_TIME                                 = "-Elapsed Time[\\[]([0-9]+[\\.]{0,1}[0-9]*s)[\\]][\\.]";
    String REGX_RAW_QUERY                                    = RegExpConstants.REGX_DATE_TIME_INFO + "REQUEST RAW QUERY : \\[";
    String REGX_CONSTRUCTION_TIME_EXECUTION_TIME_ROWS_NUMBER = "^(- SQL : construction time\\[)([0-9]+[\\.]{0,1}[0-9]*s)\\], execution time\\[([0-9]+[\\.]{0,1}[0-9]*s)\\], [R|r]{1}ows (number|affected)\\[[0-9]+\\]\\.$";

    String REGX_SESSION_SUCCESSFULLY_CLOSED                  = RegExpConstants.REGX_DATE_TIME_INFO + "Session successfully closed\\.\\.\\.\\.\\.";
    String REGX_ANY_WHITE_CHARACTER                          = "\\s";
    String REG_EXP_WHITE_CHAR_2_N                            = RegExpConstants.REGX_ANY_WHITE_CHARACTER  + "{2,}";
    String REG_EXP_WHITE_CHAR_0_N                            = RegExpConstants.REGX_ANY_WHITE_CHARACTER  + "*";
    String REG_EXP_IGNORE_SPECIAL_CHAR                       = "\\";
    String REGX_ENDS_WITH_SPACE                              = REGX_ANY_WHITE_CHARACTER + "+$";
    String REGX_RIGHT_PARENTHESIS                            = "\\)";
    String REGX_LEFT_PARENTHESIS                             = "\\(";
    String REGX_QUERY_COMMENT                                = "(^--.*)";
    String REGX_TWO_SPACES                                   = "(\\p{Space}{2,})";
    String REGX_JAVA_VARIABLE_NAME_RAW                       = "[a-zA-Z€$_][a-zA-Z0-9€$_]*";
    String REGX_JAVA_VARIABLE_NAME                           = "^" + REGX_JAVA_VARIABLE_NAME_RAW + "$";
    String REGX_JAVA_VARIABLE_NAME_WORD                      = "\\b" + REGX_JAVA_VARIABLE_NAME_RAW + "\\b";
    String REGX_JAVA_SET_METHOD                              = "^" + "set" + "[A-Z€$_][a-zA-Z0-9€$_]*" + "$";
}