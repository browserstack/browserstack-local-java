package com.browserstack.local;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.json.*;

/**
 * Creates and manages a secure tunnel connection to BrowserStack.
 */
public class Local {

    private static final List<String> IGNORE_KEYS = Arrays.asList("key", "binarypath");

    List<String> command;
    Map<String, String> startOptions;
    String binaryPath;
    int pid = 0;

    private LocalProcess proc = null;

    private final Map<String, String> parameters;
    private final Map<String, String> avoidValueParameters;

    public Local() {
        avoidValueParameters = new HashMap<String, String>();
        avoidValueParameters.put("v", "-vvv");
        avoidValueParameters.put("force", "-force");
        avoidValueParameters.put("forcelocal", "-forcelocal");
        avoidValueParameters.put("onlyAutomate", "-onlyAutomate");
        avoidValueParameters.put("forceproxy", "-forceproxy");

        parameters = new HashMap<String, String>();
        parameters.put("f", "-f");
        parameters.put("only", "-only");
        parameters.put("localIdentifier", "-localIdentifier");
        parameters.put("proxyHost", "-proxyHost");
        parameters.put("proxyPort", "-proxyPort");
        parameters.put("proxyUser", "-proxyUser");
        parameters.put("proxyPass", "-proxyPass");
    }

    /**
     * Starts Local instance with options
     *
     * @param options Options for the Local instance
     * @throws Exception
     */
    public void start(Map<String, String> options) throws Exception {
        startOptions = options;
        if (options.get("binarypath") != null) {
            binaryPath = options.get("binarypath");
        } else {
            LocalBinary lb = new LocalBinary();
            binaryPath = lb.getBinaryPath();
        }

        makeCommand(options, "start");

        if (options.get("onlyCommand") != null) return;

        if (proc == null) {
            proc = runCommand(command);
            BufferedReader stdoutbr = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            BufferedReader stderrbr = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
            String stdout="", stderr="", line;
            while ((line = stdoutbr.readLine()) != null) {
                stdout += line;
            }
            while ((line = stderrbr.readLine()) != null) {
                stderr += line;
            }
            int r = proc.waitFor();

            JSONObject obj = new JSONObject(!stdout.equals("") ? stdout : stderr);
            if(!obj.getString("state").equals("connected")){
                throw new LocalException(obj.getJSONObject("message").getString("message"));
            }
            else {
                pid = obj.getInt("pid");
            }
        }
    }

    /**
     * Stops the Local instance
     *
     * @throws InterruptedException
     */
    public void stop() throws Exception {
        if (pid != 0) {
            makeCommand(startOptions, "stop");
            proc = runCommand(command);
            proc.waitFor();
            pid = 0;
        }
    }

    /**
    * Stops the Local instance specified by the given identifier
    * @param options Options supplied for the Local instance
    **/
    public void stop(Map<String, String> options) throws Exception {
        if (options.get("binarypath") != null) {
            binaryPath = options.get("binarypath");
        } else {
            LocalBinary lb = new LocalBinary();
            binaryPath = lb.getBinaryPath();
        }
        makeCommand(options, "stop");
        proc = runCommand(command);
        proc.waitFor();
        pid = 0;
    }

    /**
     * Checks if Local instance is running
     *
     * @return true if Local instance is running, else false
     */
    public boolean isRunning() throws Exception {
        if (pid == 0) return false;
        return isProcessRunning(pid);
    }

    /**
     * Creates a list of command-line arguments for the Local instance
     *
     * @param options Options supplied for the Local instance
     */
    private void makeCommand(Map<String, String> options, String opCode) {
        command = new ArrayList<String>();
        command.add(binaryPath);
        command.add("-d");
        command.add(opCode);
        command.add(options.get("key"));

        for (Map.Entry<String, String> opt : options.entrySet()) {
            String parameter = opt.getKey().trim();
            if (IGNORE_KEYS.contains(parameter)) {
                continue;
            }
            if (avoidValueParameters.get(parameter) != null && opt.getValue().trim().toLowerCase() != "false") {
                command.add(avoidValueParameters.get(parameter));
            } else {
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

    /**
     * Checks if process with pid is running
     *
     * @param pid pid for the process to be checked.
     * @link http://stackoverflow.com/a/26423642/941691
     */
    private boolean isProcessRunning(int pid) throws Exception {
        ArrayList<String> cmd = new ArrayList<String>();
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            //tasklist exit code is always 0. Parse output
            //findstr exit code 0 if found pid, 1 if it doesn't
            cmd.add("cmd");
            cmd.add("/c");
            cmd.add("\"tasklist /FI \"PID eq " + pid + "\" | findstr " + pid + "\"");
        }
        else {
            //ps exit code 0 if process exists, 1 if it doesn't
            cmd.add("ps");
            cmd.add("-p");
            cmd.add(String.valueOf(pid));
        }

        proc = runCommand(cmd);
        int exitValue = proc.waitFor();

        // 0 is the default exit code which means the process exists
        return exitValue == 0;
    }

    /**
     * Executes the supplied command on the shell.
     *
     * @param command Command to be executed on the shell.
     * @return {@link LocalProcess} for managing the launched process.
     * @throws IOException
     */
    protected LocalProcess runCommand(List<String> command) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        final Process process = processBuilder.start();

        return new LocalProcess() {
            public InputStream getInputStream() {
                return process.getInputStream();
            }

            public InputStream getErrorStream() {
                return process.getErrorStream();
            }

            public int waitFor() throws Exception {
                return process.waitFor();
            }
        };
    }

    public interface LocalProcess {
        InputStream getInputStream();

        InputStream getErrorStream();

        int waitFor() throws Exception;
    }
}
