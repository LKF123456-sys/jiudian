package com.jchotel.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.DigestUtils;

public class PasswordUtil {

    private static final BCryptPasswordEncoder BCRYPT_ENCODER = new BCryptPasswordEncoder();
    private static final String BCRYPT_PREFIX = "$2a$";

    public static String encode(String rawPassword) {
        return BCRYPT_ENCODER.encode(rawPassword);
    }

    public static boolean matches(String rawPassword, String encodedPassword) {
        if (encodedPassword == null) return false;
        if (encodedPassword.startsWith(BCRYPT_PREFIX)) {
            return BCRYPT_ENCODER.matches(rawPassword, encodedPassword);
        }
        return DigestUtils.md5DigestAsHex(rawPassword.getBytes()).equals(encodedPassword);
    }

    public static boolean needsUpgrade(String encodedPassword) {
        return encodedPassword == null || !encodedPassword.startsWith(BCRYPT_PREFIX);
    }
}
