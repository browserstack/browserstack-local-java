package com.BrowserStack;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

class Local {
  
  Process BrowserStackLocal = null;
  List<String> command;
  String logFilePath;
  HashMap<String, String> parameters;
  
  void start(HashMap<String,String> options) throws Exception {
    command = new ArrayList<String>();
    
    if(options.get("binarypath") != null){
      command.add(options.get("binarypath")); 
    }
    else {
      LocalBinary lb = new LocalBinary();
      command.add(lb.binary_path);
    }
      
    logFilePath = options.get("logfile") == null ? (System.getProperty("user.dir") + "/local.log") :  options.get("logfile");
    command.add("-logFile");
    command.add(logFilePath);

    command.add(options.get("key"));
    makeCommand(options);
    
    if (BrowserStackLocal == null){
      ProcessBuilder processBuilder = new ProcessBuilder(command);

      if((new File(logFilePath)).exists()){
        FileWriter f = new FileWriter(logFilePath);
        f.write("");
        f.close();
      }

      BrowserStackLocal = processBuilder.start();
      FileReader f = new FileReader(logFilePath);
      BufferedReader reader = new BufferedReader(f);
      String string;
      int j = 0;
      while (true) {
        string = reader.readLine();
        if(string == null) continue;

        if (string.equalsIgnoreCase("Press Ctrl-C to exit")) {
          f.close();
          break; 
        }
        
        if (string.contains("*** Error")){
          f.close();
          throw new BrowserStackLocalException(string);
        }
      }
      
    }
  }
  
  void stop(){
    if (BrowserStackLocal != null) {
      BrowserStackLocal.destroy();
    }
  }
  
  boolean isRunning(){
    try {
      BrowserStackLocal.exitValue();
        return false;
    }
    catch (Exception e) {
        return true;
    }
  }
  
  void init(){
    parameters = new HashMap<String, String>();
    parameters.put("v","-v"); 
    parameters.put("f","-f"); 
    parameters.put("h","-h");
    parameters.put("version", "-version"); 
    parameters.put("force", "-force");
    parameters.put("only", "-only");
    parameters.put("forcelocal", "-forcelocal");
    parameters.put("onlyAutomate", "-onlyAutomate");
    parameters.put("proxyHost", "-proxyHost");
    parameters.put("proxyPort", "-proxyPort");
    parameters.put("proxyUser", "-proxyUser");
    parameters.put("proxyPass", "-proxyPass");
    parameters.put("hosts", "-hosts");
  }
  
  Local() throws Exception {
    init();
  }
  
  void makeCommand(HashMap<String,String> options){
    Set set = options.entrySet();
      Iterator i = set.iterator();
      while(i.hasNext()) {
        Map.Entry me = (Map.Entry)i.next();
        String parameter = me.getKey().toString().trim();
        if(parameters.get(parameter)!=null)
          if(me.getValue()!=null){
            command.add(parameters.get(parameter));
            command.add((String) me.getValue());
          }
          else
            command.add(parameters.get(parameter));
      }
  }
}
