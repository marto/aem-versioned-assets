package io.marto.aem.vassets.impl;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import org.apache.sling.rewriter.Transformer;
import org.apache.sling.settings.SlingSettingsService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.marto.aem.vassets.AssetVersionService;
import io.marto.aem.vassets.model.Configuration;
import io.marto.aem.vassets.servlet.RequestContext;

@RunWith(MockitoJUnitRunner.class)
public class AssetVersionTransformerFactoryTest {

    @Mock
    private SlingSettingsService settings;

    @Mock
    private AssetVersionService vService;

    @Mock
    private RequestContext requestContext;

    @Mock
    private Configuration configuration;

    @InjectMocks
    private AssetVersionTransformerFactory transformerFactory;

    @Test
    public void createTransformer() {
        when(requestContext.getRequestedResourcePath()).thenReturn("/content/some-site");
        when(vService.findConfigByContentPath(eq("/content/some-site"))).thenReturn(configuration);

        Transformer transformer = transformerFactory.createTransformer();

        assertNotNull(transformer);
    }

    @Test
    public void createNullTransformer() {
        when(requestContext.getRequestedResourcePath()).thenReturn("/content/some-site");
        when(vService.findConfigByContentPath(eq("/content/some-other-site"))).thenReturn(configuration);

        Transformer transformer = transformerFactory.createTransformer();

        assertNotNull(transformer);
    }
}
