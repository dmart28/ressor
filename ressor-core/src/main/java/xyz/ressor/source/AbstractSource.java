package xyz.ressor.source;

public abstract class AbstractSource<R extends ResourceId> implements Source<R> {
    private final String id;

    public AbstractSource() {
        this(null);
    }

    public AbstractSource(String id) {
        this.id = id;
    }

    @Override
    public String id() {
        return id;
    }
}
