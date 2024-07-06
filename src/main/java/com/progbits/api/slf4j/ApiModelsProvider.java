package com.progbits.api.slf4j;

import org.slf4j.ILoggerFactory;
import org.slf4j.IMarkerFactory;
import org.slf4j.helpers.BasicMDCAdapter;
import org.slf4j.helpers.BasicMarkerFactory;
import org.slf4j.spi.MDCAdapter;
import org.slf4j.spi.SLF4JServiceProvider;

/**
 * Slf4j Provider Implementation for ApiModels
 * 
 * @author scarr
 */
public class ApiModelsProvider implements SLF4JServiceProvider {

    public static String REQUESTED_API_VERSION = "2.0.13";
    
    private ILoggerFactory loggerFactory;
    private IMarkerFactory markerFactory;
    private MDCAdapter mdcAdapter;
    
    /** {@inheritDoc} */
    @Override
    public void initialize() {
        loggerFactory = new ApiModelsFactory();
        markerFactory = new BasicMarkerFactory();
        mdcAdapter = new BasicMDCAdapter();
        ApiModelsSlf4jConfig.getInstance();
    }
    
    /** {@inheritDoc} */
    @Override
    public ILoggerFactory getLoggerFactory() {
        return loggerFactory;
    }

    /** {@inheritDoc} */
    @Override
    public IMarkerFactory getMarkerFactory() {
        return markerFactory;
    }

    /** {@inheritDoc} */
    @Override
    public MDCAdapter getMDCAdapter() {
        return mdcAdapter;
    }

    /** {@inheritDoc} */
    @Override
    public String getRequestedApiVersion() {
        return REQUESTED_API_VERSION;
    }
    
}
