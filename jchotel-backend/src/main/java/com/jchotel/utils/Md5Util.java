package com.jchotel.utils;

import org.springframework.util.DigestUtils;

public class Md5Util {

    public static String encrypt(String password) {
        if (password == null) {
            return null;
        }
        return DigestUtils.md5DigestAsHex(password.getBytes());
    }
}
