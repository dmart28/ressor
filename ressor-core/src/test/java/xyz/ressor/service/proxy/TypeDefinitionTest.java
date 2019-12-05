package xyz.ressor.service.proxy;

import org.junit.jupiter.api.Test;
import xyz.ressor.service.proxy.model.*;

import static org.assertj.core.api.Assertions.assertThat;

public class TypeDefinitionTest {

    @Test
    public void testVeryBasicClass() {
        TypeDefinition td = TypeDefinition.of(VeryBasicClass.class);
        assertThat(td).isNotNull();
        assertThat(td.getDefaultConstructor()).isNotNull();
        assertThat(td.isInterface()).isFalse();
        assertThat(td.isFinal()).isFalse();
        assertThat(td.getDefaultConstructor().getParameterCount()).isZero();
        assertThat(td.getDefaultArguments()).hasSize(0);
    }

    @Test
    public void testPublicClassMultipleConstructors() {
        TypeDefinition td = TypeDefinition.of(PublicClassMultipleConstructors.class);
        assertThat(td).isNotNull();
        assertThat(td.getDefaultConstructor()).isNotNull();
        assertThat(td.isInterface()).isFalse();
        assertThat(td.isFinal()).isFalse();
        assertThat(td.getDefaultConstructor().getParameterCount()).isZero();
        assertThat(td.getDefaultArguments()).hasSize(0);
    }

    @Test
    public void testPublicClassConstructorAnnotated() {
        TypeDefinition td = TypeDefinition.of(PublicClassConstructorAnnotated.class);
        assertThat(td).isNotNull();
        assertThat(td.getDefaultConstructor().getParameterCount()).isEqualTo(0);
        assertThat(td.getDefaultArguments()).hasSize(0);
    }

    @Test
    public void testInnerClass() {
        TypeDefinition td = TypeDefinition.of(TestInnerClass.class);
        assertThat(td.getDefaultConstructor()).isNotNull();
        assertThat(td.getDefaultConstructor().getParameterCount()).isOne();
        assertThat(td.isFinal()).isFalse();
        assertThat(td.isInterface()).isFalse();
    }

    @Test
    public void testPackagePrivateClass() throws Exception {
        TypeDefinition td = TypeDefinition.of(Class.forName("xyz.ressor.service.proxy.model.PackagePrivateClass"));
        assertThat(td.getDefaultConstructor()).isNotNull();
        assertThat(td.getDefaultConstructor().getParameterCount()).isZero();
        assertThat(td.isFinal()).isFalse();
        assertThat(td.isInterface()).isFalse();
    }

    @Test
    public void testInnerStaticClass() {
        TypeDefinition td = TypeDefinition.of(TestInnerStaticClass.class);
        assertThat(td.getDefaultConstructor()).isNotNull();
        assertThat(td.getDefaultConstructor().getParameterCount()).isZero();
    }

    @Test
    public void testAbstractClass() {
        TypeDefinition td = TypeDefinition.of(TestAbstractConstructor.class);
        assertThat(td).isNotNull();
        assertThat(td.getDefaultConstructor()).isNotNull();
        assertThat(td.getDefaultConstructor().getParameterCount()).isEqualTo(2);
        assertThat(td.isFinal()).isFalse();
        assertThat(td.isInterface()).isFalse();
    }

    @Test
    public void testFinalClass() {
        TypeDefinition td = TypeDefinition.of(FinalClass.class);
        assertThat(td.isFinal()).isTrue();
        assertThat(td.isInterface()).isFalse();
    }

    @Test
    public void testInterface() {
        TypeDefinition td = TypeDefinition.of(Interface.class);
        assertThat(td.isFinal()).isFalse();
        assertThat(td.isInterface()).isTrue();
    }


    public class TestInnerClass {
        private int i = 0;
    }

    public static class TestInnerStaticClass {
        private int i = 0;
    }

    public abstract class TestAbstractConstructor {
        int i;
        abstract void main();

        protected TestAbstractConstructor(int i) {
            this.i = i;
        }

        protected TestAbstractConstructor(int i, String d) {
            this.i = i + Integer.parseInt(d);
        }
    }

}
