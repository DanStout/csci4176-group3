package ca.dal.csci4176.journalit.utils;

import java.util.regex.Pattern;

public class StringUtils
{
    private static final Pattern PATTERN_WHITESPACE = Pattern.compile("\\s*");

    public static boolean isWhitespace(String str)
    {
        return str.isEmpty() || PATTERN_WHITESPACE.matcher(str).matches();
    }
}
