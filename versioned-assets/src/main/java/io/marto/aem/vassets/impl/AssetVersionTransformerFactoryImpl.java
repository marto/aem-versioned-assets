package io.marto.aem.vassets.impl;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.startsWith;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

import javax.servlet.http.HttpServletResponse;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.rewriter.Transformer;
import org.apache.sling.rewriter.TransformerFactory;
import org.apache.sling.settings.SlingSettingsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.marto.aem.lib.TypedResourceResolver;
import io.marto.aem.lib.TypedResourceResolverFactory;
import io.marto.aem.vassets.VersionedAssetUpdateException;
import io.marto.aem.vassets.VersionedAssets;
import io.marto.aem.vassets.model.Configuration;
import io.marto.aem.vassets.servlet.RequestContext;


/**
 * Creates a transformer to rewrite asset links.
 */
@Component
@Service
@Properties({
    @Property(name = "pipeline.type", value = "asset-version-transformer", propertyPrivate = true)
})
public class AssetVersionTransformerFactoryImpl implements TransformerFactory, VersionedAssets {

    private static final String SRVC = "versionedAssets";
    private static final String RTYPE_CONFIG = "vassets/components/page/asset-version-configuration";
    private static final AssetVersionTransformerImpl NULL_TRANSFORMER = new AssetVersionTransformerImpl(null);

    private boolean enabled = false;

    @Reference
    private TypedResourceResolverFactory resolverFactory;

    @Reference
    private SlingSettingsService settings;

    @Reference
    private RequestContext requestContext;

    private Configurations configs = new Configurations();

    @Activate
    @Modified
    public void init() {
        this.enabled = settings.getRunModes().contains("publish");
        configs.markForReload(resolverFactory);
   }

    @Override
    public Transformer createTransformer() {
        if (!enabled) {
            return NULL_TRANSFORMER;
        }
        String reqPath = requestContext.getRequestedResourcePath();
        if (!startsWith(reqPath, "/content")) {
            return NULL_TRANSFORMER;
        }
        Configuration config = configs.getByContentPath(reqPath);
        if (config == null) {
            LOG.debug("Can't locate config for content path '{}'", reqPath);
            return NULL_TRANSFORMER;
        }
        return new AssetVersionTransformerImpl(config);
    }

    @Override
    public void updateVersion(String path, long version) throws VersionedAssetUpdateException {
        final Configuration conf = configs.getByConfigPath(path);
        if (conf == null) {
            throw new VersionedAssetUpdateException(format("Failed to find configuration at %s", path), null, HttpServletResponse.SC_NOT_FOUND);
        }
        update(conf, version);
    }

    @Override
    public Configuration findConfigByRewritePath(String basePath, long version) {
        final Configuration conf = configs.getByRewritePath(basePath);
        if (conf != null) {
            List<Long> hist = conf.getHistory();
            if (hist == null) {
                LOG.warn("Configuration {} does not have a version history!", conf);
                return null;
            } else {
                if (hist.contains(version)) {
                    return conf;
                } else {
                    LOG.info("Request is too old for {}", conf);
                    return null;
                }
            }
        }
        return null;
    }

    private Configuration update(Configuration conf, long version) throws VersionedAssetUpdateException {
        return resolverFactory.execute(SRVC, resolver -> updateVersion(resolver, conf, version));
    }

    private Configuration updateVersion(TypedResourceResolver resolver, Configuration conf, long newVersion) throws VersionedAssetUpdateException {
        final Resource resource = resolver.getResource(conf.getPath());
        final ModifiableValueMap props = resource == null ? null : resource.adaptTo(ModifiableValueMap.class);
        if (props == null) {
            throw new VersionedAssetUpdateException(format("Could not locate VersionedAsset configuration at %s", conf), null, HttpServletResponse.SC_NOT_FOUND);
        }
        if (!(conf.getVersion() < newVersion)) {
            throw new VersionedAssetUpdateException(format("Version (%s) must be greater than %s", newVersion, conf.getVersion()), null, HttpServletResponse.SC_CONFLICT);
        }
        conf.addRevision(newVersion);
        props.put("history", conf.getHistory().toArray(new Long[conf.getHistory().size()]));
        props.put("version", conf.getVersion());
        try {
            resolver.commit();
        } catch (PersistenceException e) {
            throw new VersionedAssetUpdateException(format("Failed to update %s", conf), e, HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        }
        return conf;
    }

    private static class Configurations {
        private ConcurrentMap<String, Configuration> configsByRewritePaths = new ConcurrentHashMap<>();

        // We use a SkipListMap to order content paths (shorter ones at the start) as order is important for the operations we sing this for
        private ConcurrentMap<String, Configuration> configsByContentPaths = new ConcurrentSkipListMap<>();

        private volatile boolean reload = true;
        private TypedResourceResolverFactory resolverFactory;

        private void markForReload(TypedResourceResolverFactory resolverFactory) {
            synchronized (this) {
                this.resolverFactory = resolverFactory;
                reload = true;
            }
        }

        private void reloadIfRequired() {
            if (reload) {
                synchronized (this) {
                    configsByRewritePaths.clear();
                    configsByContentPaths.clear();
                    for (Configuration conf : readConfig()) {
                        for (String rewritePaths : conf.getPaths()) {
                            configsByRewritePaths.putIfAbsent(rewritePaths, conf);
                        }
                        configsByContentPaths.putIfAbsent(conf.getContentPath(), conf);
                    }
                    reload = false;
                }
            }
        }

        private List<Configuration> readConfig() {
            return resolverFactory.execute(SRVC,
                    resolver -> resolver.listModelChildren("/etc/vassets", "jcr:content", Configuration.class, RTYPE_CONFIG));
        }

        private Configuration getByConfigPath(String configPath) {
            reloadIfRequired();
            for (Configuration conf : configsByContentPaths.values()) {
                if (conf.getPath().equals(configPath)) {
                    return conf;
                }
            }
            return null;
        }

        private Configuration getByRewritePath(String rewritePath) {
            reloadIfRequired();
            return configsByRewritePaths.get(rewritePath);
        }

        private Configuration getByContentPath(String contentPath) {
            reloadIfRequired();
            for (Map.Entry<String, Configuration> e : configsByContentPaths.entrySet()) {
                if (startsWith(contentPath, e.getKey())) {
                    return e.getValue();
                }
            }
            return null;
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(AssetVersionTransformerFactoryImpl.class);
}

