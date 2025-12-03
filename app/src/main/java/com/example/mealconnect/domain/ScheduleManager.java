package com.example.mealconnect.domain;

import java.util.Calendar;
import java.util.TimeZone;

/**

 * ScheduleManager
 *
 * Determines whether today is a valid pickup day.
 * Pickup day can be configured (e.g., Monday, Tuesday, etc.).
 */
public class ScheduleManager {

    // Default pickup day (Monday)
    private static int pickupDay = Calendar.MONDAY;

    /**

     * Set the pickup day.
     * Example: Calendar.MONDAY, Calendar.TUESDAY, ...
     *
     * @param dayOfWeek Day of the week (1=Sunday, 2=Monday, ..., 7=Saturday)
     */
    public static void setPickupDay(int dayOfWeek) {
        pickupDay = dayOfWeek;
    }

    /**

     * Returns true if today is the configured pickup day.
     *
     * @return true if today is the pickup day, false otherwise
     */
    public static boolean isPickupDay() {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        int today = calendar.get(Calendar.DAY_OF_WEEK);
        return today == pickupDay;
    }
}
