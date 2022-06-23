package io.marto.aem.vassets.impl;

import static org.apache.commons.lang3.StringUtils.startsWith;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.rewriter.TransformerFactory;
import org.apache.sling.settings.SlingSettingsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.marto.aem.vassets.AssetVersionService;
import io.marto.aem.vassets.model.Configuration;
import io.marto.aem.vassets.servlet.RequestContext;

@Component
@Service
@Properties({
    @Property(name = "pipeline.type", value = "asset-version-transformer", propertyPrivate = true),
})
public class AssetVersionTransformerFactory implements TransformerFactory {

    @Reference
    private SlingSettingsService settings;

    @Reference
    private AssetVersionService vService;

    @Reference
    private RequestContext requestContext;

    private volatile boolean enabled = false;

    @Activate
    @Modified
    public void init() {
        this.enabled = settings.getRunModes().contains("publish");
    }

    @Override
    public AssetVersionTransformer createTransformer() {
        if (!enabled) {
            return new AssetVersionTransformer(null);
        }
        String reqPath = requestContext.getRequestedResourcePath();
        if (!startsWith(reqPath, "/content")) {
            return new AssetVersionTransformer(null);
        }
        Configuration config = vService.findConfigByContentPath(reqPath);
        if (config == null) {
            LOG.debug("Can't locate config for content path '{}'", reqPath);
            return new AssetVersionTransformer(null);
        }
        return new AssetVersionTransformer(config);
    }

    private static final Logger LOG = LoggerFactory.getLogger(AssetVersionTransformerFactory.class);
}
