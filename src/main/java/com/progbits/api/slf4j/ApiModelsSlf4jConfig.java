package com.progbits.api.slf4j;

import com.progbits.api.exception.ApiClassNotFoundException;
import com.progbits.api.exception.ApiException;
import com.progbits.api.model.ApiObject;
import com.progbits.api.parser.YamlObjectParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.event.Level;

/**
 *
 * @author scarr
 */
public class ApiModelsSlf4jConfig {

    private static final Logger LOG = LoggerFactory.getLogger(ApiModelsSlf4jConfig.class);

    private static ApiModelsSlf4jConfig instance = null;
    private static ReentrantLock lock;
    public CountDownLatch configured = new CountDownLatch(1);

    public static ApiModelsSlf4jConfig getInstance() {
        if (lock == null) {
            lock = new ReentrantLock();
        }

        if (instance == null) {
            lock.lock();

            try {
                if (instance == null) {
                    instance = new ApiModelsSlf4jConfig();

                    instance.configure();
                }
            } finally {
                lock.unlock();
            }
        }

        try {
            instance.configured.await();
        } catch (InterruptedException ex) {
            // nothing to report
        }

        return instance;
    }

    protected void configure() {
        YamlObjectParser parser = new YamlObjectParser(true);
        Path path = Paths.get("./" + APIMODELS_CONFIG_FILE);

        if (Files.exists(path)) {
            try (BufferedReader br = Files.newBufferedReader(path)) {
                config = parser.parseSingle(br);
            } catch (ApiException | ApiClassNotFoundException api) {
                System.out.println("Failed to Parse " + APIMODELS_CONFIG_FILE + ": " + api.getMessage());
            } catch (IOException io) {
                System.out.println("Configure Failure: " + io.getMessage());
            }
        } else {
            try (InputStream in = ClassLoader.getSystemResourceAsStream(APIMODELS_CONFIG_FILE)) {
                if (in != null) {
                    config = parser.parseSingle(new InputStreamReader(in));
                } else {
                    config = parser.parseSingle(new StringReader(APIMODELS_DEFAULT_CONFIG));

                    System.out.println("File Not Found On Classpath" + APIMODELS_CONFIG_FILE);
                }
            } catch (ApiException | ApiClassNotFoundException api) {
                System.out.println("Failed to Parse " + APIMODELS_CONFIG_FILE + ": " + api.getMessage());
            } catch (IOException io) {
                // nothing to do here
            }
        }

        ApiObject logs = getConfigLogs();

        defaultLogLevel = convertLevelToInt(logs.getString("default.level", "INFO"));

        configured.countDown();

        outputMngr = ApiModelsOutputManager.getInstance();
    }

    private static ApiModelsOutputManager outputMngr;

    private static final String APIMODELS_CONFIG_FILE = "apilogging.yaml";
    private ApiObject config = null;

    private int defaultLogLevel = Level.INFO.toInt();

    private ConcurrentHashMap<String, Integer> loggerLevels = new ConcurrentHashMap<>();

    private static final String APIMODELS_DEFAULT_CONFIG = """
                                                           outputs:
                                                              SysOut:
                                                                type: JSON
                                                                output: SysOut
                                                                format: default
                                                           logs: 
                                                             default: 
                                                               level: INFO
                                                               output: SysOut
                                                           formats:
                                                             - name: default
                                                               fields:
                                                                 - name: timestamp
                                                                 - name: level
                                                                 - name: message
                                                                 - name: logger
                                                                 - name: exceptionName
                                                                   mapping: exception.name
                                                                 - name: stackTrace
                                                                   mapping: exception.stacktrace
                                                           """;

    public ApiObject getConfig() {
        return config;
    }

    public ApiObject getConfigLogs() {
        if (!config.containsKey("logs")) {
            config.createObject("logs");
        }

        return config.getObject("logs");
    }

    public boolean checkLogLevel(Level lvl, String loggerName) {
        return lvl.toInt() >= loggerLevels.getOrDefault(loggerName, defaultLogLevel);
    }

    public int getLogLevel(String loggerName) {
        Optional<Integer> iRet = Optional.empty();

        if (loggerName != null) {
            ApiObject objLogs = getConfigLogs();

            if (!objLogs.isEmpty()) {
                for (var entry : objLogs.entrySet()) {
                    if (loggerName.startsWith(entry.getKey())) {
                        String strLevelName = ((ApiObject) entry.getValue()).getString("level");

                        iRet = Optional.of(convertLevelToInt(strLevelName));
                        break;
                    }
                }
            }
        }

        if (iRet.isEmpty()) {
            iRet = Optional.of(defaultLogLevel);
        }

        return iRet.get();
    }

    public Optional<Boolean> checkLogLevel(Level lvl, String loggerName, boolean bRetDefault) {
        if (!bRetDefault) {
            if (loggerLevels.containsKey(loggerName)) {
                return Optional.of(lvl.toInt() >= loggerLevels.get(loggerName));
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.of(lvl.toInt() >= loggerLevels.getOrDefault(loggerName, defaultLogLevel));
        }
    }

    public Optional<Boolean> getMarkerLevel(Level lvl, Marker mrk) {
        if (null == mrk) {
            return Optional.empty();
        } else {
            Optional<Boolean> retVal = Optional.empty();

            StringBuilder sb = new StringBuilder();

            sb.append(mrk.getName());

            if (mrk.hasReferences()) {
                Iterator<Marker> itr = mrk.iterator();

                while (itr.hasNext()) {
                    Marker nxtMrk = itr.next();
                    sb.append(".").append(nxtMrk.getName());
                }
            }

            return checkLogLevel(lvl, sb.toString(), false);
        }
    }

    public int convertLevelToInt(String levelName) {
        if (null == levelName) {
            return Level.INFO.toInt();
        } else {
            return switch (levelName.toLowerCase()) {
                case "trace" ->
                    Level.TRACE.toInt();
                case "debug" ->
                    Level.DEBUG.toInt();
                case "info" ->
                    Level.INFO.toInt();
                case "warn" ->
                    Level.WARN.toInt();
                case "error" ->
                    Level.ERROR.toInt();
                case "off" ->
                    -1;

                default ->
                    Level.INFO.toInt();
            };
        }
    }
    
    public String convertIntToLevel(int level) {

            return switch (level) {
                case 0 ->
                    "TRACE";
                case 10 ->
                    "DEBUG";
                case 20 ->
                    "INFO";
                case 30 ->
                    "WARN";
                case 40 ->
                    "ERROR";
                case -1 ->
                    "OFF";

                default ->
                    "INFO";
            };
        
    }

}
