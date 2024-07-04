package com.progbits.api.slf4j;

import com.progbits.api.slf4j.utils.ApiModelLogCapture;
import java.util.Optional;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;
import org.slf4j.helpers.AbstractLogger;
import org.slf4j.spi.LoggingEventAware;

/**
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
    
    private String computeShortName() {
        return name.substring(name.lastIndexOf(".") + 1);
    }
 
    public int getCurrentLevel() {
        return currentLogLevel;
    }
    
    public void setLogLevel(String lvl) {
        this.currentLogLevel = config.convertLevelToInt(lvl);
    }
    
    public void setLogLevel(int lvl) {
        this.currentLogLevel = lvl;
    }
    
    @Override
    protected void handleNormalizedLoggingCall(Level level, Marker marker, String string, Object[] os, Throwable thrwbl) {
        outputMngr.sendLog(ApiModelLogCapture.logToApiObject(name, level, marker, string, os, thrwbl));
    }

    @Override
    public void log(LoggingEvent le) {
        outputMngr.sendLog(ApiModelLogCapture.eventToApiObject(le));
    }
    
    @Override
    protected String getFullyQualifiedCallerName() {
        return name;
    }

    @Override
    public boolean isTraceEnabled() {
        return Level.TRACE.toInt() >= currentLogLevel;
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        Optional<Boolean> bMrkRet = config.getMarkerLevel(Level.TRACE, marker);
        
        if (bMrkRet.isEmpty()) {
            return Level.TRACE.toInt() >= currentLogLevel;
        } else {
            return bMrkRet.get();
        }
    }

    @Override
    public boolean isDebugEnabled() {
        return Level.DEBUG.toInt() >= currentLogLevel;
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        Optional<Boolean> bMrkRet = config.getMarkerLevel(Level.DEBUG, marker);
        
        if (bMrkRet.isEmpty()) {
            return Level.DEBUG.toInt() >= currentLogLevel;
        } else {
            return bMrkRet.get();
        }
    }

    @Override
    public boolean isInfoEnabled() {
        return Level.INFO.toInt() >= currentLogLevel;
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        Optional<Boolean> bMrkRet = config.getMarkerLevel(Level.INFO, marker);
        
        if (bMrkRet.isEmpty()) {
            return Level.INFO.toInt() >= currentLogLevel;
        } else {
            return bMrkRet.get();
        }
    }

    @Override
    public boolean isWarnEnabled() {
        return Level.WARN.toInt() >= currentLogLevel;
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        Optional<Boolean> bMrkRet = config.getMarkerLevel(Level.WARN, marker);
        
        if (bMrkRet.isEmpty()) {
            return Level.WARN.toInt() >= currentLogLevel;
        } else {
            return bMrkRet.get();
        }
    }

    @Override
    public boolean isErrorEnabled() {
        return Level.ERROR.toInt() >= currentLogLevel;
    }

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
