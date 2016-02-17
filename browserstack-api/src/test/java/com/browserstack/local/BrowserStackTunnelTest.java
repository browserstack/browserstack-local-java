package com.browserstack.local;

import java.io.File;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.anyObject;
import static org.powermock.api.mockito.PowerMockito.*;

import com.browserstack.local.BrowserStackLocal;
import com.browserstack.local.BrowserStackTunnel;
import com.browserstack.local.BrowserStackTunnel.Platform;
import com.browserstack.local.BrowserStackTunnel.BrowserStackLocalListener;
import com.browserstack.local.BrowserStackTunnel.BrowserStackTunnelException;

import org.mockito.Matchers;
import java.util.logging.Level;
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
  public BrowserStackTunnelTest(String testName) {
    super(testName);
  }
  public static Test suite() {
    return new TestSuite(BrowserStackTunnelTest.class);
  }

  public void testTunnel() throws Exception {
    BrowserStackLocal localMock = mock(BrowserStackLocal.class);
    final String browserStackKey = "qwertyuiop";
    final String tunnelIdentifier = "qweasdzxc";

    final File zipFileMock = mock(File.class);
    final File binaryFileMock = mock(File.class);
    TestListener listener = new TestListener();

    this.tunnel = new BrowserStackTunnel(browserStackKey) {
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
    this.tunnel.logger.setLevel(Level.SEVERE);
    this.tunnel.handler.setLevel(Level.SEVERE);

    whenNew(BrowserStackLocal.class).withArguments(anyObject(), Matchers.contains(browserStackKey + " -v -forcelocal -localIdentifier " + tunnelIdentifier)).thenReturn(localMock);
    whenNew(File.class).withArguments(tunnel.binaryPath, "BrowserStackLocal.zip").thenReturn(zipFileMock);
    whenNew(File.class).withArguments(tunnel.binaryPath, "BrowserStackLocal").thenReturn(binaryFileMock);

    doReturn(false).when(binaryFileMock).exists();

    // Must Ignore this key
    this.tunnel.addArgs("key", "value");
    // Add this flag as boolean
    this.tunnel.addArgs("verbose", true);
    // Add this args with boolean string
    this.tunnel.addArgs("forceLocal", "true");
    // Add this args as key value
    this.tunnel.addArgs("localIdentifier", tunnelIdentifier);

    // Start and stop the tunnel
    this.tunnel.start(listener);
    this.tunnel.stop();

    // Runs the binary
    verify(localMock, times(1)).run(listener);
    // Kills the tunnel
    verify(localMock, times(1)).kill();
  }

  public void testUsesEnvironmentVariable() throws Exception {
    BrowserStackLocal localMock = mock(BrowserStackLocal.class);
    final String tunnelIdentifier = "qweasdzxc";

    final File zipFileMock = mock(File.class);
    final File binaryFileMock = mock(File.class);
    TestListener listener = new TestListener();

    this.tunnel = new BrowserStackTunnel() {
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

    whenNew(BrowserStackLocal.class).withArguments(anyObject(), Matchers.contains(System.getenv("BROWSERSTACK_ACCESS_KEY") + " -v -forcelocal -localIdentifier " + tunnelIdentifier)).thenReturn(localMock);
    whenNew(File.class).withArguments(tunnel.binaryPath, "BrowserStackLocal.zip").thenReturn(zipFileMock);
    whenNew(File.class).withArguments(tunnel.binaryPath, "BrowserStackLocal").thenReturn(binaryFileMock);

    doReturn(false).when(binaryFileMock).exists();

    // Must Ignore this key
    this.tunnel.addArgs("key", "value");
    // Add this flag as boolean
    this.tunnel.addArgs("verbose", true);
    // Add this args with boolean string
    this.tunnel.addArgs("forceLocal", "true");
    // Add this args as key value
    this.tunnel.addArgs("localIdentifier", tunnelIdentifier);

    // Start and stop the tunnel
    this.tunnel.start(listener);
    this.tunnel.stop();

    // Runs the binary
    verify(localMock, times(1)).run(listener);
    // Kills the tunnel
    verify(localMock, times(1)).kill();
  }

  public void testAllBooleanParams() throws Exception {
    BrowserStackLocal localMock = mock(BrowserStackLocal.class);
    final String browserStackKey = "qwertyuiop";
    final String tunnelIdentifier = "qweasdzxc";

    final File zipFileMock = mock(File.class);
    final File binaryFileMock = mock(File.class);
    TestListener listener = new TestListener();

    this.tunnel = new BrowserStackTunnel(browserStackKey) {
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

    whenNew(BrowserStackLocal.class).withArguments(anyObject(), Matchers.contains(browserStackKey + " -v -forcelocal")).thenReturn(localMock);
    whenNew(File.class).withArguments(tunnel.binaryPath, "BrowserStackLocal.zip").thenReturn(zipFileMock);
    whenNew(File.class).withArguments(tunnel.binaryPath, "BrowserStackLocal").thenReturn(binaryFileMock);

    doReturn(false).when(binaryFileMock).exists();

    // Add this flag as boolean
    this.tunnel.addArgs("verbose", true);
    this.tunnel.addArgs("forceLocal", "true");
    this.tunnel.addArgs("onlyAutomate", false);

    // Start and stop the tunnel
    this.tunnel.start(listener);
    this.tunnel.stop();

    // Runs the binary
    verify(localMock, times(1)).run(listener);
    // Kills the tunnel
    verify(localMock, times(1)).kill();
  }

  public void testAllKeyValueParams() throws Exception {
    BrowserStackLocal localMock = mock(BrowserStackLocal.class);
    final String browserStackKey = "qwertyuiop";
    final String tunnelIdentifier = "qweasdzxc";
    final String hosts = "qwadxc";
    final String proxyhost = "qeadx";
    final String proxyport = "weszxc";
    final String proxyuser = "eszxc";
    final String proxypass = "qwsdzxc";

    final File zipFileMock = mock(File.class);
    final File binaryFileMock = mock(File.class);
    TestListener listener = new TestListener();

    this.tunnel = new BrowserStackTunnel(browserStackKey) {
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

    whenNew(BrowserStackLocal.class).withArguments(anyObject(), Matchers.contains(
          browserStackKey + " -localIdentifier " + tunnelIdentifier + "  " + hosts +
          " -proxyHost " + proxyhost + " -proxyPort " + proxyport + " -proxyUser " +
          proxyuser + " -proxyPass " + proxypass)).thenReturn(localMock);
    whenNew(File.class).withArguments(tunnel.binaryPath, "BrowserStackLocal.zip").thenReturn(zipFileMock);
    whenNew(File.class).withArguments(tunnel.binaryPath, "BrowserStackLocal").thenReturn(binaryFileMock);

    doReturn(false).when(binaryFileMock).exists();

    // Add this flag as boolean
    this.tunnel.addArgs("localIdentifier", tunnelIdentifier);
    this.tunnel.addArgs("hosts", hosts);
    this.tunnel.addArgs("proxyhost", proxyhost);
    this.tunnel.addArgs("proxyport", proxyport);
    this.tunnel.addArgs("proxyuser", proxyuser);
    this.tunnel.addArgs("proxypass", proxypass);

    // Start and stop the tunnel
    this.tunnel.start(listener);
    this.tunnel.stop();

    // Runs the binary
    verify(localMock, times(1)).run(listener);
    // Kills the tunnel
    verify(localMock, times(1)).kill();
  }

  public void testDownloadURLs() throws Exception {
    assertTrue(Platform.MAC.getDownloadUrl().equals("https://www.browserstack.com/browserstack-local/BrowserStackLocal-darwin-x64.zip"));
    assertTrue(Platform.LINUX_32.getDownloadUrl().equals("https://www.browserstack.com/browserstack-local/BrowserStackLocal-linux-ia32.zip"));
    assertTrue(Platform.LINUX_64.getDownloadUrl().equals("https://www.browserstack.com/browserstack-local/BrowserStackLocal-linux-x64.zip"));
    assertTrue(Platform.WINDOWS.getDownloadUrl().equals("https://www.browserstack.com/browserstack-local/BrowserStackLocal-win32.zip"));
  }

  public void testExtensions() throws Exception {
    assertTrue(Platform.MAC.getExtension().equals(""));
    assertTrue(Platform.LINUX_32.getExtension().equals(""));
    assertTrue(Platform.LINUX_64.getExtension().equals(""));
    assertTrue(Platform.WINDOWS.getExtension().equals(".exe"));
  }
}
