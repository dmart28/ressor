package xyz.ressor.integration;

import org.junit.jupiter.api.Test;
import xyz.ressor.config.RessorConfig;

import java.util.concurrent.ForkJoinPool;

import static org.assertj.core.api.Assertions.assertThat;

public class RessorConfigTest {

    @Test
    public void configTest() {
        var rc = new RessorConfig();
        assertThat(rc.threadPool()).isNull();
        assertThat(rc.pollingThreads()).isNull();
        assertThat(rc.isCacheClasses()).isNull();

        var nrc = new RessorConfig(rc);
        assertThat(nrc.threadPool()).isNull();
        assertThat(nrc.pollingThreads()).isEqualTo(Runtime.getRuntime().availableProcessors());
        assertThat(nrc.isCacheClasses()).isTrue();

        rc.threadPool(ForkJoinPool.commonPool());

        nrc = new RessorConfig(rc);
        assertThat(nrc.threadPool()).isSameAs(ForkJoinPool.commonPool());
    }

}
