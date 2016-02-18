package com.browserstack.local;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.lang.StringBuilder;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.ConsoleHandler;

import com.browserstack.local.BrowserStackLocal;
import com.browserstack.local.BrowserStackTunnelOptions;

public class BrowserStackTunnel {
  protected static final Logger logger = Logger.getLogger("BrowserStackTunnel");
  protected static ConsoleHandler handler;
  private static final String CURRENT_DIRECTORY = System.getProperty("user.dir");
  private static final String DEFAULT_BINARY_DIRECTORY = "bin";
  private static final String BINARY_NAME = "BrowserStackLocal";
  public static final String LINE_SEPERATOR = System.getProperty("line.separator");
  
  private static final HashMap<String, String> valueCommands = new HashMap<String, String>();
  private static final HashMap<String, String> booleanCommands = new HashMap<String, String>();
  
  static {
    valueCommands.put("localidentifier", "-localIdentifier");
    valueCommands.put("hosts", "");
    valueCommands.put("proxyhost", "-proxyHost");
    valueCommands.put("proxyport", "-proxyPort");
    valueCommands.put("proxyuser", "-proxyUser");
    valueCommands.put("proxypass", "-proxyPass");
    
    booleanCommands.put("verbose", "-v");
    booleanCommands.put("forcelocal", "-forcelocal");
    booleanCommands.put("onlyautomate", "-onlyAutomate");
  }
  
  private String browserStackKey;
  protected final File binaryPath;
  private BrowserStackLocal browserStackLocal;
  
  private StringBuilder argumentString;
  
  public BrowserStackTunnel() throws BrowserStackTunnelException {
    this(System.getenv("BROWSERSTACK_ACCESS_KEY"), new File(CURRENT_DIRECTORY, DEFAULT_BINARY_DIRECTORY));
  }
  
  public BrowserStackTunnel(String key) throws BrowserStackTunnelException {
    this(key, new File(CURRENT_DIRECTORY, DEFAULT_BINARY_DIRECTORY));
  }
  
  public void verboseMode() {
    this.logger.setLevel(Level.ALL);
    this.handler.setLevel(Level.ALL);
  }
  
  public BrowserStackTunnel(String key, File binaryPath) throws BrowserStackTunnelException {
    if (!binaryPath.exists()) {
      try {
        binaryPath.mkdir();
      } catch (Exception e) {
        throw new BrowserStackTunnelException("The binary Path " + binaryPath.getPath()
              + " does not exists and cannot be created. Please check file permissions", e);
      }
    }
    this.browserStackKey = key;
    this.binaryPath = binaryPath;
    this.argumentString = new StringBuilder();
    this.handler = new ConsoleHandler();
    this.logger.addHandler(handler);
  }
    
  public void start(BrowserStackLocalListener listener, BrowserStackTunnelOptions options)
       throws BrowserStackTunnelException {
    for (Map.Entry<String, Object> entry : options.entrySet()) {
      String key = entry.getKey().toLowerCase();
      String value = String.valueOf(entry.getValue());
      if (key.equals("accesskey") || key.equals("key")) {
        this.browserStackKey = value;
      } else if (value.equals("true") || value.equals("false")) {
        addArgs(key, Boolean.valueOf(value));
      } else {
        addArgs(key, value);
      }
    }
    this.start(listener);
  }
  
  public void start(BrowserStackLocalListener listener) throws BrowserStackTunnelException {
    Platform currentPlatform = getPlatForm();
    logger.info("Platform detected to be " + currentPlatform);
    
    File zipFile = new File(this.binaryPath, BINARY_NAME + ".zip");
    File binaryFile = new File(this.binaryPath, BINARY_NAME + currentPlatform.getExtension());
    
    if (!binaryFile.exists()) {
      logger.info("Binary File not found.");
      binaryFile = downloadBinary(currentPlatform, zipFile, binaryFile);
    }
    
    this.browserStackLocal = new BrowserStackLocal(binaryFile,
    	this.browserStackKey + " " + this.argumentString.toString());
    this.browserStackLocal.runSync(listener);
  }
  
  public void stop() {
    this.browserStackLocal.kill();
  }
  
  public boolean isRunning() {
    return browserStackLocal.isConnected();
  }

  private void addArgs(String key, String value) {
    key = key.trim().toLowerCase();
    String result = valueCommands.get(key);
    
    if (result != null) {
      this.argumentString.append(result + " " + value + " ");
    }
    result = booleanCommands.get(key);
    if (result != null) {
      if (value.trim().toLowerCase() == "true") {
        this.argumentString.append(result + " ");
      }
    }
  }
  
  private void addArgs(String key, boolean value) {
    key = key.trim().toLowerCase();
    String result = booleanCommands.get(key);
    
    if (result != null) {
      if (value == true) {
    	  this.argumentString.append(result + " ");
      }
    }
  }

