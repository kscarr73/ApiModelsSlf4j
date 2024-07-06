package com.progbits.api.slf4j;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test for Validating Slf4j Implementation
 * 
 * @author scarr
 */
public class MainTest {
    private static final Logger LOG = LoggerFactory.getLogger(MainTest.class);
    
    private List<String> outputLogs = new ArrayList<>();
    
    /**
     * Run
     */
    public void run() {
        long lStart = System.currentTimeMillis();
        
        LOG.info("This is a test");
        
        run10000();

        run100000();
        
        for (var entry : outputLogs) {
            LOG.info(entry);
        }

        LOG.warn("Full Test Time: {}", System.currentTimeMillis() - lStart);
    }
    
    private void run10000() {
        long lStart = System.currentTimeMillis();

        for (int x=0; x<10000; x++) {
            LOG.info("This is a test Bulk: {}", x);
        }
        
        outputLogs.add("Test Time: " + (System.currentTimeMillis() - lStart));
        
    }

    private void run100000() {
        long lStart = System.currentTimeMillis();

        for (int x=0; x<100000; x++) {
            LOG.info("This is a test Bulk: {}", x);
        }
        
        outputLogs.add("Test Time: " + (System.currentTimeMillis() - lStart)); 
    }

    /**
     * Test Main class for Slf4j Implementation
     * 
     * @param args Argument list from command line
     */
    public static void main(String[] args) {
        MainTest main = new MainTest();
        
        main.run();
    }
}
