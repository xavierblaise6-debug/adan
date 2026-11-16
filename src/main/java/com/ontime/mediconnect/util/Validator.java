package com.ontime.mediconnect.util;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Input validation shared by all screens. Every method returns null when the value is
 * valid, otherwise a user-friendly error message.
 */
public final class Validator {
    private static final Pattern NAME = Pattern.compile("^[A-Za-z][A-Za-z .'-]{1,49}$");
    private static final Pattern PHONE = Pattern.compile("^\\+?[0-9]{7,14}$");
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern BLOOD = Pattern.compile("^(A|B|AB|O)[+-]$");
    private static final Pattern STAFF_ID = Pattern.compile("^S[0-9]{3}$");

    private Validator() { }

    public static String name(String label, String v) {
        if (isBlank(v)) return label + " is required.";
        return NAME.matcher(v.trim()).matches() ? null
                : label + " must be 2-50 characters (letters, spaces, . ' - only).";
    }

    public static String text(String label, String v, int max) {
        if (isBlank(v)) return label + " is required.";
        return v.trim().length() <= max ? null : label + " must be at most " + max + " characters.";
    }

    public static String optionalText(String label, String v, int max) {
        return v == null || v.trim().length() <= max ? null : label + " must be at most " + max + " characters.";
    }

    public static String phone(String v) {
        if (isBlank(v)) return "Phone number is required.";
        return PHONE.matcher(v.trim()).matches() ? null : "Phone must be 7-14 digits, optionally starting with +.";
    }

    public static String email(String v) {
        if (isBlank(v)) return "Email is required.";
        if (v.trim().length() > 30) return "Email must be at most 30 characters.";
        return EMAIL.matcher(v.trim()).matches() ? null : "Email format is invalid.";
    }

    public static String gender(String v) {
        return v != null && v.matches("[MFO]") ? null : "Select a gender.";
    }

    public static String age(String v) {
        try {
            int a = Integer.parseInt(v.trim());
            return a >= 0 && a <= 120 ? null : "Age must be between 0 and 120.";
        } catch (RuntimeException e) {
            return "Age must be a whole number.";
        }
    }

    public static String bloodGroup(String v) {
        return v != null && BLOOD.matcher(v).matches() ? null : "Select a valid blood group (e.g. O+).";
    }

    public static String positiveNumber(String label, String v) {
        try {
            double d = Double.parseDouble(v.trim());
            return d > 0 && d < 1000 ? null : label + " must be greater than 0 and less than 1000.";
        } catch (RuntimeException e) {
            return label + " must be a number.";
        }
    }

    public static String money(String label, String v) {
        try {
            BigDecimal b = new BigDecimal(v.trim());
            if (b.signum() < 0) return label + " cannot be negative.";
            if (b.scale() > 2) return label + " can have at most 2 decimal places.";
            return b.compareTo(new BigDecimal("99999999.99")) <= 0 ? null : label + " is too large.";
        } catch (RuntimeException e) {
            return label + " must be a number.";
        }
    }

    public static String staffId(String v) {
        return v != null && STAFF_ID.matcher(v.trim()).matches() ? null : "Staff ID looks like S001.";
    }

    /** Returns the first non-null message, or null when everything is valid. */
    public static String firstError(String... errors) {
        for (String e : errors) {
            if (e != null) return e;
        }
        return null;
    }

    private static boolean isBlank(String v) {
        return v == null || v.trim().isEmpty();
    }
}
