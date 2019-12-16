package xyz.ressor.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import xyz.ressor.Ressor;
import xyz.ressor.service.action.Actions;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static xyz.ressor.service.action.Actions.*;
import static xyz.ressor.utils.TestUtils.*;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class ReloadActionsTest {
    private Ressor ressor = Ressor.create();

    @Test
    public void testBasicReloadAction() {
        CharSequence string = createTestService();

        AtomicBoolean toggle = new AtomicBoolean();
        ressor.actions().onReload(string, s -> toggle.get());

        ressorService(string).reload(string("Never found"));
        assertThat(string).isEqualTo("Test data");

        toggle.set(true);

        ressorService(string).reload(string("Was found"));
        assertThat(string).isEqualTo("Was found");
    }

    @Test
    public void testReloadActionOperators() {
        testReloadActionOperatorsPrivate(Actions::or, Actions::and);
    }

    @Test
    public void testReloadActionOperatorsParallel() {
        testReloadActionOperatorsPrivate(Actions::orParallel, Actions::andParallel);
    }

    @Test
    public void testTriggerReloadActions() {
        StringBuilder dataSource2 = new StringBuilder();

        CharSequence string1 = stubSource(ressor.service(CharSequence.class)
                .string()
                .factory(Function.identity()))
                .build();
        CharSequence string2 = stringBuilderSource(dataSource2, ressor.service(CharSequence.class)
                .string()
                .factory(Function.identity()))
                .build();

        dataSource2.append("New Data 2");

        assertThat(string1).isEqualTo("");
        assertThat(string2).isEqualTo("");

        ressor.actions().onReload(string1, triggerAndWaitReload(string2));

        ressorService(string1).reload(stringVersioned("New Data 1"));

        assertThat(string2).isEqualTo("New Data 2");
        assertThat(string1).isEqualTo("New Data 1");

        ressor.actions().resetAll(string1);
        ressorService(string1).reload(stringVersioned(""));
        ressorService(string2).reload(stringVersioned(""));

        ressor.actions().onReload(string1, triggerReload(string2));

        ressorService(string1).reload(stringVersioned("New Data 1"));

        assertThat(string1).isEqualTo("New Data 1");
        await().atMost(10, TimeUnit.SECONDS)
                .pollInterval(10, TimeUnit.MILLISECONDS)
                .until(() -> string2.equals("New Data 2"));

        ressor.actions().resetAll(string1);
        ressor.actions().onReload(string1, abortIf(string2, (v1, v2) -> v1.val().equals(v2.val())));

        ressorService(string1).reload(stringVersioned("New Data 2"));
        assertThat(string1).isEqualTo("New Data 2");

        ressorService(string1).reload(stringVersioned("New Data 3"));
        assertThat(string1).isEqualTo("New Data 2");
    }

    public void testReloadActionOperatorsPrivate(Function<ReloadAction[], ReloadAction> orF,
                                          Function<ReloadAction[], ReloadAction> andF) {
        CharSequence string = createTestService();

        AtomicBoolean toggle1 = new AtomicBoolean();
        AtomicBoolean toggle2 = new AtomicBoolean();

        ressor.actions().onReload(string, andF.apply(Arrays.<ReloadAction>asList(
                s -> toggle1.get(),
                s -> toggle2.get()).toArray(new ReloadAction[0])));

        ressorService(string).reload(string("Never found"));
        assertThat(string).isEqualTo("Test data");

        toggle1.set(true);

        ressorService(string).reload(string("Never found"));
        assertThat(string).isEqualTo("Test data");

        toggle2.set(true);

        ressorService(string).reload(string("Was found"));
        assertThat(string).isEqualTo("Was found");

        ressor.actions().resetAll(string);
        toggle1.set(false);
        toggle2.set(false);
        ressor.actions().onReload(string, orF.apply(Arrays.<ReloadAction>asList(
                s -> toggle1.get(),
                s -> toggle2.get()).toArray(new ReloadAction[0])));

        ressorService(string).reload(string("Never found"));
        assertThat(string).isEqualTo("Was found");

        toggle2.set(true);

        ressorService(string).reload(string("Never found"));
        assertThat(string).isEqualTo("Never found");
    }

    private CharSequence createTestService() {
        CharSequence string = ressor.service(CharSequence.class)
                .fileSource("classpath:fs/simpleText.txt")
                .string()
                .factory(Function.identity())
                .build();
        assertThat(string).isEqualTo("Test data");
        return string;
    }

}
