package com.moonspace.thelastfive.helpers;

public class MathHelper
{
    /**
     * Rounds a value to the nearest tenth place.
     *
     * @param value the value to be rounded.
     * @return the value rounded to the nearest tenth place.
     */
    public static Double roundToNearestTenth(Double value)
    {
        // Round result to nearest 10th
        return Math.round(value * 10.0) / 10.0;
    }
}
