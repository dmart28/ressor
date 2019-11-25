package xyz.ressor.utils;

import java.io.IOException;
import java.io.InputStream;

public class ThrowingInputStream extends InputStream {

    @Override
    public int read() throws IOException {
        throw new IOException();
    }

}
