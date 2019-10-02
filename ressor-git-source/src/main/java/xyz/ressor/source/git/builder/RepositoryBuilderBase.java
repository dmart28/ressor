package xyz.ressor.source.git.builder;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.util.FS;
import xyz.ressor.source.git.GitSource;

public abstract class RepositoryBuilderBase<T extends RepositoryBuilderBase> {
    protected String branch = "master";
    protected String repositoryDirectory;
    protected String privateKeyPath;
    protected String filePath;
    protected boolean asyncPull = false;

    public T branch(String branch) {
        this.branch = branch;
        return (T) this;
    }

    public T repositoryDirectory(String repositoryDirectory) {
        this.repositoryDirectory = repositoryDirectory;
        return (T) this;
    }

    public T privateKeyPath(String privateKeyPath) {
        this.privateKeyPath = privateKeyPath;
        return (T) this;
    }

    public T filePath(String filePath) {
        this.filePath = filePath;
        return (T) this;
    }

    public T asyncPull(boolean asyncPull) {
        this.asyncPull = asyncPull;
        return (T) this;
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
                    jSch.addIdentity(privateKeyPath);
                    return jSch;
                }
            });
        };
    }

}
