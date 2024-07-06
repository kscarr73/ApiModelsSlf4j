package com.progbits.api.slf4j;

import com.progbits.api.exception.ApiException;
import com.progbits.api.model.ApiObject;
import com.progbits.api.writer.JsonObjectWriter;
import com.progbits.api.writer.YamlObjectWriter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

/**
 * Slf4j Logger Implementation
 * 
 * @author scarr
 */
public class ApiModelsFactory implements ILoggerFactory {
    ConcurrentMap<String, ApiModelsLogger> loggerMap = new ConcurrentHashMap<>();

    @Override
    public Logger getLogger(String name) {
        return loggerMap.computeIfAbsent(name, this::createLogger);
    }

    protected ApiModelsLogger createLogger(String name) {
        return new ApiModelsLogger(name);
    }

    /**
     * Return Logger Levels.
     *
     * @param name Full Logger Name, can be partial path. Partial Path results
     * in multiples
     * @param format JSON, application/json, YAML, application/x+yaml, String
     * @return
     */
    public String getLoggerLevel(String name, String format) {
        String lclFormat = format;

        if (lclFormat == null || lclFormat.isEmpty()) {
            lclFormat = "string";
        }

        return switch (lclFormat.toLowerCase()) {
            case "json", "application/json" ->
                getLoggerLevelJson(name);
            case "yaml", "application/x-yaml" ->
                getLoggerLevelYaml(name);
            default ->
                getLoggerLevelString(name);
        };
    }

    private String getLoggerLevelString(String name) {
        StringBuilder sb = new StringBuilder();

        for (var entry : loggerMap.entrySet()) {
            if (entry.getKey().startsWith(name)) {
                sb.append(entry.getKey())
                    .append(": ")
                    .append(ApiModelsSlf4jConfig.getInstance().convertIntToLevel(entry.getValue().getCurrentLevel()))
                    .append("\n");
            }
        }

        return sb.toString();
    }

    private ApiObject getLoggerLevelsObject(String name) {
        ApiObject objRet = new ApiObject();

        objRet.createList("levels");

        for (var entry : loggerMap.entrySet()) {
            if (entry.getKey().startsWith(name)) {
                ApiObject objLevel = objRet.getListAdd("levels");

                objLevel.setString("logger", entry.getKey());
                objLevel.setString("level", ApiModelsSlf4jConfig.getInstance().convertIntToLevel(entry.getValue().getCurrentLevel()));
            }
        }

        return objRet;
    }

    private String getLoggerLevelJson(String name) {
        JsonObjectWriter jsonWriter = new JsonObjectWriter(true);

        try {
            return jsonWriter.writeSingle(getLoggerLevelsObject(name));
        } catch (ApiException ex) {
            return ex.getMessage();
        }
    }

    private String getLoggerLevelYaml(String name) {
        YamlObjectWriter yamlWriter = new YamlObjectWriter(true);

        try {
            return yamlWriter.writeSingle(getLoggerLevelsObject(name));
        } catch (ApiException ex) {
            return ex.getMessage();
        }
    }

    public void setLoggerLevel(String name, String level) {
        int logLevel = ApiModelsSlf4jConfig.getInstance().getLogLevel(level);

        for (var entry : loggerMap.entrySet()) {
            if (entry.getKey().startsWith(name)) {
                entry.getValue().setLogLevel(logLevel);
            }
        }
    }
}
