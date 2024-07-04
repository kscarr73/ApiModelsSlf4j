package com.progbits.api.slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

/**
 *
 * @author scarr
 */
public class ApiModelsSlf4jConfigNGTest {
    private static final Logger LOG = LoggerFactory.getLogger(ApiModelsSlf4jConfigNGTest.class);
    
    public ApiModelsSlf4jConfigNGTest() {
    }

    @Test
    public void testCreate() {
        LOG.info("This is a test");
        
        LOG.error("This {} other {}", "Testing", "Format");
        
        LOG.info("This is a test", new Exception("This is a test", new NullPointerException("Another test")));
    }
    
    @Test
    public void testMultiple10000() {
        for (int x=0; x<10000;x++) {
            LOG.info("This is a test Run 1: {}", x);
        }
    }
    
    @Test
    public void testMultiple100000() {
        for (int x=0; x<100000;x++) {
            LOG.info("This is a test Run 2: {}", x);
        }
    }
    
//    @AfterClass
//    public void pause() {
//        try {
//            TimeUnit.SECONDS.sleep(20);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }
    
}
