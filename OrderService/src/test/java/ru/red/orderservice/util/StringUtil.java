package ru.red.orderservice.util;

import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StringUtil {
    public static String generateRandom(int length) {
        return Stream.generate(() -> new Random().nextInt('a', 'z'))
                .limit(length)
                .map(Character::toChars)
                .map(String::valueOf)
                .collect(Collectors.joining());
    }
}
