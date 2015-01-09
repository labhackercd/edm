package br.leg.camara.labhacker.edemocracia;

public class StringUtils {
    public static final String EMPTY = "";
    public static final int INDEX_NOT_FOUND = -1;

    // Empty checks
    //-----------------------------------------------------------------------
    /**
     * <p>Checks if a CharSequence is empty ("") or null.</p>
     *
     * <pre>
     * StringUtils.isEmpty(null)      = true
     * StringUtils.isEmpty("")        = true
     * StringUtils.isEmpty(" ")       = false
     * StringUtils.isEmpty("bob")     = false
     * StringUtils.isEmpty("  bob  ") = false
     * </pre>
     *
     * <p>NOTE: This method changed in Lang version 2.0.
     * It no longer trims the CharSequence.
     * That functionality is available in isBlank().</p>
     *
     * @param cs  the CharSequence to check, may be null
     * @return {@code true} if the CharSequence is empty or null
     * @since 3.0 Changed signature from isEmpty(String) to isEmpty(CharSequence)
     */
    public static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    // Stripping
    //-----------------------------------------------------------------------
    /**
     * <p>Strips whitespace from the start and end of a String.</p>
     *
     * <p>This is similar to {@link #trim(String)} but removes whitespace.
     * Whitespace is defined by {@link Character#isWhitespace(char)}.</p>
     *
     * <p>A {@code null} input String returns {@code null}.</p>
     *
     * <pre>
     * StringUtils.strip(null)     = null
     * StringUtils.strip("")       = ""
     * StringUtils.strip("   ")    = ""
     * StringUtils.strip("abc")    = "abc"
     * StringUtils.strip("  abc")  = "abc"
     * StringUtils.strip("abc  ")  = "abc"
     * StringUtils.strip(" abc ")  = "abc"
     * StringUtils.strip(" ab c ") = "ab c"
     * </pre>
     *
     * @param str  the String to remove whitespace from, may be null
     * @return the stripped String, {@code null} if null String input
     */
    public static String strip(final String str) {
        return strip(str, null);
    }

    /**
     * <p>Strips whitespace from the start and end of a String  returning
     * {@code null} if the String is empty ("") after the strip.</p>
     *
     * <p>This is similar to {@link #trimToNull(String)} but removes whitespace.
     * Whitespace is defined by {@link Character#isWhitespace(char)}.</p>
     *
     * <pre>
     * StringUtils.stripToNull(null)     = null
     * StringUtils.stripToNull("")       = null
     * StringUtils.stripToNull("   ")    = null
     * StringUtils.stripToNull("abc")    = "abc"
     * StringUtils.stripToNull("  abc")  = "abc"
     * StringUtils.stripToNull("abc  ")  = "abc"
     * StringUtils.stripToNull(" abc ")  = "abc"
     * StringUtils.stripToNull(" ab c ") = "ab c"
     * </pre>
     *
     * @param str  the String to be stripped, may be null
     * @return the stripped String,
     *  {@code null} if whitespace, empty or null String input
     * @since 2.0
     */
    public static String stripToNull(String str) {
        if (str == null) {
            return null;
        }
        str = strip(str, null);
        return str.isEmpty() ? null : str;
    }

    /**
     * <p>Strips whitespace from the start and end of a String  returning
     * an empty String if {@code null} input.</p>
     *
     * <p>This is similar to {@link #trimToEmpty(String)} but removes whitespace.
     * Whitespace is defined by {@link Character#isWhitespace(char)}.</p>
     *
     * <pre>
     * StringUtils.stripToEmpty(null)     = ""
     * StringUtils.stripToEmpty("")       = ""
     * StringUtils.stripToEmpty("   ")    = ""
     * StringUtils.stripToEmpty("abc")    = "abc"
     * StringUtils.stripToEmpty("  abc")  = "abc"
     * StringUtils.stripToEmpty("abc  ")  = "abc"
     * StringUtils.stripToEmpty(" abc ")  = "abc"
     * StringUtils.stripToEmpty(" ab c ") = "ab c"
     * </pre>
     *
     * @param str  the String to be stripped, may be null
     * @return the trimmed String, or an empty String if {@code null} input
     * @since 2.0
     */
    public static String stripToEmpty(final String str) {
        return str == null ? EMPTY : strip(str, null);
    }

