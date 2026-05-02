package org.example.utils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.security.SecureRandom;

/**
 * Simple in-memory OTP store — no extra DB table required.
 * Each entry is keyed by phone number and expires after EXPIRY_MINUTES.
 * Codes are one-time: a successful verify() removes the entry immediately.
 */
public class OtpStore {

    private static final int EXPIRY_MINUTES = 5;
    private static final SecureRandom RNG = new SecureRandom();

    private record Entry(String code, LocalDateTime expiresAt) {}

    // phone → OTP entry
    private static final Map<String, Entry> STORE = new HashMap<>();

    /**
     * Generates a 6-digit OTP, stores it, and returns the code.
     * Calling again for the same phone overwrites the previous code.
     */
    public static String generateAndStore(String phone) {
        String code = String.format("%06d", RNG.nextInt(1_000_000));
        STORE.put(phone, new Entry(code, LocalDateTime.now().plusMinutes(EXPIRY_MINUTES)));
        return code;
    }

    /**
     * Verifies the code for a given phone.
     * Returns true only if the code matches AND has not expired.
     * On success the entry is removed (one-time use).
     */
    public static boolean verify(String phone, String code) {
        Entry entry = STORE.get(phone);
        if (entry == null) return false;

        if (LocalDateTime.now().isAfter(entry.expiresAt())) {
            STORE.remove(phone);
            return false;
        }

        if (!entry.code().equals(code)) return false;

        STORE.remove(phone);   // consumed
        return true;
    }

    /** Returns true if there is a still-valid OTP for this phone. */
    public static boolean hasPending(String phone) {
        Entry entry = STORE.get(phone);
        if (entry == null) return false;
        if (LocalDateTime.now().isAfter(entry.expiresAt())) {
            STORE.remove(phone);
            return false;
        }
        return true;
    }
}
