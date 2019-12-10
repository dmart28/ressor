package xyz.ressor.service.action;

import xyz.ressor.config.RessorConfig;
import xyz.ressor.service.RessorService;
import xyz.ressor.source.SourceVersion;

import java.util.function.BiPredicate;

public class VersionCheckAction extends ServiceBasedAction {
    private final BiPredicate<SourceVersion, SourceVersion> versionPredicate;

    public VersionCheckAction(RessorService service, BiPredicate<SourceVersion, SourceVersion> versionPredicate) {
        super(service);
        this.versionPredicate = versionPredicate;
    }

    @Override
    public boolean perform(RessorConfig config, RessorService target) {
        try {
            return !versionPredicate.test(target.latestVersion(), getService().latestVersion());
        } catch (Throwable t) {
            return false;
        }
    }
}
