package com.example.mealconnect.domain;

import java.util.Locale;

public class ClientIdGenerator {

    // Example: SRY-1731004930123
    public static String generate(String siteCode) {
        long millis = System.currentTimeMillis();
        return String.format(Locale.US, "%s-%d",
                siteCode.toUpperCase(Locale.US),
                millis);
    }
}
