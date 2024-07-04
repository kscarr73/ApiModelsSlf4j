package com.progbits.api.slf4j.utils;

import static com.progbits.api.slf4j.utils.LoggerConstants.*;
import com.progbits.api.model.ApiObject;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;
import org.slf4j.helpers.MessageFormatter;

/**
 * Takes a Log Envent and Converts to an ApiObject
 *
 * @author scarr
 */
public class ApiModelLogCapture {

    public static ApiObject eventToApiObject(LoggingEvent le) {
        ApiObject retObj = new ApiObject();
        
        String strMessage = le.getMessage();
        
        if (le.getArguments() != null && !le.getArguments().isEmpty()) {
            strMessage = MessageFormatter.basicArrayFormat(strMessage, le.getArgumentArray());
        }
        
        if (le.getTimeStamp() == 0) {
            retObj.setLong(FIELD_TIMESTAMP, System.currentTimeMillis());
        } else {
            retObj.setLong(FIELD_TIMESTAMP, le.getTimeStamp());
        }
        
        retObj.setString(FIELD_MESSAGE, strMessage);
        retObj.setString(FIELD_LEVEL, le.getLevel().name());
        retObj.setString(FIELD_THREADNAME, le.getThreadName());
        retObj.setString(FIELD_LOGGER, le.getLoggerName());
        retObj.put(FIELD_THROWABLE, le.getThrowable());
        
        if (le.getMarkers() != null && !le.getMarkers().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            
            for (var entry : le.getMarkers()) {
                if (sb.length() > 0) {
                    sb.append(" ");
                }
                
                sb.append(entry.toString());
            }
            
            retObj.setString(FIELD_MARKER, sb.toString());
        }
        
        if (le.getKeyValuePairs() != null && !le.getKeyValuePairs().isEmpty()) {
            ApiObject objKeyVals = new ApiObject();
            
            for (var keyVal : le.getKeyValuePairs()) {
                objKeyVals.put(keyVal.key, keyVal.value);
            }
            
            retObj.setObject(FIELD_MAP, objKeyVals);
        }
        
        retObj.setObject(FIELD_MDC, getMdcObject());
        
        return retObj;
    }

    public static ApiObject logToApiObject(String logger, Level level, Marker marker, String message, Object[] args, Throwable thrwbl) {
        ApiObject retObj = new ApiObject();

        String strMessage = message;
        
        if (args != null && args.length > 0) {
            strMessage = MessageFormatter.basicArrayFormat(strMessage, args);
        }
        
        retObj.setLong(FIELD_TIMESTAMP, System.currentTimeMillis());
        retObj.setString(FIELD_MESSAGE, strMessage);
        retObj.setString(FIELD_LEVEL, level.name());
        retObj.setString(FIELD_THREADNAME, Thread.currentThread().getName());
        retObj.setString(FIELD_LOGGER, logger);
        retObj.put(FIELD_THROWABLE, thrwbl);
        
        if (marker != null) {
            retObj.setString(FIELD_MARKER, marker.toString());
        }

        retObj.setObject(FIELD_MDC, getMdcObject());

        return retObj;
    }

    public static ApiObject getMdcObject() {
        ApiObject mdcObj = null;

        var mdcMap = MDC.getCopyOfContextMap();

        if (mdcMap != null && !mdcMap.isEmpty()) {
            mdcObj = new ApiObject();

            for (var entry : mdcMap.entrySet()) {
                mdcObj.put(entry.getKey(), entry.getValue());
            }
        }

        return mdcObj;
    }
}
