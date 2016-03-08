package io.marto.aem.vassets.impl;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.removeEnd;
import static org.apache.commons.lang3.StringUtils.replace;

import java.net.URISyntaxException;

import org.apache.http.client.utils.URIBuilder;
import org.apache.sling.rewriter.DefaultTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import io.marto.aem.vassets.model.Configuration;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AssetVersionTransformerImpl extends DefaultTransformer {

    private static final Logger LOGGER = LoggerFactory.getLogger(AssetVersionTransformerImpl.class);

    private final Configuration conf;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (conf != null && handlesElement(localName)) {
            AttributesImpl attrs = new AttributesImpl(attributes);
            for (int i = 0; i < attrs.getLength(); i++) {
                String name = attrs.getLocalName(i);
                if (name.equalsIgnoreCase("src") || name.equalsIgnoreCase("href")) {
                    attrs.setValue(i, process(conf, attrs.getValue(i)));
                }
            }
            super.startElement(uri, localName, qName, attrs);
        } else {
            super.startElement(uri, localName, qName, attributes);
        }
    }

    /**
     * Determines if the html element can be handled/processed.
     *
     * @param name
     *      Element Name.
     * @return
     *      True if the element can be handled, otherwise false.
     */
    private boolean handlesElement(String name) {
        return conf != null && equalsIgnoreCase(name, "script") || equalsIgnoreCase(name, "link");
    }

    /**
     * Process the replacement of the asset version.
     * @param conf
     *
     * @param value
     *      The value to process.
     * @return
     *      The processed result.
     */
    private String process(Configuration conf, String value) {
        if (!conf.getPaths().isEmpty()) {
            try {
                URIBuilder builder = new URIBuilder(value);
                for (String path : conf.getPaths()) {
                    builder.setPath(replace(builder.getPath(), removeEnd(path, "/"), format("%s/v-%s-v", path, conf.getVersion())));
                }

                if (isNotEmpty(conf.getHost())) {
                    builder.setHost(conf.getHost());
                }

                value = builder.toString();
            } catch (URISyntaxException e) {
                LOGGER.warn("Unable to convert URI", e);
            }
        }
        return value;
    }

}
