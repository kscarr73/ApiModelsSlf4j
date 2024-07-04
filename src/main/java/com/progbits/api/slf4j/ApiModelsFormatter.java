package com.progbits.api.slf4j;

import com.progbits.api.model.ApiObject;
import static com.progbits.api.slf4j.utils.LoggerConstants.*;
import java.time.Instant;
import java.time.ZoneOffset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scarr
 */
public class ApiModelsFormatter {

    private static final Logger LOG = LoggerFactory.getLogger(ApiModelsFormatter.class);

    private static ApiModelsFormatter instance = null;

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

    ApiModelsSlf4jConfig config = null;

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

    public ApiObject getNamedFormat(String formatName) {
        return config.getConfig().getObject("formats[name=" + formatName + "]");
    }

    public ApiObject processObjectOutput(ApiObject frmt, ApiObject le) {
        ApiObject objRet = new ApiObject();

        for (var field : frmt.getList("fields")) {
            objRet.put(field.getString(MAIN_NAME), processField(field, le));
        }

        return objRet;
    }

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
