package xyz.ressor.commons.utils;

import java.util.concurrent.ThreadLocalRandom;

public class StringUtils {
    private static final int DEFAULT_LENGTH = 8;
    private static final char[] SYMBOLS;

    static {
        char[] symbols = new char[62];
        int pointer = 0;
        for (char c = 'a'; c <= 'z'; c++) {
            symbols[pointer++] = c;
        }
        for (char c = 'A'; c <= 'Z'; c++) {
            symbols[pointer++] = c;
        }
        for (char c = '0'; c <= '9'; c++) {
            symbols[pointer++] = c;
        }

        SYMBOLS = symbols;
    }

    public static String randomString() {
        return randomString(DEFAULT_LENGTH);
    }

    public static String randomString(int length) {
        StringBuilder sb = new StringBuilder();
        int sl = SYMBOLS.length;
        for (int i = 0; i < length; i++) {
            sb.append(SYMBOLS[ThreadLocalRandom.current().nextInt(0, sl)]);
        }
        return sb.toString();
    }

}
