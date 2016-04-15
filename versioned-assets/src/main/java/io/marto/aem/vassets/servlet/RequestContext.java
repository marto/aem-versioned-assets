package io.marto.aem.vassets.servlet;

/**
 * Provides a request context as a service for when the HttpServletRequest is not available. The context is stored as a
 * thread local, thus new spawned threads will lose the context.
 */
public interface RequestContext {

    /**
     * @return the requested resource path
     */
    String getRequestedResourcePath();

    /**
     * @return the requested URI
     */
    String getRequestedURI();
}
