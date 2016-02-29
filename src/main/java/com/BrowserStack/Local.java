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
  
  List<String> command;
  Process proc = null;
  String logFilePath;
  HashMap<String, String> parameters;

  Local() throws Exception {
    parameters = new HashMap<String, String>();
    parameters.put("v","-vvv");
    parameters.put("f","-f");
    parameters.put("h","-h");
    parameters.put("version", "-version"); 
    parameters.put("force", "-force");
    parameters.put("only", "-only");
    parameters.put("forcelocal", "-forcelocal");
    parameters.put("localIdentifier", "-localIdentifier");
    parameters.put("onlyAutomate", "-onlyAutomate");
    parameters.put("proxyHost", "-proxyHost");
    parameters.put("proxyPort", "-proxyPort");
    parameters.put("proxyUser", "-proxyUser");
    parameters.put("proxyPass", "-proxyPass");
    parameters.put("hosts", "-hosts");
  }
  
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

    if(options.get("onlyCommand") != null) return;
    
    if (proc == null){
      ProcessBuilder processBuilder = new ProcessBuilder(command);

      if((new File(logFilePath)).exists()){
        FileWriter f = new FileWriter(logFilePath);
        f.write("");
        f.close();
      }

      proc = processBuilder.start();
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
          stop();
          throw new LocalException(string);
        }
      }
      
    }
  }
  
  void stop() throws InterruptedException {
    if (proc != null) {
      proc.destroy();
      while(isRunning()){
        Thread.sleep(1000);
      }
    }
  }
  
  boolean isRunning(){
    if(proc == null) return false;

    try {
      proc.exitValue();
      return false;
    }
    catch (IllegalThreadStateException e){
      return true;
    }
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
