package xyz.ressor.source.git;

import org.eclipse.jgit.errors.StopWalkException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;

public class GitRev {

    public static RevFilter exact(ObjectId commitId) {
        return new RevFilter() {
            @Override
            public boolean include(RevWalk walker, RevCommit cmit) throws StopWalkException {
                return cmit != null && cmit.getId().equals(commitId);
            }

            @Override
            public RevFilter clone() {
                return exact(commitId);
            }
        };
    }

}
