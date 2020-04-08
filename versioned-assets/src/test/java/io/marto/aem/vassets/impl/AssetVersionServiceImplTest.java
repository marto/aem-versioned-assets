package io.marto.aem.vassets.impl;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.day.cq.replication.ReplicationActionType;
import com.day.cq.replication.ReplicationOptions;
import com.day.cq.replication.Replicator;

import io.marto.aem.lib.RepositoryLoginException;
import io.marto.aem.lib.TypedResourceResolver;
import io.marto.aem.vassets.VersionedAssetUpdateException;
import io.marto.aem.vassets.model.Configuration;
import io.marto.aem.vassets.servlet.TestConfiguration;

@RunWith(MockitoJUnitRunner.class)
public class AssetVersionServiceImplTest {

    @Mock
    private Replicator replicator;

    @Mock
    private TypedResourceResolver resolver;

    @Spy
    private TestTypedResourceResolverFactory resolverFactory = new TestTypedResourceResolverFactory("versionedAssets");

    @InjectMocks
    private AssetVersionServiceImpl service;

    @Mock
    private Resource resource;

    @Mock
    private ModifiableValueMap valueMap;

    @Before
    public void setUp() throws RepositoryLoginException, Exception {
        resolverFactory.setResolver(resolver);
        service.init();
        givenConfigurations(new TestConfiguration("/etc/vassets/site-config", "/content/some-site", asList("/etc/some-site"), 10, asList(5l,6l,7l,8l,9l)));
    }

    private void givenConfigurations(TestConfiguration ... conf) throws Exception {
        when(resolver.listModelChildren(
                eq("/etc/vassets"),
                eq("jcr:content"),
                eq(Configuration.class),
                eq("vassets/components/page/asset-version-configuration")))
            .thenReturn(asList(conf));

        when(resolver.getResource("/etc/vassets/site-config"))
            .thenReturn(resource);

        when(resource.adaptTo(ModifiableValueMap.class))
            .thenReturn(valueMap);
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

        givenConfigurations(new TestConfiguration("/etc/vassets/site-config2", "/content/some-site2", asList("/etc/some-site2","/etc/some-site3"), 10, asList(5l,6l,7l,8l,9l)));

        assertNull(service.findConfigByRewritePath("/etc/some-site"));
        assertNotNull(service.findConfigByRewritePath("/etc/some-site2"));
        assertNotNull(service.findConfigByRewritePath("/etc/some-site3"));
    }

    @Test
    public void testUpdate() throws Exception {
        final Configuration config = service.findConfigByRewritePath("/etc/some-site");
        assertNotNull(config);

        service.updateVersion("/etc/vassets/site-config", 11l);

        assertNotNull(config);
        assertEquals(11L, config.getVersion());
        assertTrue(config.inHistory(10l));
    }

    @Test
    public void testUpdateAutoIncrement() throws Exception {
        final Configuration config = service.findConfigByRewritePath("/etc/some-site");
        assertNotNull(config);

        service.updateVersion("/etc/vassets/site-config", -1l);

        assertNotNull(config);
        assertEquals(11L, config.getVersion());
        assertTrue(config.inHistory(10l));
    }

    @Test
    public void testUpdateWithReplication() throws Exception {
        final Configuration config = service.findConfigByRewritePath("/etc/some-site");
        assertNotNull(config);

        service.updateVersionAndActivate("/etc/vassets/site-config", 11l);

        assertNotNull(config);
        assertEquals(11L, config.getVersion());
        assertTrue(config.inHistory(10l));

        verify(replicator).replicate(any(), eq(ReplicationActionType.ACTIVATE), eq("/etc/vassets/site-config"), any(ReplicationOptions.class));
    }

    @Test(expected =  VersionedAssetUpdateException.class)
    public void testUpdateThrowsException() throws Exception {
        final Configuration config = service.findConfigByRewritePath("/etc/some-site");
        assertNotNull(config);

        service.updateVersion("/etc/vassets/site-config", 10l);

    }

}
