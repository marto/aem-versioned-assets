package io.marto.aem.vassets.servlet;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.marto.aem.vassets.AssetVersionService;

@RunWith(MockitoJUnitRunner.class)
public class AssetVersionFilterTest {
    @Mock
    private SlingHttpServletRequest request;

    @Mock
    private SlingHttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private AssetVersionService assetVersionService;

    @InjectMocks
    private AssetVersionFilter filter;

    @Mock
    private RequestDispatcher dispatcher;

    private TestConfiguration config;

    @Before
    public void setUp() {
        config = new TestConfiguration("/etc/vassets/site-config", "/content/some-site", asList("/etc/some-site/"), 10, asList(5l,6l,7l,8l,9l));
        when(request.getRequestDispatcher(eq("/etc/some-site/client-libs/blar.js"))).thenReturn(dispatcher);
    }

    @Test
    public void testInternalRedirect() throws IOException, ServletException {
        givenRequest("/etc/some-site/v-10-v/client-libs/blar.js");
        when(assetVersionService.findConfigByRewritePath("/etc/some-site")).thenReturn(config);

        filter.doFilter(request, response, filterChain);

        thenPerformsIntrnalRewriteTo("/etc/some-site/client-libs/blar.js");
    }

    @Test
    public void testRedirectsToLatestVersionWhenInHistory() throws IOException, ServletException {
        givenRequest("/etc/some-site/v-9-v/client-libs/blar.js");
        when(assetVersionService.findConfigByRewritePath("/etc/some-site")).thenReturn(config);

        filter.doFilter(request, response, filterChain);

        verify(dispatcher, times(0)).include(request, response);

        thenRedirectsTo("/etc/some-site/v-10-v/client-libs/blar.js");
    }

    @Test
    public void testResourceIsGoneWhenNotHistory() throws IOException, ServletException {
        givenRequest("/etc/some-site/v-1-v/client-libs/blar.js");
        when(assetVersionService.findConfigByRewritePath("/etc/some-site")).thenReturn(config);

        filter.doFilter(request, response, filterChain);

        thenResourceIsGone("/etc/some-site/v-10-v/client-libs/blar.js");
    }

    private void thenPerformsIntrnalRewriteTo(String location) throws ServletException, IOException {
        verify(request, times(1)).getRequestDispatcher(eq(location));
        verify(dispatcher, times(1)).include(request, response);
        verify(response, times(1)).setHeader(eq("Cache-Control"), eq("max-age=31104000, public"));
        verify(filterChain, times(0)).doFilter(request, response);
    }

    private void thenResourceIsGone(String location) throws IOException, ServletException {
        verify(response, times(1)).sendError(410, location);
        verify(dispatcher, times(0)).include(request, response);
        verify(filterChain, times(0)).doFilter(request, response);
    }

    private void thenRedirectsTo(String location) throws IOException, ServletException {
        verify(response, times(1)).setStatus(301);
        verify(response, times(1)).setHeader(eq("Location"), eq(location));
        verify(dispatcher, times(0)).include(request, response);
        verify(filterChain, times(0)).doFilter(request, response);
    }

    private void givenRequest(String path) {
        when(request.getRequestURI()).thenReturn(path);
    }
}
