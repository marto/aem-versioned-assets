package io.marto.aem.vassets.impl;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

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
    public void createTransformerOnlyOnPublish() {
        giveRunMode("author");
        givenRequestWithPath("/content/some-site");
        givenConfigurationWithContentPath("/content/some-site");

        // when
        AssetVersionTransformer transformer = transformerFactory.createTransformer();

        // then
        thenNullTransformerIsCreated(transformer);
    }

    @Test
    public void createTransformer() {
        giveRunMode("publish");
        givenRequestWithPath("/content/some-site");
        givenConfigurationWithContentPath("/content/some-site");

        // when
        AssetVersionTransformer transformer = transformerFactory.createTransformer();

        // then non-null transformer is created
        assertNotNull(transformer);
        assertNotNull(transformer.getConf());
    }

    @Test
    public void testNullTransformerIsCreatedWhenContentPathDoesNotMatch() {
        giveRunMode("publish");
        givenRequestWithPath("/content/some-site");
        givenConfigurationWithContentPath("/content/some-other-site");

        // when
        AssetVersionTransformer transformer = transformerFactory.createTransformer();

        // then
        thenNullTransformerIsCreated(transformer);
    }


    @Test
    public void testNullTransformerIsCreatedWhenOnNonContentPath() {
        giveRunMode("publish");
        givenRequestWithPath("/etc/something");
        givenConfigurationWithContentPath("/content/some-other-site");

        // when
        AssetVersionTransformer transformer = transformerFactory.createTransformer();

        // then
        thenNullTransformerIsCreated(transformer);
    }

    private void thenNullTransformerIsCreated(AssetVersionTransformer transformer) {
        assertNotNull(transformer);
        assertNull(transformer.getConf());
    }

    private void givenConfigurationWithContentPath(String path) {
        when(vService.findConfigByContentPath(eq(path))).thenReturn(configuration);
    }

    private void givenRequestWithPath(String path) {
        when(requestContext.getRequestedResourcePath()).thenReturn(path);
    }

    private void giveRunMode(String mode) {
        when(settings.getRunModes()).thenReturn(asSet(mode));
        transformerFactory.init();
    }

    private <S> Set<S> asSet(@SuppressWarnings("unchecked") S ... values) {
        return new HashSet<S>(asList(values));
    }
}
