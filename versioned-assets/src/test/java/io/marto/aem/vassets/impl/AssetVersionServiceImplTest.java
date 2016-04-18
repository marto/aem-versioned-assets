package io.marto.aem.vassets.impl;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.day.cq.replication.Replicator;

import io.marto.aem.lib.RepositoryLoginException;
import io.marto.aem.lib.RepositoryTask;
import io.marto.aem.lib.TypedResourceResolverFactory;
import io.marto.aem.vassets.model.Configuration;
import io.marto.aem.vassets.servlet.TestConfiguration;

@RunWith(MockitoJUnitRunner.class)
public class AssetVersionServiceImplTest {

    @Mock
    private Replicator replicator;

    @Mock
    private TypedResourceResolverFactory resolverFactory;

    @InjectMocks
    private AssetVersionServiceImpl service;

    @Before
    public void setUp() throws RepositoryLoginException, Exception {
        service.init();
        givenConfigurations(new TestConfiguration("/etc/vassets/site-config", "/content/some-site", asList("/etc/some-site"), 10, asList(5l,6l,7l,8l,9l)));
    }

    @SuppressWarnings("unchecked")
    private void givenConfigurations(TestConfiguration ... conf) throws Exception {
        when(resolverFactory.execute(eq("versionedAssets"), any(RepositoryTask.class))).thenAnswer(new Answer<List<Configuration>>() {
            @Override
            public List<Configuration> answer(InvocationOnMock invocation) throws Throwable {
                return asList(conf);
            }
        });
    }

    @Test
    public void testFindConfigByRewritePath() {
        assertNotNull(service.findConfigByRewritePath("/etc/some-site"));
    }

    @Test
    public void testFindConfigByContentPath() {
        assertNotNull(service.findConfigByContentPath("/content/some-site"));
    }

    @Test
    public void testReload() throws Exception {
        assertNotNull(service.findConfigByRewritePath("/etc/some-site"));

        service.handleEvent(null);

        givenConfigurations(new TestConfiguration("/etc/vassets/site-config2", "/content/some-site2", asList("/etc/some-site2"), 10, asList(5l,6l,7l,8l,9l)));

        assertNull(service.findConfigByRewritePath("/etc/some-site"));
        assertNotNull(service.findConfigByRewritePath("/etc/some-site2"));

    }

}
