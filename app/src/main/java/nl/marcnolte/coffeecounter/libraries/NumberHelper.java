package nl.marcnolte.coffeecounter.libraries;

import java.text.NumberFormat;
import java.util.Locale;

final public class NumberHelper
{
    /**
     * Debug Tag
     */
    final private static String DEBUG_TAG = "NumberHelper";

    public static String getDecimal(Double number, int minFactionDigits, int maxFractionDigits)
    {
        Locale       formatLocale = Locale.getDefault();
        NumberFormat formatter    = NumberFormat.getInstance(formatLocale);

        formatter.setMinimumFractionDigits(minFactionDigits);
        formatter.setMaximumFractionDigits(maxFractionDigits);

        return formatter.format(number);
    }
}
