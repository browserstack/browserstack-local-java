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

    private static final List<String> IGNORE_KEYS = Arrays.asList("key", "logfile", "binarypath");

    List<String> command;
    Map<String, String> startOptions;
    String binaryPath;
    String logFilePath;
    int pid = 0;

    private LocalProcess proc = null;

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
        startOptions = options;
        if (options.get("binarypath") != null) {
            binaryPath = options.get("binarypath");
        } else {
            LocalBinary lb = new LocalBinary();
            binaryPath = lb.getBinaryPath();
        }

        logFilePath = options.get("logfile") == null ? (System.getProperty("user.dir") + "/local.log") : options.get("logfile");
        makeCommand(options, "start");

        if (options.get("onlyCommand") != null) return;

        if (proc == null) {
            FileWriter fw = new FileWriter(logFilePath);
            fw.write("");
            fw.close();

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
                throw new LocalException(obj.getString("message"));
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
        command.add("-logFile");
        command.add(logFilePath);
        command.add(options.get("key"));

        for (Map.Entry<String, String> opt : options.entrySet()) {
            String parameter = opt.getKey().trim();
            if (IGNORE_KEYS.contains(parameter)) {
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
