package xyz.ressor.source.git;

import org.eclipse.jgit.lib.ObjectId;

import java.util.Objects;

public class GitRef {
    private final String fullName;
    private final String name;
    private final RefType type;

    public GitRef(String refName) {
        this.fullName = refName;
        String[] split = refName.split("/");
        this.name = split[split.length - 1];
        if (ObjectId.isId(refName)) {
            this.type = RefType.HASH;
        } else if (split.length >= 3) {
            switch (split[1]) {
                case "heads": this.type = RefType.HEAD; break;
                case "remotes": this.type = RefType.REMOTE; break;
                case "tags": this.type = RefType.TAG; break;
                default: throw new IllegalArgumentException("Unknown ref type: " + split[1]);
            }
        } else {
            this.type = RefType.HEAD;
        }
    }

    public GitRef(String refName, RefType type) {
        this.fullName = refName;
        String[] split = refName.split("/");
        this.name = split[split.length - 1];
        this.type = type;
    }

    public String getFullName() {
        return fullName;
    }

    public String getName() {
        return name;
    }

    public RefType getType() {
        return type;
    }

    public boolean isHash() {
        return type == RefType.HASH;
    }

    public boolean isTag() {
        return type == RefType.TAG;
    }

    public boolean isBranch() {
        return type == RefType.HEAD;
    }

    public boolean isRemoteBranch() {
        return type == RefType.REMOTE;
    }

    public boolean isBranchType() {
        return isBranch() || isRemoteBranch();
    }

    public boolean isShort() {
        return fullName.equalsIgnoreCase(name);
    }

    public boolean isConnectedWith(GitRef ref) {
        return this.equals(ref) || ref != null && ref.getName().equals(name) && (type.id | ref.type.id) == 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GitRef gitRef = (GitRef) o;
        return Objects.equals(name, gitRef.name) &&
                type == gitRef.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type);
    }

    @Override
    public String toString() {
        return "GitRef{" +
                "name='" + name + '\'' +
                ", type=" + type +
                '}';
    }

}