    /**
     * <p>Strips any of a set of characters from the start and end of a String.
     * This is similar to {@link String#trim()} but allows the characters
     * to be stripped to be controlled.</p>
     *
     * <p>A {@code null} input String returns {@code null}.
     * An empty string ("") input returns the empty string.</p>
     *
     * <p>If the stripChars String is {@code null}, whitespace is
     * stripped as defined by {@link Character#isWhitespace(char)}.
     * Alternatively use {@link #strip(String)}.</p>
     *
     * <pre>
     * StringUtils.strip(null, *)          = null
     * StringUtils.strip("", *)            = ""
     * StringUtils.strip("abc", null)      = "abc"
     * StringUtils.strip("  abc", null)    = "abc"
     * StringUtils.strip("abc  ", null)    = "abc"
     * StringUtils.strip(" abc ", null)    = "abc"
     * StringUtils.strip("  abcyx", "xyz") = "  abc"
     * </pre>
     *
     * @param str  the String to remove characters from, may be null
     * @param stripChars  the characters to remove, null treated as whitespace
     * @return the stripped String, {@code null} if null String input
     */
    public static String strip(String str, final String stripChars) {
        if (isEmpty(str)) {
            return str;
        }
        str = stripStart(str, stripChars);
        return stripEnd(str, stripChars);
    }

    /**
     * <p>Strips any of a set of characters from the start of a String.</p>
     *
     * <p>A {@code null} input String returns {@code null}.
     * An empty string ("") input returns the empty string.</p>
     *
     * <p>If the stripChars String is {@code null}, whitespace is
     * stripped as defined by {@link Character#isWhitespace(char)}.</p>
     *
     * <pre>
     * StringUtils.stripStart(null, *)          = null
     * StringUtils.stripStart("", *)            = ""
     * StringUtils.stripStart("abc", "")        = "abc"
     * StringUtils.stripStart("abc", null)      = "abc"
     * StringUtils.stripStart("  abc", null)    = "abc"
     * StringUtils.stripStart("abc  ", null)    = "abc  "
     * StringUtils.stripStart(" abc ", null)    = "abc "
     * StringUtils.stripStart("yxabc  ", "xyz") = "abc  "
     * </pre>
     *
     * @param str  the String to remove characters from, may be null
     * @param stripChars  the characters to remove, null treated as whitespace
     * @return the stripped String, {@code null} if null String input
     */
    public static String stripStart(final String str, final String stripChars) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return str;
        }
        int start = 0;
        if (stripChars == null) {
            while (start != strLen && Character.isWhitespace(str.charAt(start))) {
                start++;
            }
        } else if (stripChars.isEmpty()) {
            return str;
        } else {
            while (start != strLen && stripChars.indexOf(str.charAt(start)) != INDEX_NOT_FOUND) {
                start++;
            }
        }
        return str.substring(start);
    }

    /**
     * <p>Strips any of a set of characters from the end of a String.</p>
     *
     * <p>A {@code null} input String returns {@code null}.
     * An empty string ("") input returns the empty string.</p>
     *
     * <p>If the stripChars String is {@code null}, whitespace is
     * stripped as defined by {@link Character#isWhitespace(char)}.</p>
     *
     * <pre>
     * StringUtils.stripEnd(null, *)          = null
     * StringUtils.stripEnd("", *)            = ""
     * StringUtils.stripEnd("abc", "")        = "abc"
     * StringUtils.stripEnd("abc", null)      = "abc"
     * StringUtils.stripEnd("  abc", null)    = "  abc"
     * StringUtils.stripEnd("abc  ", null)    = "abc"
     * StringUtils.stripEnd(" abc ", null)    = " abc"
     * StringUtils.stripEnd("  abcyx", "xyz") = "  abc"
     * StringUtils.stripEnd("120.00", ".0")   = "12"
     * </pre>
     *
     * @param str  the String to remove characters from, may be null
     * @param stripChars  the set of characters to remove, null treated as whitespace
     * @return the stripped String, {@code null} if null String input
     */
    public static String stripEnd(final String str, final String stripChars) {
        int end;
        if (str == null || (end = str.length()) == 0) {
            return str;
        }

        if (stripChars == null) {
            while (end != 0 && Character.isWhitespace(str.charAt(end - 1))) {
                end--;
            }
        } else if (stripChars.isEmpty()) {
            return str;
        } else {
            while (end != 0 && stripChars.indexOf(str.charAt(end - 1)) != INDEX_NOT_FOUND) {
                end--;
            }
        }
        return str.substring(0, end);
    }
}
