package io.marto.aem.vassets;

import io.marto.aem.vassets.model.Configuration;

/**
 * Versioned Assets
 *
 * TODO rename me to ConfigurationFactory
 */
public interface VersionedAssets {


    /**
     * Update the asset version.
     *
     * @param   version - the new version; should be greater than the existing version.
     * @param   path - the path of the versioned asset configuration
     * @throws  VersionedAssetUpdateException if the configration can't be found or it failed to update it
     */
    void updateVersion(String path, long version) throws VersionedAssetUpdateException;


    /**
     * Find configuration based on incoming path request
     *
     * @param basePath
     * @param version
     * @return
     */
    Configuration findConfigByRewritePath(String basePath, long version);
}
