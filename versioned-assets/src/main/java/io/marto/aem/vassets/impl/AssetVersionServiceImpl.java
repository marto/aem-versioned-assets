package io.marto.aem.vassets.impl;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.startsWith;
import static org.osgi.service.event.EventConstants.EVENT_FILTER;
import static org.osgi.service.event.EventConstants.EVENT_TOPIC;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

import javax.jcr.Session;
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
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.replication.ReplicationActionType;
import com.day.cq.replication.ReplicationException;
import com.day.cq.replication.ReplicationOptions;
import com.day.cq.replication.Replicator;

import io.marto.aem.lib.TypedResourceResolver;
import io.marto.aem.lib.TypedResourceResolverFactory;
import io.marto.aem.vassets.AssetVersionService;
import io.marto.aem.vassets.VersionedAssetUpdateException;
import io.marto.aem.vassets.model.Configuration;

/**
 * Creates a transformer to rewrite asset links.
 */
@Component
@Service
@Properties({
    @Property(name = EVENT_TOPIC, value = { "org/apache/sling/api/resource/Resource/*" }),
    @Property(name = EVENT_FILTER, value = "(&(path=/etc/vassets/*/jcr:content)(resourceType=vassets/components/page/asset-version-configuration))")
})
public class AssetVersionServiceImpl implements AssetVersionService, EventHandler {

    private static final String SRVC = "versionedAssets";
    private static final String RTYPE_CONFIG = "vassets/components/page/asset-version-configuration";

    @Reference
    private Replicator replicator;

    @Reference
    private TypedResourceResolverFactory resolverFactory;

    private Configurations configs = new Configurations();

    private static final ReplicationOptions REP_OPTIONS = new ReplicationOptions();

    static {
        REP_OPTIONS.setSynchronous(true);
        REP_OPTIONS.setSuppressStatusUpdate(false);
    }

    @Activate
    @Modified
    public void init() {
        configs.markForReload(resolverFactory);
   }

    @Override
    public void updateVersion(String path, long version) throws VersionedAssetUpdateException {
        updateVersion(path, version, false);
    }

    @Override
    public void updateVersionAndActivate(String path, long version) throws VersionedAssetUpdateException {
        updateVersion(path, version, true);
    }

    private void updateVersion(String path, long version, boolean activate) throws VersionedAssetUpdateException {
        final Configuration conf = configs.getByConfigPath(path);
        if (conf == null) {
            throw new VersionedAssetUpdateException(format("Failed to find configuration at %s", path), null, HttpServletResponse.SC_NOT_FOUND);
        }
        resolverFactory.execute(SRVC, resolver -> updateVersion(resolver, conf, version, activate));
    }

    @Override
    public Configuration findConfigByRewritePath(String basePath) {
        return configs.getByRewritePath(basePath);
    }

    @Override
    public Configuration findConfigByContentPath(String path) {
        return configs.getByContentPath(path);
    }

    private Void updateVersion(TypedResourceResolver resolver, Configuration conf, long newVersion, boolean activate) throws VersionedAssetUpdateException {
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
        if (activate) {
            replicate(conf.getPath(), resolver);
        }
        try {
            resolver.commit();
        } catch (PersistenceException e) {
            throw new VersionedAssetUpdateException(format("Failed to update %s", conf), e, HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        }
        return null;
    }

    private void replicate(final String path, final ResourceResolver resolver) throws VersionedAssetUpdateException {
        try {
            LOG.debug("Activating {}", path);
            replicator.replicate(resolver.adaptTo(Session.class), ReplicationActionType.ACTIVATE, path, REP_OPTIONS);
        } catch (ReplicationException e) {
            throw new VersionedAssetUpdateException(format("Failed to activate configuration at %s", path), e, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void handleEvent(Event event) {
        configs.markForReload(resolverFactory);
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

    private static final Logger LOG = LoggerFactory.getLogger(AssetVersionServiceImpl.class);
}

