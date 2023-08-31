package io.marto.aem.vassets.sling;

import lombok.AllArgsConstructor;
import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.api.resource.Resource;

/**
 * Custom implementation of the {@link RequestPathInfo}
 * that always returns {@code null} for the extension.
 *
 * <p>This class delegates most of its method calls to an underlying
 * {@link RequestPathInfo} instance, with the exception of {@link #getExtension()},
 * which is overridden to always return {@code null}.</p>
 */
@AllArgsConstructor
public class NoExtensionRequestPathInfoWrapper implements RequestPathInfo {

    /**
     * The delegate {@link RequestPathInfo} to which most of the method calls are forwarded.
     */
    private final RequestPathInfo delegate;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getResourcePath() {
        return delegate.getResourcePath();
    }

    /**
     * Overrides the {@link RequestPathInfo#getExtension()} method to always return {@code null}.
     *
     * @return always {@code null}.
     */
    @Override
    public String getExtension() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSelectorString() {
        return delegate.getSelectorString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getSelectors() {
        return delegate.getSelectors();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSuffix() {
        return delegate.getSuffix();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Resource getSuffixResource() {
        return delegate.getSuffixResource();
    }
}
