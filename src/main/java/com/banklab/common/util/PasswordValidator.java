package com.banklab.common.util;

public class PasswordValidator {
    // 최소 8자, 최대 30자, 영문자(a-zA-Z) 1개 이상, 숫자(0-9) 1개 이상, 특수문자 1개 이상
    private static final String PASSWORD_REGEX =
            "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[^a-zA-Z\\d]).{8,30}$";

    public static boolean isValid(String password) {
        return password != null && password.matches(PASSWORD_REGEX);
    }
}
