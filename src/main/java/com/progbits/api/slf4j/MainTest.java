package com.progbits.api.slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scarr
 */
public class MainTest {
    private static final Logger LOG = LoggerFactory.getLogger(MainTest.class);
    
    public void run() {
        long lStart = System.currentTimeMillis();
        
        LOG.info("This is a test");
        
        for (int x=0; x<100000; x++) {
            LOG.info("This is a test Bulk: {}", x);
        }
        
        LOG.warn("Test Time: {}", System.currentTimeMillis() - lStart);
    }
    
    public static void main(String[] args) {
        MainTest main = new MainTest();
        
        main.run();
    }
}
