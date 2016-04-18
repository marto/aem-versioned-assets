package io.marto.aem.vassets.servlet;

import java.util.ArrayList;
import java.util.List;

import io.marto.aem.vassets.model.Configuration;

public class TestConfiguration extends Configuration {
    public TestConfiguration(String configPath, String contentPath, List<String> requestPaths, long version, List<Long> versions) {
        super(null, configPath, new ArrayList<>(requestPaths), contentPath, version, 10, new ArrayList<>(versions), null);
    }
}