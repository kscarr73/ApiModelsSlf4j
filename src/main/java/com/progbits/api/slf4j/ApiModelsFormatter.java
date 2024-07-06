package com.progbits.api.slf4j;

import com.progbits.api.model.ApiObject;
import static com.progbits.api.slf4j.utils.LoggerConstants.*;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handle Formatting for JSON, YAML, and String
 * 
 * @author scarr
 */
public class ApiModelsFormatter {
    private static ApiModelsFormatter instance = null;

    /**
     * Get singleton configured instance
     * 
     * @return The instance for this class
     */
    public static ApiModelsFormatter getInstance() {
        if (instance == null) {
            instance = new ApiModelsFormatter();

            instance.configure();
        }

        return instance;
    }

    protected void configure() {
        config = ApiModelsSlf4jConfig.getInstance();
    }

    private ApiModelsSlf4jConfig config = null;
    private ConcurrentHashMap<String, ApiObject> formatNames = new ConcurrentHashMap<>();

    /**
     * Process Log with Specific Format
     * 
     * @param outputType JSON, YAML, String output type for the Formatting
     * @param formatName The format name to use
     * @param le The Log Event object to process
     * 
     * @return The Processed Log Object
     */
    public ApiObject processFormat(String outputType, String formatName, ApiObject le) {
        ApiObject objFormat = getNamedFormat(formatName);
        ApiObject outputObj = null;

        if (objFormat != null) {
            if (OUTPUT_TYPE_STRING.equals(outputType)) {
                outputObj = processStringOutput(objFormat, le);
            } else {
                outputObj = processObjectOutput(objFormat, le);
            }
        }

        return outputObj;
    }

    /**
     * Get Format designated for this Output
     * 
     * @param formatName The named format to gather
     * 
     * @return The Format found by the request
     */
    public ApiObject getNamedFormat(String formatName) {
        return formatNames.computeIfAbsent(formatName, this::getNamedFormatViaConfig);
    }

    private ApiObject getNamedFormatViaConfig(String formatName) {
        return config.getConfig().getObject("formats[name=" + formatName + "]");
    }

    /**
     * Process Format for Object Output Types
     * 
     * @param frmt Definition for Format
     * @param le Log Entry to Process
     * 
     * @return The processed object for the Format
     */
    public ApiObject processObjectOutput(ApiObject frmt, ApiObject le) {
        ApiObject objRet = new ApiObject();

        for (var field : frmt.getList("fields")) {
            objRet.put(field.getString(MAIN_NAME), processField(field, le));
        }

        return objRet;
    }

    /**
     * Process String output for Format
     * 
     * @param frmt Definition for Format
     * @param le Log Entry to Process
     * 
     * @return The processed object for this Format
     */
    public ApiObject processStringOutput(ApiObject frmt, ApiObject le) {
        StringBuilder sb = new StringBuilder();

        for (var field : frmt.getList("fields")) {
            if (!sb.isEmpty()) {
                sb.append(frmt.getString("delimiter", " "));
            }

            sb.append(frmt.getString("prefix", ""))
                .append(processField(field, le))
                .append(frmt.getString("postfix", ""));
        }

        return new ApiObject().setString("output", sb.toString());
    }

    /**
     * Process a Field for the Log Entry
     * 
     * @param field The field definition to process
     * @param le Log Entry to process
     * 
     * @return The object from the Log Entry formatted
     */
    public Object processField(ApiObject field, ApiObject le) {
        String mapping = field.getString(FIELD_MAPPING, field.getString(MAIN_NAME));
        
        if (mapping.startsWith("exception") && le.containsKey(FIELD_THROWABLE) && le.get(FIELD_THROWABLE) instanceof Throwable t) {
            return processThrowable(mapping, field, t);
        } else if ("date".equals(mapping)) {
            return Instant.ofEpochMilli(le.getLong(FIELD_TIMESTAMP)).atOffset(ZoneOffset.UTC).toString();
        } else {
            return le.getCoreObject(mapping);
        }
    }

    /**
     * Process the Throwable Mapping
     * 
     * @param mapping The mapping to process
     * @param field The field mapping
     * @param t The Throwable to process
     * 
     * @return The object from the Format
     */
    public Object processThrowable(String mapping, ApiObject field, Throwable t) {
        String[] spltMap = mapping.split("\\.");

        return switch (spltMap[1]) {
            case MAIN_NAME ->
                t.getClass().getSimpleName();
            case FIELD_MESSAGE ->
                t.getMessage();
            case "stacktrace" ->
                processStackTrace(field, t);
            default ->
                t.getMessage();
        };
    }

    /**
     * Process the Stacktrace of a Throwable
     * 
     * @param field The field format to use while processing
     * @param t The Throwable to process
     * 
     * @return The object from the Format
     */
    public Object processStackTrace(ApiObject field, Throwable t) {
        StringBuilder sb = new StringBuilder();
        int limit = field.getInteger("limit", 20);
        int tCnt = 0;
        int afterLimitCnt = 0;
        sb.append(t.getClass().getCanonicalName()).append(": ").append(t.getMessage()).append("\n");

        for (var st : t.getStackTrace()) {
            if (tCnt < limit) {
                sb.append(" at ").append(st.getClassName())
                    .append("(").append(st.getMethodName()).append(":").append(st.getLineNumber()).append(")").append("\n");
            } else {
                afterLimitCnt++;
            }
            
            tCnt++;
        }
        
        if (afterLimitCnt > 0) {
            sb.append(" Plus ").append(afterLimitCnt).append( " more Classes");
        }

        return sb.toString();
    }
}
