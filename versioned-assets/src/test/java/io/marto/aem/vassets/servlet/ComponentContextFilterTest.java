package io.marto.aem.vassets.servlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

@RunWith(MockitoJUnitRunner.class)
public class ComponentContextFilterTest {

    @Mock
    private SlingHttpServletRequest request;

    @Mock
    private SlingHttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private Resource resource;

    private ComponentContextFilter filter = new ComponentContextFilter();

    @Before
    public void setUp() {
        when(request.getResource()).thenReturn(resource);
    }

    public void givenReq(String path, String uri) {
        when(resource.getPath()).thenReturn(path);
        when(request.getRequestURI()).thenReturn(uri);
    }

    @Test
    public void testFirstInvocation() throws IOException, ServletException {
        givenReq("/content/blar", "/content/blar.html");

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                assertEquals("/content/blar", filter.getRequestedResourcePath());
                assertEquals("/content/blar.html", filter.getRequestedURI());
                return null;
            }
        }).when(filterChain).doFilter(request,response);

        filter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(filter.getRequestedResourcePath());
        assertNull(filter.getRequestedURI());
    }


    @Test
    public void testMultipleInvocationsAreIgnored() throws IOException, ServletException {
        givenReq("/content/blar", "/content/blar.html");
        when(request.getAttribute(eq("io.marto.aem.vassets.servlet.ComponentContextFilter.ONCE"))).thenReturn(true);

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                assertNull(filter.getRequestedResourcePath());
                assertNull(filter.getRequestedURI());
                return null;
            }
        }).when(filterChain).doFilter(request,response);

        filter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }
}
