package com.browserstack.local;

import java.io.File;
import java.util.logging.Level;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.anyObject;
import static org.mockito.AdditionalMatchers.and;
import static org.powermock.api.mockito.PowerMockito.*;

import com.browserstack.local.BrowserStackLocal;
import com.browserstack.local.BrowserStackTunnel;
import com.browserstack.local.BrowserStackTunnel.Platform;
import com.browserstack.local.BrowserStackTunnel.BrowserStackLocalListener;
import com.browserstack.local.BrowserStackTunnel.BrowserStackTunnelException;

import org.mockito.Matchers;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.core.classloader.annotations.PrepareForTest;

@RunWith(PowerMockRunner.class)
@PrepareForTest(BrowserStackTunnel.class)
public class BrowserStackTunnelTest extends TestCase {
  class TestListener extends BrowserStackLocalListener {
    public void onDisconnect() {
    }
  
    public void onConnect() {
    }
  
    public void onError(String errorMessage) {
    }
  
    public void onConnecting() {
    }
  }
  
  private BrowserStackTunnel tunnel;
  private final String browserStackTestKey = "qwertyuiop";
  
  private BrowserStackLocal localMock = mock(BrowserStackLocal.class);
  private final File zipFileMock = mock(File.class);
  private final File binaryFileMock = mock(File.class);
  private TestListener listener = new TestListener();
  
  public BrowserStackTunnelTest(String testName) {
    super(testName);
  }
  
  public static Test suite() {
    return new TestSuite(BrowserStackTunnelTest.class);
  }
  
  private BrowserStackTunnel createTestTunnel() throws Exception {
    return new BrowserStackTunnel(browserStackTestKey) {
        protected File downloadBinary(Platform currentPlatform, File zipFile, File targetFile) {
        assertTrue(zipFileMock == zipFile);
        assertTrue(binaryFileMock == targetFile);
        assertTrue(currentPlatform == Platform.MAC);
        return binaryFileMock;
      }
  
      protected Platform getPlatform() {
        return Platform.MAC;
      }
    };
  }
  
  public void testTunnel() throws Exception {
    BrowserStackTunnel testTunnel = this.createTestTunnel();
    testTunnel.logger.setLevel(Level.SEVERE);
    testTunnel.handler.setLevel(Level.SEVERE);
  
    whenNew(BrowserStackLocal.class).withArguments(anyObject(), Matchers.contains(browserStackTestKey))
    	.thenReturn(this.localMock);
    whenNew(File.class).withArguments(testTunnel.binaryPath, "BrowserStackLocal.zip").thenReturn(this.zipFileMock);
    whenNew(File.class).withArguments(testTunnel.binaryPath, "BrowserStackLocal").thenReturn(this.binaryFileMock);
  
    doReturn(false).when(this.binaryFileMock).exists();
  
    // Start and stop the tunnel
    testTunnel.start(this.listener);
    testTunnel.stop();
  
    // Runs the binary
    verify(this.localMock, times(1)).runSync(this.listener);
    // Kills the tunnel
    verify(this.localMock, times(1)).kill();
  }
  
  public void testUsesEnvironmentVariable() throws Exception {
    BrowserStackTunnel testTunnel = new BrowserStackTunnel() {
      protected File downloadBinary(Platform currentPlatform, File zipFile, File targetFile) {
    	  assertTrue(zipFileMock == zipFile);
    	  assertTrue(binaryFileMock == targetFile);
    	  assertTrue(currentPlatform == Platform.MAC);
    	  return binaryFileMock;
      }
  
      protected Platform getPlatform() {
    	  return Platform.MAC;
      }
    };
  
    whenNew(BrowserStackLocal.class)
    	.withArguments(anyObject(), Matchers.contains(System.getenv("BROWSERSTACK_ACCESS_KEY")))
    	.thenReturn(this.localMock);
    whenNew(File.class).withArguments(testTunnel.binaryPath, "BrowserStackLocal.zip").thenReturn(this.zipFileMock);
    whenNew(File.class).withArguments(testTunnel.binaryPath, "BrowserStackLocal").thenReturn(this.binaryFileMock);
  
    doReturn(false).when(this.binaryFileMock).exists();
  
    // Start and stop the tunnel
    testTunnel.start(this.listener);
    testTunnel.stop();
  
    // Runs the binary
    verify(this.localMock, times(1)).runSync(this.listener);
    // Kills the tunnel
    verify(this.localMock, times(1)).kill();
  }
  
