package xyz.ressor.source.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.filter.CommitTimeRevFilter;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ressor.commons.utils.Exceptions;
import xyz.ressor.source.LoadedResource;
import xyz.ressor.source.Source;
import xyz.ressor.source.Subscription;

import java.io.InputStream;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;

public class GitSource implements Source {
    private static final Logger log = LoggerFactory.getLogger(GitSource.class);
    public static final TransportConfigCallback EMPTY_TRANSPORT_CONFIG = transport -> {};
    private final Git git;
    private final TransportConfigCallback transportConfig;
    private final String filePath;
    private final String branchName;
    private final ObjectId branchId;
    private final boolean asyncPull;
    private final boolean hasRemotes;

    public GitSource(Git git, TransportConfigCallback transportConfig,
                     String filePath, String branchName, boolean asyncPull) {
        this.git = git;
        this.transportConfig = transportConfig;
        this.filePath = filePath;
        this.branchName = branchName;
        try {
            this.branchId = git.getRepository().resolve(branchName);
            if (branchId == null) {
                throw new IllegalArgumentException("Unable to find any branch with name " + branchName);
            }
            this.hasRemotes = git.remoteList().call().size() > 0;
        } catch (Throwable t) {
            throw Exceptions.wrap(t);
        }
        this.asyncPull = asyncPull;
    }

    @Override
    public LoadedResource loadIfModified(long lastModifiedMillis) {
        try {
            pull();
            var logs = git.log()
                    .setRevFilter(CommitTimeRevFilter.after(lastModifiedMillis))
                    .addPath(filePath)
                    .setMaxCount(1)
                    .add(branchId)
                    .call().iterator();
            if (logs.hasNext()) {
                var commit = logs.next();
                var stream = getContent(commit, filePath);
                return new LoadedResource(stream, commit.getCommitTime() * 1000L, filePath);
            } else {
                return null;
            }
        } catch (Throwable t) {
            throw Exceptions.wrap(t);
        }
    }

    @Override
    public boolean isListenable() {
        return false;
    }

    @Override
    public Subscription subscribe(Consumer<LoadedResource> listener) {
        return null;
    }

    private void pull() {
        if (asyncPull) {
            ForkJoinPool.commonPool().submit(this::doPull);
        } else {
            doPull();
        }
    }

    protected void doPull() {
        try {
            if (hasRemotes) {
                git.pull().setRemoteBranchName(branchName).setTransportConfigCallback(transportConfig).call();
            }
        } catch (Throwable t) {
            log.error("doPull error: {}", t.getMessage(), t);
        }
    }

    protected InputStream getContent(RevCommit commit, String path) {
        try (var treeWalk = TreeWalk.forPath(git.getRepository(), path, commit.getTree())) {
            var blobId = treeWalk.getObjectId(0);
            try (var objectReader = git.getRepository().newObjectReader()) {
                var objectLoader = objectReader.open(blobId);
                return objectLoader.openStream();
            }
        } catch (Throwable t) {
            throw Exceptions.wrap(t);
        }
    }
}
