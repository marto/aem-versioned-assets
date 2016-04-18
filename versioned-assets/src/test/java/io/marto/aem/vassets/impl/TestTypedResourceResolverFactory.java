package io.marto.aem.vassets.impl;

import org.apache.commons.lang3.StringUtils;

import io.marto.aem.lib.RepositoryLoginException;
import io.marto.aem.lib.RepositoryTask;
import io.marto.aem.lib.TypedResourceResolver;
import io.marto.aem.lib.TypedResourceResolverFactory;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
public class TestTypedResourceResolverFactory implements TypedResourceResolverFactory {

    private final String subServiceName;
    @Setter
    private TypedResourceResolver resolver;

    @Override
    public TypedResourceResolver getSubServiceResolver(String subService) throws RepositoryLoginException {
        if (StringUtils.equals(subServiceName, subService)) {
            return resolver;
        }
        throw new RuntimeException("Failed...");
    }

    @Override
    public <T, E extends Exception> T execute(String subService, RepositoryTask<T, E> task) throws E, RepositoryLoginException {
        TypedResourceResolver resolver = null;
        try {
            resolver = getSubServiceResolver(subService);
            return task.run(resolver);
        } finally {
            if (resolver != null && resolver.isLive()) {
                resolver.close();
            }
        }
    }

}