  protected File downloadBinary(Platform currentPlatform, File zipFile, File targetFile)
       throws BrowserStackTunnelException {
    BufferedInputStream in;
    BufferedOutputStream out;
    ZipInputStream zipInputStream;
    ZipEntry zipEntry;
    FileOutputStream outputStream;
    
    byte data[] = new byte[1024];
    int count;
    
    logger.info("Downloading zip binary");
    try {
      in = new BufferedInputStream(new URL(currentPlatform.getDownloadUrl()).openStream());
    } catch (MalformedURLException e) {
        throw new BrowserStackTunnelException("The URL to download the binary might have changed.", e);
    } catch (IOException e) {
        throw new BrowserStackTunnelException(
    	  "Cannot reach browserstack.com. Please check your internet connection.", e);
    }
    try {
      out = new BufferedOutputStream(new FileOutputStream(zipFile));
    } catch (FileNotFoundException e) {
        throw new BrowserStackTunnelException("The BrowserStackLocal zip file cannot be downloaded to "
    	    + zipFile.getPath() + ". Please check file permissions.", e);
    }
    try {
      while ((count = in.read(data, 0, 1024)) != -1) {
        out.write(data, 0, count);
      }
    
      in.close();
      out.close();
    } catch (IOException e) {
        throw new BrowserStackTunnelException("IOException while reading or writing to the zip file.", e);
    }
    
    logger.info("Extracting zip file");
    try {
      zipInputStream = new ZipInputStream(new FileInputStream(zipFile));
    } catch (FileNotFoundException e) {
        throw new BrowserStackTunnelException(
    	    "Cannot access the downloaded zip file. The downloaded file might be corrupt.", e);
    }
    try {
      zipEntry = zipInputStream.getNextEntry();
      while (zipEntry != null && !zipEntry.getName().toLowerCase().contains(targetFile.getName().toLowerCase())) {
        zipEntry = zipInputStream.getNextEntry();
      }
    } catch (IOException e) {
        throw new BrowserStackTunnelException("Zip extraction failed. The downloaded file might be corrupt.", e);
    }
    
    data = new byte[1024];
    
    try {
      outputStream = new FileOutputStream(targetFile);
    
      int len;
      while ((len = zipInputStream.read(data)) > 0) {
        outputStream.write(data, 0, len);
      }
      outputStream.close();
    
      zipInputStream.closeEntry();
      zipInputStream.close();
    } catch (IOException e) {
        throw new BrowserStackTunnelException("Cannot create or write to " + targetFile.getAbsoluteFile()
    	    + ". Please check the folder permissions.", e);
    }
    
    logger.info("BrowserStackLocal binary downloaded");
    zipFile.delete();
    
    targetFile.setExecutable(true);
    return targetFile;
  }
    
  private Platform getPlatForm() {
    String systemProperty = System.getProperty("os.name").trim().toLowerCase();
    if (systemProperty.startsWith("window")) {
      return Platform.WINDOWS;
    } else if (systemProperty.startsWith("mac")) {
      return Platform.MAC;
    } else {
      if (System.getProperty("os.arch").trim().toLowerCase().contains("x86")) {
        return Platform.LINUX_32;
      }
    }
    return Platform.LINUX_64;
  }
  
  protected enum Platform {
    WINDOWS("win32", ".exe"), 
    LINUX_32("linux-ia32", ""), 
    LINUX_64("linux-x64", ""),
    MAC("darwin-x64", "");
  
    private String downloadUrl = "https://www.browserstack.com/browserstack-local/BrowserStackLocal-";
    private String extension;
  
    Platform(String descriptor, String extension) {
      this.downloadUrl += descriptor + ".zip";
      this.extension = extension;
    }
  
    public String getDownloadUrl() {
      return this.downloadUrl;
    }
  
    public String getExtension() {
      return this.extension;
    }
  }
  
  public static abstract class BrowserStackLocalListener {
    protected String lastError;
    
    public void onTunnelStateChange(TunnelState state) {
      if (state != TunnelState.ERROR) {
        lastError = null;
      }
    
      switch (state) {
        case CONNECTING:
    	    logger.info("Connecting Local");
    	    onConnecting();
    	    break;

        case CONNECTED:
          logger.info("Local Connected");
          onConnect();
    	    break;
    
        case DISCONNECTED:
    	    logger.info("Local Disconnected");
    	    onDisconnect();
    	    break;
    
        case ERROR:
    	    logger.info("Error on Local connection");
    	    onError(lastError);
    	    break;
      }
    }
    
    public void onConnecting() {
    }
    
    public abstract void onError(String error);
    
    public abstract void onConnect();
    
    public abstract void onDisconnect();
  }
  
  public enum TunnelState {
    IDLE, 
    CONNECTING, 
    CONNECTED, 
    ERROR, 
    DISCONNECTED
  }
  
  class BrowserStackTunnelException extends Exception {
    private Exception originalException;
  
    public BrowserStackTunnelException() {
    }
  
    public BrowserStackTunnelException(String message, Exception originalException) {
      super(message);
      this.originalException = originalException;
    }
  
    public String toString() {
      StringWriter stringWriter = new StringWriter();
      this.originalException.printStackTrace(new PrintWriter(stringWriter));

      return super.toString() + BrowserStackTunnel.LINE_SEPERATOR + "Original Error: " + stringWriter.toString();
   }
  }

}
