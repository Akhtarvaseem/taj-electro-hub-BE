package com.tajelectric.electrohub.util;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PinUtil {
    private static final Pattern SIX = Pattern.compile("[1-9][0-9]{5}");

    /** Pull a 6-digit Indian PIN from any map/string (digits only, ignores spaces/decimals). */
    public static String extract(Map<String, Object> addr) {
        if (addr == null) return "";
        String pin = from(addr.get("pincode"));
        if (pin.isEmpty()) pin = from(addr.get("pinCode"));
        if (pin.isEmpty()) pin = from(addr.get("pin"));
        if (pin.isEmpty()) pin = from(addr.get("address"));
        return pin;
    }

    public static String from(Object raw) {
        if (raw == null) return "";
        String s = String.valueOf(raw).trim();
        if (s.equals("null") || s.equals("undefined")) return "";
        // handle 272202.0 from numeric JSON
        if (s.matches("[0-9]+\\.0+")) s = s.substring(0, s.indexOf('.'));
        Matcher m = SIX.matcher(s.replaceAll("\\D", " "));
        return m.find() ? m.group() : "";
    }

    public static boolean isValid(String pin) {
        return pin != null && pin.matches("[1-9][0-9]{5}");
    }
}
