package io.marto.aem.vassets.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AssetVersionFilter2 implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LOG.info("Initialising {}", this.getClass());
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
        }
    }

    private void doFilter(final HttpServletRequest request, final HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        final String path = request.getRequestURI();

        String newPath = path.replaceAll("/v-[0-9]+-v/", "/");

        if (newPath.length() != path.length()) {
            LOG.debug("Rewriting request {} to {}", path, newPath);
            request.getRequestDispatcher(newPath).forward(request, response);
            return;
        }
        newPath = path.replaceAll("/v-[0-9]+-v\\.", ".");
        if (newPath.length() != path.length()) {
            LOG.debug("Rewriting request {} to {}", path, newPath);
            request.getRequestDispatcher(newPath).forward(request, response);
            return;
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }

    private static final Logger LOG = LoggerFactory.getLogger(AssetVersionFilter2.class);

}
