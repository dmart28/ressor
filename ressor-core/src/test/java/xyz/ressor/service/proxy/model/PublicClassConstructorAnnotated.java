package xyz.ressor.service.proxy.model;

import xyz.ressor.commons.annotations.ProxyConstructor;

public class PublicClassConstructorAnnotated {
    private int i = 0;

    public PublicClassConstructorAnnotated() {
    }

    public PublicClassConstructorAnnotated(int i) {
        this.i = i;
    }

    @ProxyConstructor
    private PublicClassConstructorAnnotated(int i, long j) {
        this.i = i + (int) j;
    }
}
