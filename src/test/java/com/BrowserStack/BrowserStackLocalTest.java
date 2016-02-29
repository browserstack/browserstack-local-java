package com.BrowserStack;

import java.net.URL;
import java.util.HashMap;

import org.junit.*;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

public class BrowserStackLocalTest {  
  private WebDriver driver;
  Local l; 

  @Before
  public void setUp() throws Exception {  
    DesiredCapabilities caps = new DesiredCapabilities();
    caps.setCapability("browser", "Firefox");
    caps.setCapability("browser_version", "40.0");
    caps.setCapability("os", "Windows");
    caps.setCapability("os_version", "8.1");
    caps.setCapability("browserstack.local", true);
    
	l = new Local();
	HashMap<String, String> options = new HashMap<String, String>();
	options.put("key", "<automate-key>");
	options.put("only", "localhost,80,0");
	options.put("forcelocal", "");
	options.put("proxyHost", "127.0.0.1");
	options.put("proxyPort", "8118");
	options.put("xyz", "qwerty");
	l.start(options);
    
    driver = new RemoteWebDriver(
      new URL("http://<username>:<automate-key>@hub.browserstack.com/wd/hub"),caps);
  }  

  @Test
  public void testSimple() throws Exception {  
    driver.get("http://localhost");
    System.out.println("Process is running : " + l.isRunning());    
    System.out.println("Page title is: " + driver.getTitle());
  }  

  @After
  public void tearDown() throws Exception {  
      driver.quit();
      l.stop();
  }  
}