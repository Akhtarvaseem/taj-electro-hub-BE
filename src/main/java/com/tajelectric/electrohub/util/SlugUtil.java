package com.tajelectric.electrohub.util;

import java.util.Locale;

public class SlugUtil {
    public static String slugify(String text) {
        String base = text.toLowerCase(Locale.ROOT).trim()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
        if (base.length() > 300) base = base.substring(0, 300);
        String rand = Integer.toHexString((int) (Math.random() * 0xFFFFF));
        return base + "-" + rand;
    }
}
