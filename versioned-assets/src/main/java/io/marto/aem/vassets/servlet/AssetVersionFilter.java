package io.marto.aem.vassets.servlet;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.marto.aem.vassets.AssetVersionService;
import io.marto.aem.vassets.model.Configuration;

/**
 * A {@link Filter Servlet Filter} that intercepts any requests with a version fingerprint, checks the fingerprint to see if it's valid
 * and either forwards it to be handled by the system, redirects it to the latest fingerprinted URL or bombs out and returns a 410.
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
    private AssetVersionService assetVersionService;


    private Pattern PATTERN = Pattern.compile("^(/.*)/v-([0-9]+)-v/(.*)$");

    @Override
    protected void doFilterInternal(final SlingHttpServletRequest request, final SlingHttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        final String requestURI = request.getRequestURI();
        final Matcher matcher = PATTERN.matcher(requestURI);
        boolean processed = false;

        if (matcher.find()) {
            final String uriBase = matcher.group(1);
            final Configuration conf = assetVersionService.findConfigByRewritePath(uriBase);
            if (conf != null) {
                final long fingerprint = toLong(matcher.group(2));
                if (conf.getVersion() == fingerprint) {
                    final String newPath = uri(uriBase, null, matcher.group(3), null);
                    LOG.debug("Rewriting request {} to {}", requestURI, newPath);
                    response.setHeader("Cache-Control", "max-age=31104000, public");
                    request.getRequestDispatcher(newPath).include(request, response);
                    processed = true;
                } else {
                    final String newPath = uri(uriBase, conf.getVersion(), matcher.group(3),request.getQueryString());
                    if (conf.inHistory(fingerprint)) {
                        LOG.debug("Redirecting request {} to {}", requestURI, newPath);
                        response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
                        response.setHeader("Location", newPath);
                        processed = true;
                    } else {
                        LOG.debug("Ignoring request {} as it's too old (not in history of possible requests).", requestURI);
                        response.setHeader("Location", newPath);
                        response.sendError(HttpServletResponse.SC_GONE, newPath);
                        processed = true;
                    }
                }
            }
        }
        if (!processed) {
            chain.doFilter(request, response);
        }
    }

    private String uri(String base, Long version, String tail, String queryString) {
        /// TODO USE URIBuilder
        final StringBuilder uri = new StringBuilder(base).append("/");
        if (version != null) {
            uri.append("v-").append(version).append("-v/");
        }
        uri.append(tail);
        if (isNotBlank(queryString)) {
            uri.append("?").append(queryString);
        }
        return uri.toString();
    }

    private static final Logger LOG = LoggerFactory.getLogger(AssetVersionFilter.class);
}
