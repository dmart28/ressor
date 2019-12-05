package xyz.ressor.ext.spring;

import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodCall;
import org.springframework.beans.factory.FactoryBean;
import xyz.ressor.ext.ServiceExtension;

import static net.bytebuddy.matcher.ElementMatchers.named;

public class FactoryBeanExtension implements ServiceExtension {

    @Override
    public <T> DynamicType.Builder<T> interceptProxy(DynamicType.Builder<T> builder, Class<T> type) {
        return builder.implement(FactoryBean.class)
                .method(named("getObject"))
                .intercept(MethodCall.invoke(named("instance")))
                .method(named("getObjectType"))
                .intercept(MethodCall.invoke(named("underlyingType")));
    }
}
