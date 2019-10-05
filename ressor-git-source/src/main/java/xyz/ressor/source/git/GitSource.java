package xyz.ressor.source.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.filter.AndRevFilter;
import org.eclipse.jgit.revwalk.filter.CommitTimeRevFilter;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ressor.commons.utils.Exceptions;
import xyz.ressor.source.LoadedResource;
import xyz.ressor.source.Source;
import xyz.ressor.source.SourceVersion;
import xyz.ressor.source.Subscription;
import xyz.ressor.source.version.LastModified;

import java.io.InputStream;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;

import static xyz.ressor.source.git.GitRev.exact;

public class GitSource implements Source {
    private static final Logger log = LoggerFactory.getLogger(GitSource.class);
    public static final TransportConfigCallback EMPTY_TRANSPORT_CONFIG = transport -> {};
    protected static final SourceVersion EMPTY = new LastModified(-1L);
    protected final Git git;
    protected final TransportConfigCallback transportConfig;
    protected final String filePath;
    protected final GitRef refValue;
    protected final ObjectId objectId;
    protected final boolean asyncPull;
    protected final boolean hasRemotes;

    public GitSource(Git git, TransportConfigCallback transportConfig,
                     String filePath, GitRef ref, boolean asyncPull) {
        this.git = git;
        this.transportConfig = transportConfig;
        this.filePath = filePath;
        var refValue = ref;
        try {
            this.objectId = git.getRepository().resolve(refValue.getFullName());
            if (objectId == null) {
                throw new IllegalArgumentException("Unable to find a ref with id [" + refValue.getFullName() + "]," +
                        " try passing a full ref name (like 'refs/heads/develop', 'refs/tags/tag-1', etc) or another one.");
            } else if (refValue.isShort() && refValue.isBranch()) {
                var isBranch = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call()
                        .stream()
                        .map(r -> new GitRef(r.getName()))
                        .anyMatch(r -> r.isConnectedWith(ref));
                if (!isBranch) {
                    refValue = new GitRef(refValue.getName(), RefType.TAG);
                }
            }
            this.hasRemotes = git.remoteList().call().size() > 0;
            this.refValue = refValue;
        } catch (Throwable t) {
            throw Exceptions.wrap(t);
        }
        this.asyncPull = asyncPull;
    }

    @Override
    public LoadedResource loadIfModified(SourceVersion version) {
        try {
            pull();
            var filter = CommitTimeRevFilter.after((long) version.val());
            var logsCmd = git.log().all();

            if (refValue.isHash()) {
                logsCmd.add(objectId).setRevFilter(AndRevFilter.create(filter, exact(objectId)));
            } else {
                if (refValue.isTag()) {
                    var objectId = git.getRepository().findRef(refValue.getFullName()).getPeeledObjectId();
                    logsCmd.setRevFilter(AndRevFilter.create(filter, exact(objectId)));
                } else {
                    logsCmd.addPath(filePath).setRevFilter(filter);
                }
            }

            for (var commit : logsCmd.call()) {
                if (refValue.isBranchType()) {
                    var branches = git.branchList()
                            .setListMode(ListBranchCommand.ListMode.ALL)
                            .setContains(commit.getId().name())
                            .call();

                    for (var branchRefValue : branches) {
                        var branchRef = new GitRef(branchRefValue.getName());
                        if (refValue.isConnectedWith(branchRef)) {
                            return loadFromCommit(commit);
                        }
                    }
                } else {
                    return loadFromCommit(commit);
                }
            }
            return null;
        } catch (Throwable t) {
            throw Exceptions.wrap(t);
        }
    }

    @Override
    public boolean isListenable() {
        return false;
    }

    @Override
    public Subscription subscribe(Runnable listener) {
        return null;
    }

    @Override
    public String describe() {
        return git.toString() + ";" + filePath;
    }

    @Override
    public SourceVersion emptyVersion() {
        return EMPTY;
    }

    protected void pull() {
        if (asyncPull) {
            ForkJoinPool.commonPool().submit(this::doPull);
        } else {
            doPull();
        }
    }

    protected void doPull() {
        try {
            if (hasRemotes) {
                log.debug("Performing repository fetch");
                git.fetch().setTransportConfigCallback(transportConfig).call();
            }
        } catch (Throwable t) {
            log.error("doPull error: {}", t.getMessage(), t);
        }
    }

    protected LoadedResource loadFromCommit(RevCommit commit) {
        var stream = getContent(commit, filePath);
        return stream == null ? null : new LoadedResource(stream, new LastModified(commit.getCommitTime() * 1000L), filePath);
    }

    protected InputStream getContent(RevCommit commit, String path) {
        try (var treeWalk = TreeWalk.forPath(git.getRepository(), path, commit.getTree())) {
            if (treeWalk != null) {
                var blobId = treeWalk.getObjectId(0);
                try (var objectReader = git.getRepository().newObjectReader()) {
                    var objectLoader = objectReader.open(blobId);
                    return objectLoader.openStream();
                }
            } else {
                return null;
            }
        } catch (Throwable t) {
            throw Exceptions.wrap(t);
        }
    }
}
