package com.browserstack;

import java.net.URL;
import java.util.HashMap;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

public class BrowserStackLocalExample {  
  private WebDriver driver;
  Local l; 

  public static void main() throws Exception {
    String username = System.getenv("BROWSERSTACK_USERNAME");
    String access_key = System.getenv("BROWSERSTACK_ACCESS_KEY");

    DesiredCapabilities caps = new DesiredCapabilities();
    caps.setCapability("browser", "Firefox");
    caps.setCapability("browser_version", "40.0");
    caps.setCapability("os", "Windows");
    caps.setCapability("os_version", "8.1");
    caps.setCapability("browserstack.local", true);

    l = new Local();
    HashMap<String, String> options = new HashMap<String, String>();
    options.put("key", access_key);
    //options.put("only", "localhost,80,0");
    //options.put("forcelocal", "");
    //options.put("proxyHost", "127.0.0.1");
    //options.put("proxyPort", "8118");
    //options.put("xyz", "qwerty");
    l.start(options);

    System.out.println("Starting session");
    driver = new RemoteWebDriver(new URL("http://" + username + ":" + access_key + "@hub.browserstack.com/wd/hub"), caps);
    System.out.println("Started session");
  
    driver.get("http://localhost");
    System.out.println("Process is running : " + l.isRunning());
    System.out.println("Page title is: " + driver.getTitle());

    driver.quit();
    l.stop();
  }
}
