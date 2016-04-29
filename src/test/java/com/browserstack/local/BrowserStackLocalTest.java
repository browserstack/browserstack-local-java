package com.browserstack.local;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BrowserStackLocalTest {
    private Local l;
    private Map<String, String> options;

    @Before
    public void setUp() throws Exception {
        l = new Local();
        options = new HashMap<String, String>();
        options.put("key", System.getenv("BROWSERSTACK_ACCESS_KEY"));
    }

    @Test
    public void testIsRunning() throws Exception {
        assertFalse(l.isRunning());
        l.start(options);
        assertTrue(l.isRunning());
    }

    @Test
    public void testMultipleBinary() throws Exception {
        l.start(options);
        assertTrue(l.isRunning());
        Local l2 = new Local();
        try {
            l2.start(options);
        } catch (LocalException e) {
            assertFalse(l2.isRunning());
        }
    }

    @Test
    public void testEnableVerbose() throws Exception {
        options.put("v", "true");
        options.put("onlyCommand", "true");
        l.start(options);
        assertTrue(l.command.contains("-vvv"));
    }

    @Test
    public void testSetFolder() throws Exception {
        options.put("f", "/var/html");
        options.put("onlyCommand", "true");
        l.start(options);
        assertTrue(l.command.contains("-f"));
        assertTrue(l.command.contains("/var/html"));
    }

    @Test
    public void testEnableForce() throws Exception {
        options.put("force", "true");
        options.put("onlyCommand", "true");
        l.start(options);
        assertTrue(l.command.contains("-force"));
    }

    @Test
    public void testEnableOnly() throws Exception {
        options.put("only", "true");
        options.put("onlyCommand", "true");
        l.start(options);
        assertTrue(l.command.contains("-only"));
    }

    @Test
    public void testEnableOnlyAutomate() throws Exception {
        options.put("onlyAutomate", "true");
        options.put("onlyCommand", "true");
        l.start(options);
        assertTrue(l.command.contains("-onlyAutomate"));
    }

    @Test
    public void testEnableForceLocal() throws Exception {
        options.put("forcelocal", "true");
        options.put("onlyCommand", "true");
        l.start(options);
        assertTrue(l.command.contains("-forcelocal"));
    }

    @Test
    public void testEnableForceProxy() throws Exception {
        options.put("forceproxy", "true");
        options.put("onlyCommand", "true");
        l.start(options);
        assertTrue(l.command.contains("-forceproxy"));
    }

    @Test
    public void testSetLocalIdentifier() throws Exception {
        options.put("localIdentifier", "abcdef");
        options.put("onlyCommand", "true");
        l.start(options);
        assertTrue(l.command.contains("-localIdentifier"));
        assertTrue(l.command.contains("abcdef"));
    }

    @Test
    public void testSetProxy() throws Exception {
        options.put("proxyHost", "localhost");
        options.put("proxyPort", "8080");
        options.put("proxyUser", "user");
        options.put("proxyPass", "pass");
        options.put("onlyCommand", "true");
        l.start(options);
        assertTrue(l.command.contains("-proxyHost"));
        assertTrue(l.command.contains("localhost"));
        assertTrue(l.command.contains("-proxyPort"));
        assertTrue(l.command.contains("8080"));
        assertTrue(l.command.contains("-proxyUser"));
        assertTrue(l.command.contains("user"));
        assertTrue(l.command.contains("-proxyPass"));
        assertTrue(l.command.contains("pass"));
    }

    @Test
    public void testSetHosts() throws Exception {
        options.put("hosts", "localhost,8000,0");
        options.put("onlyCommand", "true");
        l.start(options);
        assertTrue(l.command.contains("localhost,8000,0"));
    }

    @Test
    public void testCustomArguments() throws Exception {
        options.put("customKey", "customValue");
        options.put("customKey2", "customValue2");
        options.put("onlyCommand", "true");
        l.start(options);
        assertTrue(l.command.contains("-customKey"));
        assertTrue(l.command.contains("customValue"));
        assertTrue(l.command.contains("-customKey2"));
        assertTrue(l.command.contains("customValue2"));
    }


    @Test
    public void testCustomBoolArguments() throws Exception {
        options.put("customKey1", "true");
        options.put("customKey2", "true");
        options.put("onlyCommand", "true");
        l.start(options);
        assertTrue(l.command.contains("-customKey1"));
        assertTrue(l.command.contains("-customKey2"));
    }

    @After
    public void tearDown() throws Exception {
        l.stop();
    }
}
