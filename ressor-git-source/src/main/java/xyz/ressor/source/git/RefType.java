package xyz.ressor.source.git;

public enum RefType {
    HASH(-1), HEAD(0), REMOTE(1), TAG(2);

    public final int id;

    RefType(int id) {
        this.id = id;
    }
}
