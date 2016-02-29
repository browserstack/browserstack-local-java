package com.browserstack.local;

import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.browserstack.local.BrowserStackTunnel.TunnelState;
import com.browserstack.local.BrowserStackTunnel.BrowserStackLocalListener;

public class BrowserStackLocal {
  private static Logger logger;
  private static final int MAX_CONNECT_WAIT = 30000; // 30s x 2 = 60s
  private static final int MAX_CONNECT_ATTEMPTS = 2;
  
  private final File binaryFile;
  private final String argumentString;
  
  private Process process;
  private Thread processThread;
  
  private StringBuffer output;
  private String lastError;
  private TunnelState tunnelState;
  
  private BrowserStackLocalListener listener;
  
  private static final Object monitor = new Object();
  private static final Map<Pattern, TunnelState> stateMatchers = new HashMap<Pattern, TunnelState>();

  static {
    stateMatchers.put(Pattern.compile("Press Ctrl-C to exit.*", Pattern.MULTILINE), TunnelState.CONNECTED);
    stateMatchers.put(Pattern.compile("\\s*\\*\\*\\* Error:\\s+(.*)$", Pattern.MULTILINE), TunnelState.ERROR);
  }

  protected BrowserStackLocal(File binaryFile, String argumentString) {
    if (binaryFile == null || argumentString == null || !binaryFile.exists()) {
      throw new IllegalArgumentException("Invalid arguments");
    }

  this.binaryFile = binaryFile;
  this.argumentString = argumentString;
  this.output = new StringBuffer();
  this.tunnelState = TunnelState.IDLE;
  this.logger = BrowserStackTunnel.logger;

  Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
      @Override
      public void run() {
        BrowserStackLocal.this.kill();
      }
    }));
  }

  protected void run() {
    if (process != null) {
      kill();
    }

    processThread = new Thread(new Runnable() {
      @Override
      public void run() {
     	  notifyTunnelStateChanged(TunnelState.CONNECTING);
    
     	  try {
     	    logger.fine("Arguments -- " + argumentString);
     	    process = new ProcessBuilder((binaryFile.getAbsolutePath() + " " + argumentString)
                        .split(" ")).start();
     	    BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
    
     	    String line;
     	    while ((line = br.readLine()) != null) {
     	      output.append(line).append("\n");
    
     	      if (processOutput(output.toString())) {
     	        logger.fine(output.toString());
     	        output.setLength(0);
     	      }
     	    }
     	  } catch (IOException e) {
     	      if (listener != null) {
     	  	    listener.onError(e.getMessage());
     	      }
     	  } finally {
     	      logger.fine(output.toString());
     	      output.setLength(0);
     	      notifyTunnelStateChanged(TunnelState.DISCONNECTED);
     	  }
      }
    });

    processThread.start();
  }

  protected void run(BrowserStackLocalListener listener) {
    setListener(listener);
    run();
  }

  protected void runSync(BrowserStackLocalListener listener) {
    setListener(listener);
    
    if (process != null) {
      kill();
    }
    
    notifyTunnelStateChanged(TunnelState.CONNECTING);
    run();
    
    int connAttempts = 0;
    boolean connFailed = false;
    
    synchronized (monitor) {
      while (tunnelState == TunnelState.CONNECTING) {
        logger.info("Waiting: " + connAttempts);
        try {
          monitor.wait(MAX_CONNECT_WAIT);
        } catch (InterruptedException e) {
          logger.info("Exc: " + e.getMessage() + " " + isConnected());
        }
        
        if (MAX_CONNECT_ATTEMPTS > 0 && ++connAttempts >= MAX_CONNECT_ATTEMPTS) {
          connFailed = true;
          break;
        }
      }
    }
    
    if (connFailed) {
      killWithError("Failed to connect to BrowserStack");
    }
  }

  protected void kill() {
    if (process != null) {
      process.destroy();
      process = null;
    }
    
    if (processThread != null && processThread.isAlive()) {
      processThread.interrupt();
      processThread = null;
    }
    
    logger.fine(output.toString());
    output.setLength(0);
    tunnelState = TunnelState.DISCONNECTED;
  }

  protected void setListener(BrowserStackLocalListener listener) {
    this.listener = listener;
  }

  protected boolean isConnected() {
    return (tunnelState == TunnelState.CONNECTED);
  }

  protected String getLastError() {
    return lastError;
  }

  protected TunnelState getTunnelState() {
    return tunnelState;
  }

  private void killWithError(String message) {
    setError(message);
    notifyTunnelStateChanged(TunnelState.ERROR);
    kill();
  }

  private boolean processOutput(final String output) {
    if (output != null && !output.trim().isEmpty()) {
      String error;
      
      for (Map.Entry<Pattern, TunnelState> entry : stateMatchers.entrySet()) {
        Matcher m = entry.getKey().matcher(output);
        
        if (m.find()) {
          if (entry.getValue() == TunnelState.ERROR) {
            error = (m.groupCount() > 0) ? m.group(1) : output;
          } else {
            error = null;
          }
          
          setError(error);
          notifyTunnelStateChanged(entry.getValue());
          return true;
        }
      }
    }
  
    return false;
  }

  private void notifyTunnelStateChanged(TunnelState state) {
    if (tunnelState != state) {
      if (listener != null) {
        listener.onTunnelStateChange(state);
      }
      
      synchronized (monitor) {
        monitor.notifyAll();
      }
    }
  
    tunnelState = state;
  }

  private void setError(String message) {
    lastError = message;
    
    if (listener != null) {
      listener.lastError = lastError;
    }
  }

}
