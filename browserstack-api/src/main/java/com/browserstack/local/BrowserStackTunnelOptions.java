package com.browserstack.local;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BrowserStackTunnelOptions {
  private final Map<String, Object> arguments = new HashMap<String, Object>();

  public BrowserStackTunnelOptions() {
    // no-args constructor;
  }

  public BrowserStackTunnelOptions(Map<String, Object> options) {
    arguments.putAll(options);
  }

  public void add(String key, Object value) {
    arguments.put(key, value);
  }

  public Set<Map.Entry<String, Object>> entrySet() {
    return arguments.entrySet();
  }

}