  public void testTunnelWithOptions() throws Exception {
    BrowserStackTunnel testTunnel = this.createTestTunnel();
  
    whenNew(BrowserStackLocal.class)
    	.withArguments(anyObject(), and(Matchers.contains("-forcelocal"), Matchers.contains("-v")))
    	.thenReturn(this.localMock);
    whenNew(File.class).withArguments(testTunnel.binaryPath, "BrowserStackLocal.zip").thenReturn(this.zipFileMock);
    whenNew(File.class).withArguments(testTunnel.binaryPath, "BrowserStackLocal").thenReturn(this.binaryFileMock);
  
    doReturn(false).when(this.binaryFileMock).exists();
  
    // Add this flag as boolean
    BrowserStackTunnelOptions options = new BrowserStackTunnelOptions();
    options.add("verbose", true);
    options.add("forceLocal", "true");
  
    // Start and stop the tunnel
    testTunnel.start(this.listener, options);
    testTunnel.stop();
  
    // Runs the binary
    verify(this.localMock, times(1)).runSync(this.listener);
    // Kills the tunnel
    verify(this.localMock, times(1)).kill();
  }

  public void testIsRunningFunction() throws Exception {
    final BrowserStackTunnel testTunnel = this.createTestTunnel();

    class NewTestListener extends TestListener {
      public void onConnect() {
        assertEquals(testTunnel.isRunning(), true);
      }
    }

    NewTestListener newListener = new NewTestListener();

    whenNew(BrowserStackLocal.class)
    	.withArguments(anyObject(), Matchers.contains(browserStackTestKey)).thenReturn(this.localMock);
    whenNew(File.class).withArguments(testTunnel.binaryPath, "BrowserStackLocal.zip").thenReturn(this.zipFileMock);
    whenNew(File.class).withArguments(testTunnel.binaryPath, "BrowserStackLocal").thenReturn(this.binaryFileMock);
  
    doReturn(false).when(this.binaryFileMock).exists();
    
    testTunnel.start(newListener);
    this.localMock.runSync(newListener);
    testTunnel.stop();
  
    assertEquals(testTunnel.isRunning(), false);
    this.localMock.kill();

  }
  
  public void testAllBooleanParams() throws Exception {
    BrowserStackTunnel testTunnel = this.createTestTunnel();
  
    whenNew(BrowserStackLocal.class)
    	.withArguments(anyObject(), and(Matchers.contains("-forcelocal"), Matchers.contains("-v")))
    	.thenReturn(this.localMock);
    whenNew(File.class).withArguments(testTunnel.binaryPath, "BrowserStackLocal.zip").thenReturn(this.zipFileMock);
    whenNew(File.class).withArguments(testTunnel.binaryPath, "BrowserStackLocal").thenReturn(this.binaryFileMock);
  
    doReturn(false).when(this.binaryFileMock).exists();
  
    // Add this flag as boolean
    BrowserStackTunnelOptions options = new BrowserStackTunnelOptions();
    options.add("verbose", true);
    options.add("forceLocal", "true");
    options.add("onlyAutomate", false);
  
    // Start and stop the tunnel
    testTunnel.start(this.listener, options);
    testTunnel.stop();
  
    // Runs the binary
    verify(localMock, times(1)).runSync(this.listener);
    // Kills the tunnel
    verify(localMock, times(1)).kill();
  }
  
