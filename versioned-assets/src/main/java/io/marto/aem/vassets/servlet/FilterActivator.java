package io.marto.aem.vassets.servlet;

import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.felix.http.api.ExtHttpService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FilterActivator implements BundleActivator {
    private ServiceTracker httpTracker;

    @Override
    public void start(BundleContext context) throws Exception {
        httpTracker = new ServiceTracker(context, ExtHttpService.class.getName(), null) {
            private AssetVersionFilter2 filter;

            @Override
            public void removedService(ServiceReference reference, Object service) {
                // HTTP service is no longer available, unregister our resources...
                try {
                    if (filter != null) {
                        ((ExtHttpService) service).unregisterFilter(filter);
                    }
                } catch (IllegalArgumentException ex) {
                    LOG.warn("Failed to unregister {}", ServiceTracker.class.getSimpleName(), ex);
                } finally {
                    filter = null;
                }
            }

            @Override
            public Object addingService(ServiceReference reference) {
                // HTTP service is available, register our resources...
                ExtHttpService httpService = (ExtHttpService) this.context.getService(reference);
                filter = new AssetVersionFilter2();
                try {
                    Dictionary<String, String> properties = new Hashtable<>();
                    properties.put("service.pid", AssetVersionFilter2.class.getName());
                    httpService.registerFilter(filter, "/.*/v-[0-9]+-v.*", properties, 0, null);
                } catch (Exception ex) {
                    LOG.error("Failed to register {}", filter.getClass().getSimpleName(), ex);
                    filter = null;
                }
                return httpService;
            }
        };

        // start tracking all HTTP services...
        httpTracker.open();
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        httpTracker.close();
        LOG.info("{} stopped",context.getBundle().getSymbolicName());
    }

    private static final Logger LOG = LoggerFactory.getLogger(FilterActivator.class);
}
