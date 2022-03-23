package ru.red.authenticationservice.util;

public class EmailUtil {
    private static final String EMAIL_REGEX = "^[\\w-.]+@([\\w-]+\\.){1,32}[\\w-]{2,4}$";

    private EmailUtil() {

    }

    public static boolean isEmail(String email) {
        return email.matches(EMAIL_REGEX);
    }
}
