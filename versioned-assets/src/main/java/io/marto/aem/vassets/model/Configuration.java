package io.marto.aem.vassets.model;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Optional;
import org.apache.sling.models.annotations.injectorspecific.Self;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Models a single Versioned Asset configuration
 */
@Model(adaptables = Resource.class)
@ToString(of = { "path", "version" })
@Getter
@EqualsAndHashCode(of = "path")
public class Configuration {

    @Self
    @Getter(AccessLevel.PRIVATE)
    private Resource resource;

    private String path;

    /**
     * List of versioned asset paths to rewrite
     */
    @Inject
    private List<String> paths;

    /**
     * The content path that should be processed ( eg: /content/somesite )
     */
    @Inject
    private String contentPath;

    @Inject
    private long version = 0L;

    @Inject
    private int maxHistory;

    @Inject
    @Optional
    private List<Long> history;

    /**
     * The external host that should serve the assets - usually a cookieless CDN backed domain. You should most likely use "//somehost.com".
     */
    @Inject
    @Optional
    private String host;

    @PostConstruct
    protected void init() {
        path = resource.getPath();
        // ensure configuration is thread safe for updating paths and history
        paths = new CopyOnWriteArrayList<String>(paths == null ? Collections.emptyList() : paths);
        history = new CopyOnWriteArrayList<Long>(history == null ? Collections.emptyList() : history);
    }

    /**
     * Set a new version and update the history. Do not call this method directly but instead use
     * {@link io.marto.aem.vassets.VersionedAssets#updateVersion(String, long)}
     *
     * @param newVersion the new version
     */
    public void addRevision(long newVersion) {
        history.add(0, newVersion);
        while (history.size() > getMaxHistory()) {
            history.remove(history.size() - 1);
        }
        this.version = newVersion;
    }

    /**
     * @param revision the version to check for in the history
     *
     * @return true if <code>revision</code> is in history, false otherwise.
     */
    public boolean inHistory(long revision) {
        return history.contains(revision);
    }

}
