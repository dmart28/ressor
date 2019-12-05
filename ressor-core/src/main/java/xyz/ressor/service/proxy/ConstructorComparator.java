package xyz.ressor.service.proxy;

import java.lang.reflect.Constructor;
import java.util.Comparator;

import static java.lang.reflect.Modifier.*;

public class ConstructorComparator implements Comparator<Constructor> {
    private static final Comparator<Constructor> INSTANCE = new ConstructorComparator().reversed();

    public static Comparator<Constructor> instance() {
        return INSTANCE;
    }

    @Override
    public int compare(Constructor a, Constructor b) {
        int wa = weight(a);
        int wb = weight(b);
        if (wa == wb) {
            return Integer.compare(b.getParameterCount(), a.getParameterCount());
        } else {
            return Integer.compare(wa, wb);
        }
    }

    private int weight(Constructor constructor) {
        int mult = 2;
        int modifiers = constructor.getModifiers();
        if (isPrivate(modifiers)) {
            mult = 1;
        } else if (isProtected(modifiers)) {
            mult = 3;
        } else if (isPublic(modifiers)) {
            mult = 4;
        }
        return mult;
    }
}
