package io.marto.aem.vassets.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;

import org.apache.felix.scr.annotations.sling.SlingFilter;
import org.apache.felix.scr.annotations.sling.SlingFilterScope;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestDispatcherOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link Filter Servlet Filter} that intercepts any requests with a version fingerprint, checks them and forwards them to be handled by the system.
 */
@SlingFilter(
    scope = { SlingFilterScope.REQUEST },
    order = Integer.MAX_VALUE,
    generateComponent = true,
    generateService = true,
    label = "Versioned Asset Filter",
    description = "Intercepts requests and validates URLfingerprinted requests and then forwards the request to the undelrying system")
public class AssetVersionFilter extends AbstractSlingFilter {

    @Override
    protected void doFilterInternal(final SlingHttpServletRequest request, final SlingHttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        final String path = request.getRequestURI();

        // TODO MP - No version checks are done here!

        final String newPath = path.replaceAll("/v-[0-9]+-v/", "/");

        if (newPath.length() != path.length()) {
            LOG.debug("Rewriting request {} to {}", path, newPath);
            RequestDispatcherOptions ops = new RequestDispatcherOptions();
            request.getRequestDispatcher(newPath, ops).forward(request, response);
        } else {
            chain.doFilter(request, response);
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(AssetVersionFilter.class);
}
