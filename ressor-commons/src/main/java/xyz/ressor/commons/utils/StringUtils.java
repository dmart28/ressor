package xyz.ressor.commons.utils;

import java.util.concurrent.ThreadLocalRandom;

public class StringUtils {
    private static final int DEFAULT_LENGTH = 8;
    private static final char[] SYMBOLS;

    static {
        var symbols = new char[62];
        var pointer = 0;
        for (var c = 'a'; c <= 'z'; c++) {
            symbols[pointer++] = c;
        }
        for (var c = 'A'; c <= 'Z'; c++) {
            symbols[pointer++] = c;
        }
        for (var c = '0'; c <= '9'; c++) {
            symbols[pointer++] = c;
        }

        SYMBOLS = symbols;
    }

    public static String randomString() {
        return randomString(DEFAULT_LENGTH);
    }

    public static String randomString(int length) {
        var sb = new StringBuilder();
        var sl = SYMBOLS.length;
        for (var i = 0; i < length; i++) {
            sb.append(SYMBOLS[ThreadLocalRandom.current().nextInt(0, sl)]);
        }
        return sb.toString();
    }

}
