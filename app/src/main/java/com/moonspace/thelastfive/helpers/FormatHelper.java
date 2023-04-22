package com.moonspace.thelastfive.helpers;

import java.util.Locale;

public class FormatHelper
{
    /**
     * Formats a double value as a String using the default locale.
     *
     * @param value the value to be converted.
     * @return a double value as a String using the default locale.
     */
    public static String doubleToDefaultLocaleString(double value)
    {
        return String.format(Locale.getDefault(), "%.1f", value);
    }
}
