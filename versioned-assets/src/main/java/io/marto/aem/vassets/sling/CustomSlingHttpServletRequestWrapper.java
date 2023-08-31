package io.marto.aem.vassets.sling;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.api.wrappers.SlingHttpServletRequestWrapper;

/**
 * Custom implementation of the {@link SlingHttpServletRequestWrapper}
 * which allows for overriding the default {@link RequestPathInfo}.
 *
 * <p>This class takes in a custom {@link RequestPathInfo} during instantiation
 * and uses it to override the wrapped request's path info.</p>
 */
public class CustomSlingHttpServletRequestWrapper extends SlingHttpServletRequestWrapper {

    /**
     * Custom {@link RequestPathInfo} to be returned by this wrapper.
     */
    private final RequestPathInfo requestPathInfo;

    /**
     * Constructs a new {@link CustomSlingHttpServletRequestWrapper} with the given wrapped request
     * and a custom {@link RequestPathInfo}.
     *
     * @param wrappedRequest     the original {@link SlingHttpServletRequest} to wrap.
     * @param newRequestPathInfo the custom {@link RequestPathInfo} to override the wrapped request's path info.
     */
    public CustomSlingHttpServletRequestWrapper(SlingHttpServletRequest wrappedRequest, RequestPathInfo newRequestPathInfo) {
        super(wrappedRequest);
        this.requestPathInfo = newRequestPathInfo;
    }

    /**
     * Returns the custom {@link RequestPathInfo} set during the instantiation of this wrapper.
     *
     * @return custom {@link RequestPathInfo}.
     */
    @Override
    public RequestPathInfo getRequestPathInfo() {
        return requestPathInfo;
    }
}
