package xyz.ressor.source;

import org.jetbrains.annotations.Nullable;

public interface SourceVersion {

    @Nullable
    <V> V val();

    SourceVersion EMPTY = new SourceVersion() {
        @Override
        public <V> V val() {
            return null;
        }
    };

}