  public void testAllKeyValueParams() throws Exception {
    final String testTunnelIdentifier = "qweasdzxc";
    final String testHosts = "qwadxc";
    final String testProxyHost = "qeadx";
    final String testProxyPort = "weszxc";
    final String testProxyUser = "eszxc";
    final String testProxyPass = "qwsdzxc";
  
    BrowserStackTunnel testTunnel = this.createTestTunnel();
  
    whenNew(BrowserStackLocal.class)
    	.withArguments(anyObject(),
          and(Matchers.contains("-localIdentifier " + testTunnelIdentifier),
    			and(Matchers.contains(testHosts),
    			and(Matchers.contains("-proxyHost " + testProxyHost),
    			and(Matchers.contains("-proxyPort " + testProxyPort),
    			and(Matchers.contains("-proxyUser " + testProxyUser),
    			Matchers.contains("-proxyPass " + testProxyPass)))))))
    	.thenReturn(this.localMock);
    whenNew(File.class).withArguments(testTunnel.binaryPath, "BrowserStackLocal.zip").thenReturn(this.zipFileMock);
    whenNew(File.class).withArguments(testTunnel.binaryPath, "BrowserStackLocal").thenReturn(this.binaryFileMock);
  
    doReturn(false).when(this.binaryFileMock).exists();
  
    BrowserStackTunnelOptions options = new BrowserStackTunnelOptions();
  
    options.add("localIdentifier", testTunnelIdentifier);
    options.add("hosts", testHosts);
    options.add("proxyhost", testProxyHost);
    options.add("proxyport", testProxyPort);
    options.add("proxyuser", testProxyUser);
    options.add("proxypass", testProxyPass);
  
    // Start and stop the tunnel
    testTunnel.start(this.listener, options);
    testTunnel.stop();
  
    // Runs the binary
    verify(this.localMock, times(1)).runSync(listener);
    // Kills the tunnel
    verify(this.localMock, times(1)).kill();
  }
  
  public void testTunnelWithUnknownBinaryOptions() throws Exception {
    final String randomKeyWithStringValue = "randomKeyString";
    final String randomStringValue = "randomValueString";
    final String randomKeyWithBooleanValue = "randomKeyWithBooleanValue";
    final boolean randomBooleanValue = false;
  
    BrowserStackTunnel testTunnel = this.createTestTunnel();
  
    whenNew(BrowserStackLocal.class)
    	.withArguments(anyObject(),
    		and(Matchers.contains(randomKeyWithBooleanValue),
    		and(Matchers.contains(randomKeyWithStringValue), Matchers.contains(randomStringValue))))
    	.thenReturn(this.localMock);
    whenNew(File.class).withArguments(testTunnel.binaryPath, "BrowserStackLocal.zip").thenReturn(this.zipFileMock);
    whenNew(File.class).withArguments(testTunnel.binaryPath, "BrowserStackLocal").thenReturn(this.binaryFileMock);
  
    BrowserStackTunnelOptions options = new BrowserStackTunnelOptions();
  
    options.add(randomKeyWithStringValue, randomStringValue);
    options.add(randomKeyWithBooleanValue, randomBooleanValue);
  
    try {
      testTunnel.start(this.listener, options);
      testTunnel.stop();
    } catch (Exception e) {}
  
    verify(this.localMock, null);
  }
  
  public void testDownloadURLs() throws Exception {
    assertTrue(Platform.MAC.getDownloadUrl()
    	.equals("https://www.browserstack.com/browserstack-local/BrowserStackLocal-darwin-x64.zip"));
    assertTrue(Platform.LINUX_32.getDownloadUrl()
    	.equals("https://www.browserstack.com/browserstack-local/BrowserStackLocal-linux-ia32.zip"));
    assertTrue(Platform.LINUX_64.getDownloadUrl()
    	.equals("https://www.browserstack.com/browserstack-local/BrowserStackLocal-linux-x64.zip"));
    assertTrue(Platform.WINDOWS.getDownloadUrl()
    	.equals("https://www.browserstack.com/browserstack-local/BrowserStackLocal-win32.zip"));
  }
  
  public void testExtensions() throws Exception {
    assertTrue(Platform.MAC.getExtension().equals(""));
    assertTrue(Platform.LINUX_32.getExtension().equals(""));
    assertTrue(Platform.LINUX_64.getExtension().equals(""));
    assertTrue(Platform.WINDOWS.getExtension().equals(".exe"));
  }

}
