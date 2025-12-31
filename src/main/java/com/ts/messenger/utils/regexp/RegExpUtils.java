package com.ts.messenger.utils.regexp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RegExpUtils {
    private RegExpUtils() {
    }

    public static boolean isMatchFound(final String string, final String regexp) {
        final Matcher matcher = RegExpUtils.createMatcher(string, regexp);
        return isMatchFound(matcher);
    }

    public static Matcher createMatcher(final String string, final String regexp) {
        final Pattern pattern = Pattern.compile(regexp);
        final Matcher matcher = pattern.matcher(string);
        return matcher;
    }
    
    public static boolean isMatchFound(final Matcher matcher) {
        final boolean matchFound = matcher.find();
        return matchFound;
    }
}
