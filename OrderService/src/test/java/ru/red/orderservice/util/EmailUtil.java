package ru.red.orderservice.util;

import java.util.Random;

public class EmailUtil {
    public static String generateRandomEmail() {
        var random = new Random();
        return random
                .ints(random.nextInt(5, 16), 'a', 'z')
                .mapToObj(i -> (char) i)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .append("@example.com")
                .toString();
    }
}
