package xyz.ressor.source.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.filter.AndRevFilter;
import org.eclipse.jgit.revwalk.filter.CommitTimeRevFilter;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.ressor.commons.utils.Exceptions;
import xyz.ressor.source.*;
import xyz.ressor.source.version.LastModified;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;

import static java.lang.String.format;
import static xyz.ressor.source.git.GitRev.exact;

public class GitSource extends AbstractSource<GitResourceId> implements NonListenableSource<GitResourceId> {
    private static final Logger log = LoggerFactory.getLogger(GitSource.class);
    public static final TransportConfigCallback EMPTY_TRANSPORT_CONFIG = transport -> {};
    protected static final SourceVersion EMPTY = new LastModified(-1L);
    protected final Git git;
    protected final TransportConfigCallback transportConfig;
    protected final boolean asyncPull;
    protected final boolean hasRemotes;
    protected final Map<String, ObjectId> objectIds = new ConcurrentHashMap<>();
    protected final Map<GitRef, GitRef> branchMapping = new ConcurrentHashMap<>();

    public GitSource(Git git, TransportConfigCallback transportConfig, boolean asyncPull) {
        this.git = git;
        this.transportConfig = transportConfig;
        try {
            this.hasRemotes = git.remoteList().call().size() > 0;
        } catch (GitAPIException e) {
            throw Exceptions.wrap(e);
        }
        this.asyncPull = asyncPull;
    }

    @Override
    public LoadedResource loadIfModified(GitResourceId resourceId, SourceVersion version) {
        try {
            pull();
            GitRef refValue = mapIfRequired(resourceId.getRefValue());
            RevFilter filter = CommitTimeRevFilter.after((long) version.val());
            LogCommand logsCmd = git.log().all();

            if (refValue.isHash()) {
                ObjectId objectId = getObjectId(resourceId.getRefValue());
                logsCmd.add(objectId).setRevFilter(AndRevFilter.create(filter, exact(objectId)));
            } else {
                if (refValue.isTag()) {
                    Ref ref = git.getRepository().findRef(refValue.getFullName());
                    ObjectId objectId = checkExists(refValue.getFullName(), ref != null ? ref.getPeeledObjectId() : null);
                    logsCmd.setRevFilter(AndRevFilter.create(filter, exact(objectId)));
                } else {
                    logsCmd.addPath(resourceId.getFilePath()).setRevFilter(filter);
                }
            }

            for (RevCommit commit : logsCmd.call()) {
                if (refValue.isBranchType()) {
                    List<Ref> branches = git.branchList()
                            .setListMode(ListBranchCommand.ListMode.ALL)
                            .setContains(commit.getId().name())
                            .call();

                    for (Ref branchRefValue : branches) {
                        GitRef branchRef = new GitRef(branchRefValue.getName());
                        if (refValue.isConnectedWith(branchRef)) {
                            return loadFromCommit(resourceId, commit);
                        }
                    }
                } else {
                    return loadFromCommit(resourceId, commit);
                }
            }
            return null;
        } catch (Throwable t) {
            throw Exceptions.wrap(t);
        }
    }

    private GitRef mapIfRequired(GitRef refValue) {
        return branchMapping.computeIfAbsent(refValue, k -> {
           if (k.isShort() && k.isBranch()) {
               try {
                   boolean isBranch = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call()
                           .stream()
                           .map(r -> new GitRef(r.getName()))
                           .anyMatch(r -> r.isConnectedWith(k));
                   if (!isBranch) {
                       return new GitRef(k.getName(), RefType.TAG);
                   } else {
                       return k;
                   }
               } catch (Throwable t) {
                   log.error("Unable to detect branch status of ref {}: {}", k, t.getMessage(), t);
                   return k;
               }
           } else {
               return k;
           }
        });
    }

    public ObjectId getObjectId(GitRef refValue) {
        return objectIds.computeIfAbsent(refValue.getFullName(), k -> {
            try {
                return checkExists(k, git.getRepository().resolve(k));
            } catch (IOException e) {
                throw Exceptions.wrap(e);
            }
        });
    }

    private ObjectId checkExists(String refName, ObjectId objectId) {
        if (objectId == null) {
            throw new IllegalArgumentException("Unable to find a ref with id [" + refName + "]," +
                    " try passing a full ref name (like 'refs/heads/develop', 'refs/tags/tag-1', etc) or another one.");
        }
        return objectId;
    }

    @Override
    public String describe() {
        return git.toString();
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

    protected LoadedResource loadFromCommit(GitResourceId resourceId, RevCommit commit) {
        InputStream stream = getContent(commit, resourceId.getFilePath());
        return stream == null ? null : new LoadedResource(stream, new LastModified(commit.getCommitTime() * 1000L), resourceId);
    }

    protected InputStream getContent(RevCommit commit, String path) {
        try (TreeWalk treeWalk = TreeWalk.forPath(git.getRepository(), path, commit.getTree())) {
            if (treeWalk != null) {
                ObjectId blobId = treeWalk.getObjectId(0);
                try (ObjectReader objectReader = git.getRepository().newObjectReader()) {
                    ObjectLoader objectLoader = objectReader.open(blobId);
                    return objectLoader.openStream();
                }
            } else {
                throw new FileNotFoundException(format("No file [%s] found for commit [%s]", path, commit.getId()));
            }
        } catch (Throwable t) {
            throw Exceptions.wrap(t);
        }
    }
}
