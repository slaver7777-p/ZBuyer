package dev.slava7777.buyer.util;

import java.text.DecimalFormat;

public class EconomyUtil {

    public static final DecimalFormat df = new DecimalFormat("#.##");
    public static long toCents(double amount) {
        return Math.round(amount * 100D);
    }
    public static double fromCents(long amount) {
        return amount * 0.01D;
    }
}
