package io.marto.aem.vassets.servlet;

import static java.lang.String.format;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;

import org.apache.felix.scr.annotations.Service;
import org.apache.felix.scr.annotations.sling.SlingFilter;
import org.apache.felix.scr.annotations.sling.SlingFilterScope;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * A {@link Filter Servlet Filter} that intercepts a component request to save the context of a request.
 */
@Service({RequestContext.class, Filter.class})
@SlingFilter(
    scope = { SlingFilterScope.COMPONENT },
    order = Integer.MAX_VALUE,
    generateComponent = true,
    generateService = false,
    label = "Request Context Filter",
    description = "Intercepts requests and validates URLfingerprinted requests and then forwards the request to the undelrying system")
public class ComponentContextFilter extends AbstractSlingFilter implements RequestContext {

    private static final String ALREADY_EXECUTED_ATTRIB = format("%s.ONCE", ComponentContextFilter.class.getName());

    private static ThreadLocal<FullRequestContext> context = new ThreadLocal<>();

    @Override
    protected void doFilterInternal(final SlingHttpServletRequest req, final SlingHttpServletResponse res, FilterChain chain) throws ServletException, IOException {
        if (req.getAttribute(ALREADY_EXECUTED_ATTRIB) != null) {
            // Proceed without invoking this filter...
            chain.doFilter(req, res);
        } else {
            try {
                req.setAttribute(ALREADY_EXECUTED_ATTRIB, Boolean.TRUE);
                Resource resource = req.getResource();
                if (resource != null) {
                    context.set(new FullRequestContext(resource.getPath(), req.getRequestURI()));
                }
                chain.doFilter(req, res);
            } finally {
                // Remove the "already filtered" request attribute for this request.
                req.removeAttribute(ALREADY_EXECUTED_ATTRIB);
                context.remove();
            }
        }
    }

    @Override
    public String getRequestedResourcePath() {
        FullRequestContext r = context.get();
        return r == null ? null : r.getResourcePath();
    }

    @Override
    public String getRequestedURI() {
        FullRequestContext r = context.get();
        return r == null ? null : r.getRequestURI();
    }

    @RequiredArgsConstructor
    @Getter
    private static class FullRequestContext {
        private final String resourcePath;
        private final String requestURI;
    }
}