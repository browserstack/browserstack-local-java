package com.BS;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

class Local{
	
	Process BrowserStackLocal = null;
	List<String> command;
	HashMap<String, String> parameters;
	
	void start(HashMap<String,String> options) throws Exception
	{
		LocalBinary lb = new LocalBinary();
		command = new ArrayList<String>();
		command.add(lb.binary_path); 
		command.add(options.get("key"));

		makeCommand(options);
		
	    if (BrowserStackLocal == null) 
		{
	    	ProcessBuilder processBuilder = new ProcessBuilder(command);

	    	System.out.println("Setting up Local Testing connection...");
			BrowserStackLocal = processBuilder.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(BrowserStackLocal.getInputStream()));
			String string;
			int j = 0;
			while ((string = reader.readLine()) != null) 
			{
				if (string.equalsIgnoreCase("Press Ctrl-C to exit")) 
				{
					System.out.println("Local Testing connection has been established.");
					break; 
				}
				
				if (string.contains("*** Error"))
				{
					throw new BrowserStackLocalException(string);
				}
				if (j++ > 20) 
				{ 
					throw new BrowserStackLocalException("Could not start BrowserStackLocal"); 
				}
			}
			
		}

	}
	
	void stop()
	{
		if (BrowserStackLocal != null) 
		{
			BrowserStackLocal.destroy();
			System.out.println("Disconnected successfully");
		}
	}
	
	boolean isRunning()
	{
		try
		{
			BrowserStackLocal.exitValue();
		    return false;
		}
		catch (Exception e) 
		{
		    return true;
		}
	}
	
	void init()
	{
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
		parameters.put("logfile", "-logfile");
	}
	
	Local() throws Exception
	{
		init();
	}
	
	void makeCommand(HashMap<String,String> options)
	{
		Set set = options.entrySet();
	    Iterator i = set.iterator();
	    while(i.hasNext()) 
	    {
	    	Map.Entry me = (Map.Entry)i.next();
	    	String parameter = me.getKey().toString().trim();
	    	if(parameters.get(parameter)!=null)
	    		if(me.getValue()!=null)
	    		{
	    			command.add(parameters.get(parameter));
	    			command.add((String) me.getValue());
	    		}
	    		else
	    			command.add(parameters.get(parameter));
	    }
	}
}