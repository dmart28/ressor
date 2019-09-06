package xyz.ressor.service.proxy.model;

public class ClassPrivateConstructor {
    private int i = 0;

    private ClassPrivateConstructor() {
    }

    private ClassPrivateConstructor(int i) {
        this.i = i;
    }

}
