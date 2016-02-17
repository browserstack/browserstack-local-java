package sample;

import org.openqa.selenium.By;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import java.net.URL;

import com.browserstack.local.BrowserStackTunnel;
import com.browserstack.local.BrowserStackTunnel.BrowserStackLocalListener;

public class JavaSample {

  public static void main(String[] args) throws Exception {
    Sample sample = new Sample();
    sample.startExample();
  }
}
class Sample extends BrowserStackLocalListener {
  public static final String USERNAME = System.getenv("BROWSERSTACK_USERNAME");
  public static final String AUTOMATE_KEY = System.getenv("BROWSERSTACK_ACCESS_KEY");
  public static final String URL = "http://" + USERNAME + ":" + AUTOMATE_KEY + "@hub.browserstack.com/wd/hub";
  public static final String identifier = "qwerqwerqwer";

  BrowserStackTunnel tunnel;
  WebDriver driver = null;

  public void startExample() {
    try {
      this.tunnel = new BrowserStackTunnel();
    } catch(Exception e) {
      System.out.println(e);
    }
    this.tunnel.verboseMode();
    this.tunnel.addArgs("localIdentifier", this.identifier);
    this.tunnel.addArgs("qweqwe", "wer");
    this.tunnel.addArgs("onlyAutomate", "true");
    this.tunnel.addArgs("forcelocal", true);
    //this.tunnel.addArgs("hosts", "localhost,3000,0");
    this.tunnel.addArgs("verbose", true);

    try {
      this.tunnel.start(this);
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
    caps.setCapability("build", "build");

    try {
      this.driver = new RemoteWebDriver(new URL(URL), caps);
    } catch(Exception e) {
      System.out.println("Exception e: " + e);
    }
    this.driver.get("http://localhost:3000");

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
