package io.marto.aem.vassets.servlet;

import static java.lang.String.format;
import static org.apache.commons.lang3.math.NumberUtils.toLong;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingFilter;
import org.apache.felix.scr.annotations.sling.SlingFilterScope;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestDispatcherOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.marto.aem.vassets.VersionedAssets;
import io.marto.aem.vassets.model.Configuration;

/**
 * A {@link Filter Servlet Filter} that intercepts any requests with a version fingerprint, checks the fingerprint to see if it's valid
 * and either forwards it to be handled by the system, redirects it to the latest fingerprinted URL or bombs out and returns a 404.
 */
@SlingFilter(
    scope = { SlingFilterScope.REQUEST },
    order = Integer.MAX_VALUE,
    generateComponent = true,
    generateService = true,
    label = "Versioned Asset Filter",
    description = "Intercepts requests and validates URLfingerprinted requests and then forwards the request to the undelrying system")
public class AssetVersionFilter extends AbstractSlingFilter {

    @Reference
    private VersionedAssets versionedAssets;


    private Pattern PATTERN = Pattern.compile("^(/.*)/v-([0-9]+)-v/(.*)$");

    @Override
    protected void doFilterInternal(final SlingHttpServletRequest request, final SlingHttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        final String requestURI = request.getRequestURI();
        final Matcher matcher = PATTERN.matcher(requestURI);
        boolean processed = false;

        if (matcher.find()) {
            final long fingerprint = toLong(matcher.group(2));
            final Configuration conf = versionedAssets.findConfigByRewritePath(matcher.group(1), fingerprint);
            if (conf != null) {
                if (conf.getVersion() == fingerprint) {
                    final String newPath = format("%s/%s", matcher.group(1), matcher.group(3));
                    LOG.debug("Rewriting request {} to {}", requestURI, newPath);
                    response.setHeader("Cache-Control", "max-age=31104000, public");
                    request.getRequestDispatcher(newPath, new RequestDispatcherOptions()).include(request, response);
                    processed = true;
                } else if (conf.inHistory(fingerprint)) {
                    final String newPath = format("%s/v-%d-v/%s", matcher.group(1), conf.getVersion(), matcher.group(3));
                    LOG.debug("Redirecting request {} to {}", requestURI, newPath);
                    response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
                    response.setHeader("Location", newPath);
                    processed = true;
                }
            }
        }
        if (!processed) {
            chain.doFilter(request, response);
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(AssetVersionFilter.class);
}
