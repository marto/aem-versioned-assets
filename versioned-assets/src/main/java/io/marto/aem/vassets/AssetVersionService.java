package io.marto.aem.vassets;

import io.marto.aem.vassets.model.Configuration;

/**
 * Versioned Assets
 *
 */
public interface AssetVersionService {

    /**
     * Update the asset version.
     *
     * @param   version - the new version; should be greater than the existing version.
     * @param   path - the path of the versioned asset configuration
     * @throws  VersionedAssetUpdateException if the configration can't be found or it failed to update it
     */
    void updateVersion(String path, long version) throws VersionedAssetUpdateException;

    /**
     * Same as {@link #updateVersion(String, long)} but also activates (replicates) the configuration to the publishers.
     *
     * @param   version - the new version; should be greater than the existing version.
     * @param   path - the path of the versioned asset configuration
     * @throws  VersionedAssetUpdateException if the configration can't be found or it failed to update it
     */
    void updateVersionAndActivate(String path, long version) throws VersionedAssetUpdateException;

    /**
     * Find configuration based on a rewritepath
     *
     * @param basePath the base of the versioned path, before the "/v-{number}-v/..." tag.
     *
     * @return the configuration if one found, null if none found
     */
    Configuration findConfigByRewritePath(String basePath);

    /**
     * Find configuration by the "/content" path that it will be used to rewrite versioned assets.
     *
     * @param path the path starting with "/content" for which the configuration will be transforming html content
     *
     * @return the configuration if one found, null if none found
     */
    Configuration findConfigByContentPath(String path);
}
