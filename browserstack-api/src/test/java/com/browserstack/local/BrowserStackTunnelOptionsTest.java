package com.browserstack.local;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import com.browserstack.local.BrowserStackTunnelOptions;

public class BrowserStackTunnelOptionsTest extends TestCase {
  private BrowserStackTunnelOptions testOptions;
  private HashMap<String, Object> sampleOptions;
  private String keyWithStringValue;
  private String keyWithBooleanValue;
  private String stringValue;
  private boolean booleanValue;
  
  protected void setUp() {
    sampleOptions = new HashMap<String, Object>();

    keyWithStringValue = "keyWithStringValue";
    keyWithBooleanValue = "keyWithBooleanValue";
    stringValue = "stringValue";
    booleanValue = true;

    sampleOptions.put(keyWithStringValue, stringValue);
    sampleOptions.put(keyWithBooleanValue, booleanValue);
  }
  
  public void testEntrySetFunction() {
    testOptions = new BrowserStackTunnelOptions();
    
    assertEquals(testOptions.entrySet().isEmpty(), true);
    
    testOptions.add(keyWithStringValue, stringValue);
    testOptions.add(keyWithBooleanValue, booleanValue);
    assertEquals(testOptions.entrySet().
        containsAll(sampleOptions.entrySet()), true);
  }
  
  public void testAddFunction() {
    testOptions = new BrowserStackTunnelOptions();
    
    assertEquals(testOptions.entrySet().
        containsAll(this.sampleOptions.entrySet()), false);
    
    testOptions.add(keyWithStringValue, stringValue);
    testOptions.add(keyWithBooleanValue, booleanValue);
    
    assertEquals(testOptions.entrySet().
        containsAll(sampleOptions.entrySet()), true);
  }
  
  public void testConstructorWithOptions() {
    testOptions = new BrowserStackTunnelOptions(sampleOptions);
    final String newKey = "newKey";
    final String newValue = "newValue";
    
    assertEquals(testOptions.entrySet().
        containsAll(this.sampleOptions.entrySet()), true);
    
    sampleOptions.put(newKey, newValue);
    testOptions.add(newKey, newValue);
    
    assertEquals(testOptions.entrySet().
        containsAll(this.sampleOptions.entrySet()), true);
  }
}
