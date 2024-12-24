package com.browserstack.local;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipException;

class LocalBinary {

    private static final String BIN_URL = "https://www.browserstack.com/local-testing/downloads/binaries/";

    private String httpPath;

    private String binaryPath;

    private boolean isOSWindows;

    private final String orderedPaths[] = {
            System.getProperty("user.home") + "/.browserstack",
            System.getProperty("user.dir"),
            System.getProperty("java.io.tmpdir")
    };

    LocalBinary(String path) throws LocalException {
        initialize();
        if (path != "") {
            getBinaryOnPath(path);
        } else {
            getBinary();
        }
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
            if (arch.contains("64")) {
                if (isAlpine()) {
                    binFileName = "BrowserStackLocal-alpine";
                } else {
                    binFileName = "BrowserStackLocal-linux-x64";
                }
            } else {
                binFileName = "BrowserStackLocal-linux-ia32";
            }
        } else {
            throw new LocalException("Failed to detect OS type");
        }

        String sourceURL = BIN_URL;
        httpPath = sourceURL + binFileName;
    }

    private boolean isAlpine() {
        String[] cmd = { "/bin/sh", "-c", "grep -w \"NAME\" /etc/os-release" };
        boolean flag = false;

        try {
            Process os = Runtime.getRuntime().exec(cmd);
            BufferedReader stdout = new BufferedReader(new InputStreamReader(os.getInputStream()));

            flag = stdout.readLine().contains("Alpine");
        } finally {
            return flag;
        }
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

    private void getBinaryOnPath(String path) throws LocalException {
        binaryPath = path;

        if (!new File(binaryPath).exists()) {
            downloadBinary(binaryPath, true);
        }
    }

    private void getBinary() throws LocalException {
        String destParentDir = getAvailableDirectory();
        binaryPath = destParentDir + "/BrowserStackLocal";

        if (isOSWindows) {
            binaryPath += ".exe";
        }

        if (!new File(binaryPath).exists()) {
            downloadBinary(destParentDir, false);
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

    private void downloadBinary(String destParentDir, Boolean custom) throws LocalException {
        try {
            String source = destParentDir;
            if (!custom) {
                if (!new File(destParentDir).exists())
                    new File(destParentDir).mkdirs();

                source = destParentDir + "/BrowserStackLocal";
                if (isOSWindows) {
                    source += ".exe";
                }
            }
            URL url = new URL(httpPath);

            File f = new File(source);
            newCopyToFile(url, f);

            changePermissions(binaryPath);
        } catch (Exception e) {
            throw new LocalException("Error trying to download BrowserStackLocal binary: " + e.getMessage());
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

    private static void newCopyToFile(URL url, File f) throws IOException {
        URLConnection conn = url.openConnection();
        conn.setRequestProperty("User-Agent", "browserstack-local-java/" + Local.getPackageVersion());
        conn.setRequestProperty("Accept-Encoding", "gzip, *");
        String contentEncoding = conn.getContentEncoding();

        if (contentEncoding == null || !contentEncoding.toLowerCase().contains("gzip")) {
            customCopyInputStreamToFile(conn.getInputStream(), f, url);
            return;
        }

        try (InputStream stream = new GZIPInputStream(conn.getInputStream())) {
            if (System.getenv().containsKey("BROWSERSTACK_LOCAL_DEBUG_GZIP")) {
                System.out.println("using gzip in " + conn.getRequestProperty("User-Agent"));
            }

            customCopyInputStreamToFile(stream, f, url);
        } catch (ZipException e) {
            FileUtils.copyURLToFile(url, f);
        }
    }

    private static void customCopyInputStreamToFile(InputStream stream, File file, URL url) throws IOException {
        try {
            FileUtils.copyInputStreamToFile(stream, file); 
        } catch (Throwable e) {
            try (FileOutputStream fos = new FileOutputStream(file)) {
                IOUtils.copy(stream, fos);
            } catch (Throwable th) {
                FileUtils.copyURLToFile(url, file);
            }
        }
    }
}
