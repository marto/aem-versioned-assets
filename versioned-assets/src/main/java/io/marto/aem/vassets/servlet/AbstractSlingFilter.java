package io.marto.aem.vassets.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.slf4j.LoggerFactory;

/**
 * Abstract boilerplate Filter class that just types servlet requests/responses to sling conter parts.
 */
public abstract class AbstractSlingFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LoggerFactory.getLogger(getClass()).info("Initialising {}", this.getClass());
    }

    @Override
    public final void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (!(request instanceof SlingHttpServletRequest) || !(response instanceof SlingHttpServletResponse)) {
            throw new ServletException(getClass().getSimpleName() + " just supports Sling HTTP requests");
        }
        doFilterInternal((SlingHttpServletRequest)request, (SlingHttpServletResponse) response, chain);
    }

    @Override
    public void destroy() {
    }

    abstract  protected void doFilterInternal(final SlingHttpServletRequest req, final SlingHttpServletResponse res, FilterChain chain) throws ServletException, IOException;
}
