package com.progbits.api.slf4j;

import com.progbits.api.slf4j.utils.ApiModelLogCapture;
import java.util.Optional;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;
import org.slf4j.helpers.AbstractLogger;
import org.slf4j.spi.LoggingEventAware;

/**
 * Logger Implementation for SLF4j
 * 
 * @author scarr
 */
public class ApiModelsLogger extends AbstractLogger implements LoggingEventAware {

    public ApiModelsLogger(String loggerName) {
        this.name = loggerName;
        currentLogLevel = config.getLogLevel(loggerName);
    }
    
    private static final ApiModelsSlf4jConfig config = ApiModelsSlf4jConfig.getInstance();
    private static final ApiModelsOutputManager outputMngr = ApiModelsOutputManager.getInstance();
    
    private int currentLogLevel = Level.INFO.toInt();
 
    public int getCurrentLevel() {
        return currentLogLevel;
    }
    
    public void setLogLevel(String lvl) {
        this.currentLogLevel = config.convertLevelToInt(lvl);
    }
    
    public void setLogLevel(int lvl) {
        this.currentLogLevel = lvl;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleNormalizedLoggingCall(Level level, Marker marker, String string, Object[] os, Throwable thrwbl) {
        outputMngr.sendLog(ApiModelLogCapture.logToApiObject(name, level, marker, string, os, thrwbl));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void log(LoggingEvent le) {
        outputMngr.sendLog(ApiModelLogCapture.eventToApiObject(le));
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected String getFullyQualifiedCallerName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTraceEnabled() {
        return Level.TRACE.toInt() >= currentLogLevel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTraceEnabled(Marker marker) {
        Optional<Boolean> bMrkRet = config.getMarkerLevel(Level.TRACE, marker);
        
        if (bMrkRet.isEmpty()) {
            return Level.TRACE.toInt() >= currentLogLevel;
        } else {
            return bMrkRet.get();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDebugEnabled() {
        return Level.DEBUG.toInt() >= currentLogLevel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDebugEnabled(Marker marker) {
        Optional<Boolean> bMrkRet = config.getMarkerLevel(Level.DEBUG, marker);
        
        if (bMrkRet.isEmpty()) {
            return Level.DEBUG.toInt() >= currentLogLevel;
        } else {
            return bMrkRet.get();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isInfoEnabled() {
        return Level.INFO.toInt() >= currentLogLevel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isInfoEnabled(Marker marker) {
        Optional<Boolean> bMrkRet = config.getMarkerLevel(Level.INFO, marker);
        
        if (bMrkRet.isEmpty()) {
            return Level.INFO.toInt() >= currentLogLevel;
        } else {
            return bMrkRet.get();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isWarnEnabled() {
        return Level.WARN.toInt() >= currentLogLevel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isWarnEnabled(Marker marker) {
        Optional<Boolean> bMrkRet = config.getMarkerLevel(Level.WARN, marker);
        
        if (bMrkRet.isEmpty()) {
            return Level.WARN.toInt() >= currentLogLevel;
        } else {
            return bMrkRet.get();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isErrorEnabled() {
        return Level.ERROR.toInt() >= currentLogLevel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isErrorEnabled(Marker marker) {
        Optional<Boolean> bMrkRet = config.getMarkerLevel(Level.ERROR, marker);
        
        if (bMrkRet.isEmpty()) {
            return Level.ERROR.toInt() >= currentLogLevel;
        } else {
            return bMrkRet.get();
        }
    }

}
