package io.marto.aem.vassets.servlet;

public interface RequestContext {

    /**
     * @return the requested resource path
     */
    String getRequestedResourcePath();

    String getRequestedURI();

}
