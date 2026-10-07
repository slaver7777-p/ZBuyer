package dev.slava7777.buyer.util;

public class EconomyUtil {

    public static long toCents(double amount) {
        return Math.round(amount * 100D);
    }

    public static double fromCents(long amount) {
        return amount * 0.01D;
    }

    public static String format(double value) {
        final boolean neg = value < 0;
        long cents = Math.round(Math.abs(value) * 100);
        long intPart = cents / 100;
        int frac = (int) (cents % 100);

        final String intStr = Long.toString(intPart);
        final StringBuilder sb = new StringBuilder(intStr.length() + 8);
        if (neg) sb.append('-');

        final int firstGroup = intStr.length() % 3;
        if (firstGroup > 0) {
            sb.append(intStr, 0, firstGroup);
            if (intStr.length() > firstGroup) sb.append('.');
        }
        for (int i = firstGroup; i < intStr.length(); i += 3) {
            sb.append(intStr, i, i + 3);
            if (i + 3 < intStr.length()) sb.append('.');
        }

        sb.append(',');
        if (frac < 10) sb.append('0');
        sb.append(frac);
        return sb.toString();
    }
}
