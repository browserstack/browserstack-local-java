package com.browserstack.local;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Creates and manages a secure tunnel connection to BrowserStack.
 */
public class Local {

    List<String> command;

    private Process proc = null;

    private final Map<String, String> parameters;

    public Local() {
        parameters = new HashMap<String, String>();
        parameters.put("v", "-vvv");
        parameters.put("f", "-f");
        parameters.put("force", "-force");
        parameters.put("only", "-only");
        parameters.put("forcelocal", "-forcelocal");
        parameters.put("localIdentifier", "-localIdentifier");
        parameters.put("onlyAutomate", "-onlyAutomate");
        parameters.put("proxyHost", "-proxyHost");
        parameters.put("proxyPort", "-proxyPort");
        parameters.put("proxyUser", "-proxyUser");
        parameters.put("proxyPass", "-proxyPass");
        parameters.put("forceproxy", "-forceproxy");
        parameters.put("hosts", "-hosts");
    }

    /**
     * Starts Local instance with options
     *
     * @param options Options for the Local instance
     * @throws Exception
     */
    public void start(Map<String, String> options) throws Exception {
        command = new ArrayList<String>();

        if (options.get("binarypath") != null) {
            command.add(options.get("binarypath"));
        } else {
            LocalBinary lb = new LocalBinary();
            command.add(lb.getBinaryPath());
        }

        String logFilePath = options.get("logfile") == null ?
                (System.getProperty("user.dir") + "/local.log") : options.get("logfile");
        command.add("-logFile");
        command.add(logFilePath);

        command.add(options.get("key"));
        makeCommand(options);

        if (options.get("onlyCommand") != null) return;

        if (proc == null) {
            ProcessBuilder processBuilder = new ProcessBuilder(command);

            FileWriter fw = new FileWriter(logFilePath);
            fw.write("");
            fw.close();

            proc = processBuilder.start();
            FileReader f = new FileReader(logFilePath);
            BufferedReader reader = new BufferedReader(f);
            String string;

            while (true) {
                string = reader.readLine();
                if (string == null) continue;

                if (string.equalsIgnoreCase("Press Ctrl-C to exit")) {
                    f.close();
                    break;
                }

                if (string.contains("*** Error")) {
                    f.close();
                    stop();
                    throw new LocalException(string);
                }
            }

        }
    }

    /**
     * Stops the Local instance
     *
     * @throws InterruptedException
     */
    public void stop() throws InterruptedException {
        if (proc != null) {
            proc.destroy();
            while (isRunning()) {
                Thread.sleep(1000);
            }
        }
    }

    /**
     * Checks if Local instance is running
     *
     * @return true if Local instance is running, else false
     */
    public boolean isRunning() {
        if (proc == null) return false;

        try {
            proc.exitValue();
            return false;
        } catch (IllegalThreadStateException e) {
            return true;
        }
    }

    /**
     * Creates a list of command-line arguments for the Local instance
     *
     * @param options Options supplied for the Local instance
     */
    private void makeCommand(Map<String, String> options) {
        for (Map.Entry<String, String> opt : options.entrySet()) {
            List<String> ignoreKeys = Arrays.asList("key", "logfile", "binarypath");
            String parameter = opt.getKey().trim();
            if (ignoreKeys.contains(parameter)) {
                continue;
            }
            if (parameters.get(parameter) != null) {
                command.add(parameters.get(parameter));
            } else {
                command.add("-" + parameter);
            }
            if (opt.getValue() != null) {
                command.add(opt.getValue().trim());
            }
        }
    }
}
