package io.marto.aem.vassets.servlet;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.trimToNull;
import static org.apache.commons.lang3.math.NumberUtils.toLong;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.marto.aem.vassets.VersionedAssetUpdateException;
import io.marto.aem.vassets.VersionedAssets;

/**
 * Updates the version of a versioned asset directory.
 *
 * The version can only contain numeric characters.
 * The path must denote a valid configuration (i.e. /etc/
 *
 * Note: Ensure the servlet is secured by using the dispatcher configuration.
 */
@SlingServlet(paths = "/bin/private/vassets/assetversion", selectors = "update", extensions = "cfg")
public class AssetVersionUpdateServlet extends SlingAllMethodsServlet {

    @Reference
    private transient VersionedAssets versionedAssets;

    /**
     * Performs the update for the version.
     */
    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
        final String path = trimToNull(request.getParameter("path"));
        if (isBlank(path)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "'path' paramater is missing");
            return;
        }
        final long version = toLong(request.getParameter("version"), -1);
        if (version < 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "'version' paramater is missing or not a number");
            return;
        }
        try {
            versionedAssets.updateVersion(path, version);
            response.setContentType("text/plain");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().append("OK");
        } catch (VersionedAssetUpdateException e) {
            LOG.warn(format("Failed to update path(%s) with version %s: %s", path, version, e), e);
            response.setContentType("text/plain");
            response.sendError(e.getResponse(), e.getMessage());
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(AssetVersionUpdateServlet.class);
    private static final long serialVersionUID = 1L;
}
