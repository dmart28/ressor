package xyz.ressor.service.proxy.model;

public class PublicClassMultipleConstructors {
    private int i = 0;

    public PublicClassMultipleConstructors() {
    }

    public PublicClassMultipleConstructors(int i) {
        this.i = i;
    }

    private PublicClassMultipleConstructors(int i, long j) {
        this.i = i + (int) j;
    }
}
