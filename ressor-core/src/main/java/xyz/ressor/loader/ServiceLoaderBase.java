package xyz.ressor.loader;

import xyz.ressor.service.RessorService;
import xyz.ressor.source.Source;

public abstract class ServiceLoaderBase {
    protected final RessorService service;
    protected final Source source;

    public abstract void stop();

    protected ServiceLoaderBase(RessorService service, Source source) {
        this.service = service;
        this.source = source;
    }

}
