package com.browserstack.local;

import org.apache.commons.io.FileUtils;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
import java.net.URL;
import java.util.regex.Pattern;

class LocalBinary {

    private static final String BIN_URL = "https://s3.amazonaws.com/browserStack/browserstack-local/";

    private String httpPath;

    private String binaryPath;

    private boolean isOSWindows;

    private final String orderedPaths[] = {
            System.getProperty("user.home") + "/.browserstack",
            System.getProperty("user.dir"),
            System.getProperty("java.io.tmpdir")
    };

    LocalBinary() throws LocalException {
        initialize();
        getBinary();
        checkBinary();
    }

    private void initialize() throws LocalException {
        String osname = System.getProperty("os.name").toLowerCase();
        isOSWindows = osname.contains("windows");
        String binFileName;

        if (isOSWindows) {
            binFileName = "BrowserStackLocal.exe";
        } else if (osname.contains("mac") || osname.contains("darwin")) {
            binFileName = "BrowserStackLocal-darwin-x64";
        } else if (osname.contains("linux")) {
            String arch = System.getProperty("os.arch");
            binFileName = "BrowserStackLocal-linux-" + (arch.contains("64") ? "x64" : "ia32");
        } else {
            throw new LocalException("Failed to detect OS type");
        }

        httpPath = BIN_URL + binFileName;
    }

    private void checkBinary() throws LocalException{
        boolean binaryWorking = validateBinary();

        if(!binaryWorking){
            File binary_file = new File(binaryPath);
            if (binary_file.exists()) {
                binary_file.delete();
            }
            getBinary();
            if(!validateBinary()){
                throw new LocalException("BrowserStackLocal binary is corrupt");
            }
        }
    }

    private boolean validateBinary() throws LocalException{
        Process process;
        try {

            process = new ProcessBuilder(binaryPath,"--version").start();

            BufferedReader stdoutbr = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String stdout="",line="";

            while ((line = stdoutbr.readLine()) != null) {
                stdout += line;
            }
            process.waitFor();

            boolean validBinary = Pattern.matches("BrowserStack Local version \\d+\\.\\d+", stdout);

            return validBinary;
        }catch(IOException ex){
            throw new LocalException(ex.toString());
        }
        catch(InterruptedException ex){
            throw new LocalException(ex.toString());
        }
    }

    private void getBinary() throws LocalException {
        String destParentDir = getAvailableDirectory();
        binaryPath = destParentDir + "/BrowserStackLocal";

        if (isOSWindows) {
            binaryPath += ".exe";
        }

        if (!new File(binaryPath).exists()) {
            downloadBinary(destParentDir);
        }
    }

    private String getAvailableDirectory() throws LocalException {
        int i = 0;
        while (i < orderedPaths.length) {
            String path = orderedPaths[i];
            if (makePath(path))
                return path;
            else
                i++;
        }

        throw new LocalException("Error trying to download BrowserStackLocal binary");
    }

    private boolean makePath(String path) {
        try {
            if (!new File(path).exists())
                new File(path).mkdirs();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void downloadBinary(String destParentDir) throws LocalException {
        try {
            if (!new File(destParentDir).exists())
                new File(destParentDir).mkdirs();

            URL url = new URL(httpPath);
            String source = destParentDir + "/BrowserStackLocal";
            if (isOSWindows) {
                source += ".exe";
            }

            File f = new File(source);
            FileUtils.copyURLToFile(url, f);

            changePermissions(binaryPath);
        } catch (Exception e) {
            throw new LocalException("Error trying to download BrowserStackLocal binary");
        }
    }

    private void changePermissions(String path) {
        File f = new File(path);
        f.setExecutable(true, true);
        f.setReadable(true, true);
        f.setWritable(true, true);
    }

    public String getBinaryPath() {
        return binaryPath;
    }
}
