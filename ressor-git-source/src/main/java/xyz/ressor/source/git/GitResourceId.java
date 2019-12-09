package xyz.ressor.source.git;

import xyz.ressor.source.ResourceId;

public class GitResourceId implements ResourceId {
    private final String filePath;
    private final GitRef refValue;

    public String getFilePath() {
        return filePath;
    }

    public GitRef getRefValue() {
        return refValue;
    }

    public GitResourceId(String filePath, String refName) {
        this.filePath = filePath;
        this.refValue = new GitRef(refName);
    }

    public GitResourceId(String filePath, String refName, RefType refType) {
        this.filePath = filePath;
        this.refValue = new GitRef(refName, refType);
    }

    @Override
    public String describe() {
        return refValue + " [" + filePath + "]";
    }

    @Override
    public Class<?> sourceType() {
        return GitSource.class;
    }
}
