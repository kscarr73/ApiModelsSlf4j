package com.progbits.api.slf4j;

import org.slf4j.ILoggerFactory;
import org.slf4j.IMarkerFactory;
import org.slf4j.helpers.BasicMDCAdapter;
import org.slf4j.helpers.BasicMarkerFactory;
import org.slf4j.spi.MDCAdapter;
import org.slf4j.spi.SLF4JServiceProvider;

/**
 *
 * @author scarr
 */
public class ApiModelsProvider implements SLF4JServiceProvider {

    public static String REQUESTED_API_VERSION = "2.0.13";
    
    private ApiModelsSlf4jConfig config;
    private ILoggerFactory loggerFactory;
    private IMarkerFactory markerFactory;
    private MDCAdapter mdcAdapter;
    
    @Override
    public void initialize() {
        loggerFactory = new ApiModelsFactory();
        markerFactory = new BasicMarkerFactory();
        mdcAdapter = new BasicMDCAdapter();
        config = ApiModelsSlf4jConfig.getInstance();
    }
    
    @Override
    public ILoggerFactory getLoggerFactory() {
        return loggerFactory;
    }

    @Override
    public IMarkerFactory getMarkerFactory() {
        return markerFactory;
    }

    @Override
    public MDCAdapter getMDCAdapter() {
        return mdcAdapter;
    }

    @Override
    public String getRequestedApiVersion() {
        return REQUESTED_API_VERSION;
    }
    
}
