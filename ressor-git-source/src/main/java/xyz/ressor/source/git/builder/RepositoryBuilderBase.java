package xyz.ressor.source.git.builder;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.util.FS;
import xyz.ressor.source.git.GitRef;
import xyz.ressor.source.git.GitSource;
import xyz.ressor.source.git.RefType;

public abstract class RepositoryBuilderBase<T extends RepositoryBuilderBase> {
    protected String ref = "master";
    protected RefType refType;
    protected String repositoryDirectory;
    protected String privateKeyPath;
    protected String privateKeyPassphrase;
    protected String filePath;
    protected boolean asyncPull = false;

    public T refValue(String ref) {
        this.ref = ref;
        return getThis();
    }

    public T refValue(String ref, RefType refType) {
        this.ref = ref;
        this.refType = refType;
        return getThis();
    }

    public T repositoryDirectory(String repositoryDirectory) {
        this.repositoryDirectory = repositoryDirectory;
        return getThis();
    }

    public T privateKeyPath(String privateKeyPath) {
        this.privateKeyPath = privateKeyPath;
        return getThis();
    }

    public T privateKeyPassphrase(String privateKeyPassphrase) {
        this.privateKeyPassphrase = privateKeyPassphrase;
        return getThis();
    }

    public T filePath(String filePath) {
        this.filePath = filePath;
        return getThis();
    }

    public T asyncPull(boolean asyncPull) {
        this.asyncPull = asyncPull;
        return getThis();
    }

    protected GitRef ref() {
        return refType == null ? new GitRef(ref) : new GitRef(ref, refType);
    }

    protected TransportConfigCallback createTransportConfig() {
        return privateKeyPath == null ? GitSource.EMPTY_TRANSPORT_CONFIG : transport -> {
            var ssh = (SshTransport) transport;
            ssh.setSshSessionFactory(new JschConfigSessionFactory() {
                @Override
                protected void configure(OpenSshConfig.Host hc, Session session) {
                    session.setConfig("StrictHostKeyChecking", "no");
                }

                @Override
                protected JSch createDefaultJSch(FS fs) throws JSchException {
                    var jSch = super.createDefaultJSch(fs);
                    jSch.addIdentity(privateKeyPath, privateKeyPassphrase);
                    return jSch;
                }
            });
        };
    }

    private T getThis() {
        return (T) this;
    }

}
