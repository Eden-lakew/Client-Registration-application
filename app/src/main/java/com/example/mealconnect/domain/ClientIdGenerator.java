package com.example.mealconnect.domain;

import java.util.Locale;

public class ClientIdGenerator {

    public static String generate(String siteCode) {
        long millis = System.currentTimeMillis();
        return String.format(Locale.US, "%s-%d",
                siteCode.toUpperCase(Locale.US),
                millis);
    }
}
