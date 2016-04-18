package io.marto.aem.vassets.impl;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import io.marto.aem.vassets.model.Configuration;

@RunWith(MockitoJUnitRunner.class)
public class AssetVersionTransformerTest {

    @Mock
    private ContentHandler contentHandler;

    @Mock
    private Configuration configuration;

    private AssetVersionTransformer assetVersionTransformer;

    @Before
    public void setUp() {
        assetVersionTransformer = new AssetVersionTransformer(configuration);
        assetVersionTransformer.setContentHandler(contentHandler);
    }

    @Test
    public void startElement() throws Exception {
        when(configuration.getPaths()).thenReturn(asList("/etc"));
        when(configuration.getVersion()).thenReturn(123456789l);

        AttributesImpl attributesImpl = new AttributesImpl();
        attributesImpl.addAttribute(null, "src", "src", null, "/etc/designs/asset");
        attributesImpl.addAttribute(null, "href", "href", null, "/etc/designs/asset");
        attributesImpl.addAttribute(null, "class", "class", null, "test");

        assetVersionTransformer.startElement(null, "link", "link", attributesImpl);

        ArgumentCaptor<Attributes> argument = ArgumentCaptor.forClass(Attributes.class);
        verify(contentHandler).startElement((String) eq(null), eq("link"), eq("link"), argument.capture());

        assertEquals("/etc/v-123456789-v/designs/asset", argument.getValue().getValue(0));
        assertEquals("/etc/v-123456789-v/designs/asset", argument.getValue().getValue(1));
        assertEquals("test", argument.getValue().getValue(2));
    }

    @Test
    public void startElementWithHost() throws Exception {
        when(configuration.getPaths()).thenReturn(asList("/etc"));
        when(configuration.getVersion()).thenReturn(123456789l);
        when(configuration.getHost()).thenReturn("host");

        AttributesImpl attributesImpl = new AttributesImpl();
        attributesImpl.addAttribute(null, "src", "src", null, "/etc/designs/asset");
        attributesImpl.addAttribute(null, "href", "href", null, "/etc/designs/asset");
        attributesImpl.addAttribute(null, "class", "class", null, "test");

        assetVersionTransformer.startElement(null, "link", "link", attributesImpl);

        ArgumentCaptor<Attributes> argument = ArgumentCaptor.forClass(Attributes.class);
        verify(contentHandler).startElement((String) eq(null), eq("link"), eq("link"), argument.capture());

        assertEquals("//host/etc/v-123456789-v/designs/asset", argument.getValue().getValue(0));
        assertEquals("//host/etc/v-123456789-v/designs/asset", argument.getValue().getValue(1));
        assertEquals("test", argument.getValue().getValue(2));
    }

    @Test
    public void startElementEmptyAttributes() throws Exception {
        AttributesImpl attributesImpl = new AttributesImpl();

        assetVersionTransformer.startElement(null, "link", "link", attributesImpl);

        ArgumentCaptor<Attributes> argument = ArgumentCaptor.forClass(Attributes.class);
        verify(contentHandler).startElement((String) eq(null), eq("link"), eq("link"), argument.capture());

        assertEquals(null, argument.getValue().getValue(0));
    }

    @Test
    public void startElementDifferentElement() throws Exception {
        AttributesImpl attributesImpl = new AttributesImpl();

        assetVersionTransformer.startElement(null, "div", "div", attributesImpl);


        ArgumentCaptor<Attributes> argument = ArgumentCaptor.forClass(Attributes.class);
        verify(contentHandler).startElement((String) eq(null), eq("div"), eq("div"), argument.capture());

        assertEquals(null, argument.getValue().getValue(0));
    }

    @Test
    public void testNullTransformerDoesNotPerformTransformations() throws SAXException {
        assetVersionTransformer = new AssetVersionTransformer(null);
        assetVersionTransformer.setContentHandler(contentHandler);

        AttributesImpl attributesImpl = new AttributesImpl();
        attributesImpl.addAttribute(null, "src", "src", null, "/etc/designs/asset");

        assetVersionTransformer.startElement(null, "link", "link", attributesImpl);

        ArgumentCaptor<Attributes> argument = ArgumentCaptor.forClass(Attributes.class);
        verify(contentHandler).startElement((String) eq(null), eq("link"), eq("link"), argument.capture());

        assertEquals(null, argument.getValue().getValue(1));
        assertEquals("/etc/designs/asset", argument.getValue().getValue(0));
    }

    @Test
    public void testEmptyTransformerDoesNotPerformTransformations() throws SAXException {
        when(configuration.getPaths()).thenReturn(asList());

        AttributesImpl attributesImpl = new AttributesImpl();
        attributesImpl.addAttribute(null, "src", "src", null, "/etc/designs/asset");

        assetVersionTransformer.startElement(null, "link", "link", attributesImpl);

        ArgumentCaptor<Attributes> argument = ArgumentCaptor.forClass(Attributes.class);
        verify(contentHandler).startElement((String) eq(null), eq("link"), eq("link"), argument.capture());

        assertEquals(null, argument.getValue().getValue(1));
        assertEquals("/etc/designs/asset", argument.getValue().getValue(0));
    }
}
