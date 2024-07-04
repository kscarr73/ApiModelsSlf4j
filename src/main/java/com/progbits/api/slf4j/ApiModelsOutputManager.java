package com.progbits.api.slf4j;

import com.progbits.api.model.ApiObject;
import static com.progbits.api.slf4j.utils.LoggerConstants.*;
import com.progbits.api.writer.JsonObjectWriter;
import com.progbits.api.writer.YamlObjectWriter;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scarr
 */
public class ApiModelsOutputManager {

    private static final Logger LOG = LoggerFactory.getLogger(ApiModelsOutputManager.class);

    private static ApiModelsOutputManager instance = null;
    private static ReentrantLock lock;
    public CountDownLatch configured = new CountDownLatch(1);

    public static ApiModelsOutputManager getInstance() {
        if (lock == null) {
            lock = new ReentrantLock();
        }

        if (instance == null) {
            lock.lock();

            try {
                instance = new ApiModelsOutputManager();

                instance.configure();
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
        config = ApiModelsSlf4jConfig.getInstance();
        setupWriters();

        logProcessor = Thread.ofPlatform()
            .name("Api Model Logging")
            .daemon(true)
            .start(this::outputLogs);

        configured.countDown();

        Thread shutdownThread = Thread.ofPlatform().daemon(false).unstarted(() -> {
            final LinkedList<ApiObject> finalLogs = new LinkedList<>();

            queue.drainTo(finalLogs);

            System.out.println("Shutdown Hook Cleaning Logs: " + finalLogs.size());
            
            for (var log : finalLogs) {
                processLog(log);
            }
        });
        
        Runtime.getRuntime().addShutdownHook(shutdownThread);
    }

    private AtomicBoolean bContinue = new AtomicBoolean(true);

    private ApiModelsSlf4jConfig config;
    private static final LinkedBlockingQueue<ApiObject> queue = new LinkedBlockingQueue<>();

    private Thread logProcessor;
    private Map<String, PrintWriter> outputs = new ConcurrentHashMap<>();
    private Map<String, String> outputFormat = new ConcurrentHashMap<>();
    private Map<String, String> outputType = new ConcurrentHashMap<>();

    private static final JsonObjectWriter jsonWriter = new JsonObjectWriter(true);
    private static final YamlObjectWriter yamlWriter = new YamlObjectWriter(true);

    public void sendLog(ApiObject log) {
        queue.add(log);
    }
    
    private void setupWriters() {
        if (config.getConfig().isSet(MAIN_OUTPUTS)) {
            for (var entry : config.getConfig().getObject(MAIN_OUTPUTS).entrySet()) {
                ApiObject entryValue = (ApiObject) entry.getValue();
                String outputStyle = entryValue.getString("output", "SysOut");

                try {
                    PrintWriter writerEntry = null;

                    if ("SYSOUT".equalsIgnoreCase(outputStyle)) {
                        writerEntry = new PrintWriter(System.out, true);
                    } else if ("SYSERR".equalsIgnoreCase(outputStyle)) {
                        writerEntry = new PrintWriter(System.err, true);
                    } else {
                        FileOutputStream file = new FileOutputStream(outputStyle);
                        writerEntry = new PrintWriter(file, true);
                    }

                    outputType.put(entry.getKey(), entryValue.getString(MAIN_TYPE));
                    outputFormat.put(entry.getKey(), entryValue.getString(MAIN_FORMAT));
                    outputs.put(entry.getKey(), writerEntry);
                } catch (Exception fnf) {
                    System.out.println("File " + outputStyle + " Error: " + fnf.getMessage());
                }
            }
        }
    }

    private void outputLogs() {
        while (!(Thread.currentThread().isInterrupted())) {
            try {
                var entry = queue.take();

                processLog(entry);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }

//        System.out.println("In Interupt");
//
//        final LinkedList<ApiObject> finalLogs = new LinkedList<>();
//
//        queue.drainTo(finalLogs);
//
//        for (var log : finalLogs) {
//            processLog(log);
//        }

    }

    private void processLog(ApiObject entry) {

        ApiObject logEntry = getLoggerOutput(entry.getString(FIELD_LOGGER));

        if (logEntry != null) {
            if (logEntry.getType(MAIN_OUTPUT) == ApiObject.TYPE_STRINGARRAY) {
                for (var logOutput : logEntry.getStringArray(MAIN_OUTPUT)) {
                    processLogEntry(logOutput, entry);
                }
            } else {
                processLogEntry(logEntry.getString(MAIN_OUTPUT), entry);
            }
        }

    }

    private void processLogEntry(String outputName, ApiObject logEntry) {
        ApiObject output = ApiModelsFormatter.getInstance().processFormat(outputName, outputFormat.get(outputName), logEntry);

        switch (outputType.get(outputName)) {
            case OUTPUT_TYPE_YAML ->
                outputYaml(outputName, output);

            case OUTPUT_TYPE_STRING ->
                outputString(outputName, output);

            default ->
                outputJson(outputName, output);
        }
    }

    private void outputJson(String outputName, ApiObject outputLog) {
        PrintWriter writer = outputs.get(outputName);

        try {
            if (writer != null) {
                String strOutput = jsonWriter.writeSingle(outputLog);
                strOutput += "\n";

                writer.print(strOutput);
                writer.flush();
            }
        } catch (Exception ex) {
            // Nothing to log here yet
        }
    }

    private void outputYaml(String outputName, ApiObject outputLog) {
        PrintWriter writer = outputs.get(outputName);

        try {
            if (writer != null) {
                String strOutput = yamlWriter.writeSingle(outputLog);

                writer.print(strOutput);
                writer.flush();
            }
        } catch (Exception ex) {
            // Nothing to log here yet
        }
    }

    private void outputString(String outputName, ApiObject outputLog) {
        PrintWriter writer = outputs.get(outputName);

        if (writer != null) {
            String strOutput = outputLog.getString("output");
            strOutput += "\n";

            writer.print("---\n");
            writer.print(strOutput);
            writer.flush();
        }
    }

    private ApiObject getLoggerOutput(String className) {
        ApiObject objRet = config.getConfig().getObject("logs.default");

        for (var log : config.getConfig().getObject("logs").entrySet()) {
            if (!log.getKey().equals("default")) {
                if (className.startsWith(log.getKey())) {
                    objRet = (ApiObject) log.getValue();
                    break;
                }
            }
        }

        return objRet;
    }
}
