package ru.red.authenticationservice.util;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class StringUtil {
    private static final Random random = new Random();

    private StringUtil() {

    }

    /**
     * Wrap input string by length
     *
     * @param str    input
     * @param length max wrapping length
     * @return wrapped multiline {@link String}
     */
    public static String wrapString(String str, int length) {
        List<String> chars = str.codePoints()
                .filter(c -> c != '\n')
                .mapToObj(Character::toString)
                .collect(Collectors.toList());
        for (int i = 0; i < chars.size(); i++) {
            if (i % (length + 1) == 0) {
                chars.add(i, "\n");
            }
        }
        return String.join("", chars);
    }

    /**
     * Generate random Alphanumerical string
     *
     * @param length String length
     * @return Random {@link String}
     */
    public static String generateRandomString(int length) {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'

        return random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97)) // Select only numbers and characters
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    public static String generateRandomEmail(int length) {
        return generateRandomString(length) + "@example.com";
    }
}
