import org.openqa.selenium.By;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import java.net.URL;

import com.browserstack.local.BrowserStackTunnel;
import com.browserstack.local.BrowserStackTunnel.BrowserStackLocalListener;
import com.browserstack.local.BrowserStackTunnelOptions;

public class JavaSample {

  public static void main(String[] args) throws Exception {
    Sample sample = new Sample();
    sample.startExample();
  }
}
class Sample extends BrowserStackLocalListener {
  public static final String USERNAME = "BROWSERSTACK_USERNAME";
  public static final String AUTOMATE_KEY = "BROWSERSTACK_ACCESS_KEY";
  public static final String URL = "http://" + USERNAME + ":" + AUTOMATE_KEY + "@hub.browserstack.com/wd/hub";
  public static final String identifier = "TEST_IDENTIFIER";

  BrowserStackTunnel tunnel;
  WebDriver driver = null;

  public void startExample() {
    try {
      this.tunnel = new BrowserStackTunnel(this.AUTOMATE_KEY);
    } catch(Exception e) {
      System.out.println(e);
    }
    this.tunnel.verboseMode();
    
    BrowserStackTunnelOptions options = new BrowserStackTunnelOptions();
    
    options.add("localIdentifier", this.identifier);
    options.add("onlyAutomate", true);
    options.add("forcelocal", true);
    options.add("verbose", true);

    try {
      this.tunnel.start(this, options);
    } catch(Exception e) {
      System.out.println(e);
    }

    try {
      Thread.sleep(15000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public void onDisconnect() {
    if(this.driver != null) {
      driver.quit();
    }
    System.exit(0);
  }

  public void onConnect() {
    DesiredCapabilities caps = new DesiredCapabilities();
    caps.setCapability("browser", "Chrome");
    caps.setCapability("browser_version", "44.0");
    caps.setCapability("os", "OS X");
    caps.setCapability("os_version", "El Capitan");
    caps.setCapability("browserstack.debug", "true");
    caps.setCapability("browserstack.localIdentifier", this.identifier);
    caps.setCapability("browserstack.local", "true");
    caps.setCapability("build", "Java Sample Local test");

    try {
      this.driver = new RemoteWebDriver(new URL(URL), caps);
    } catch(Exception e) {
      System.out.println("Exception e: " + e);
    }
    this.driver.get("http://localhost:5000");

    System.out.println(this.driver.getTitle());
    System.out.println("Test complete. Trying to quit driver.");
    this.driver.quit();
    this.tunnel.stop();
    try {
      Thread.sleep(3000);
    } catch (InterruptedException e) {
    }
  }

  public void onError(String errorMessage) {
    if(!errorMessage.trim().toLowerCase().equals("stream closed")) {
      System.out.println("Local error " + errorMessage);
    }
    if(this.driver != null) {
      driver.quit();
    }
    System.exit(0);
  }
}
