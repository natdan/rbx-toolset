package org.ah.robox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class InstallCommand {

    public static final String BIN_DIR = "/usr/local/bin";

    public static void execute(List<String> args) throws Exception {
        boolean isLinux = System.getProperty("os.name").contains("Linux");
        boolean isOSX = System.getProperty("os.name").contains("Mac");
        if (!isLinux && !isOSX) {
            System.err.println("Install currently works only on Linux or OSX operating systems");
            System.exit(-1);;
        }

        String version = System.getenv().get("RBX_VERSION");
        String timestamp = System.getenv().get("RBX_TIMESTAMP");

        if (version == null) { version = "NOT_DETECTED"; }
        if (timestamp == null) { timestamp = "NOT_DETECTED"; }

        System.out.println("Detected version: " + version + " and timestamp " + timestamp);
        System.out.println();

        File thisPath = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());

//        System.out.println(thisPath);

        if (thisPath.getAbsolutePath().startsWith(BIN_DIR)) {
            System.err.println("Already installed in " + thisPath.getAbsolutePath());
            System.exit(-1);
        }

        String destName = "rbx";

        File destDir = new File(BIN_DIR);

        if (!destDir.exists()) {
            System.out.println("Cannot install as there is no " + BIN_DIR);
            System.exit(-1);
        }

        byte[] buf = new byte[10240];

        File destFile = new File(destDir, destName);
        try {
            FileOutputStream fos = new FileOutputStream(destFile);
            try {
                FileInputStream fis = new FileInputStream(thisPath);
                try {
                    int r = fis.read(buf);
                    while (r > 0) {
                        fos.write(buf, 0, r);
                        r = fis.read(buf);
                    }
                } finally {
                    fis.close();
                }
            } finally {
                fos.close();
            }
        } catch (FileNotFoundException e) {
            if (e.getMessage() != null && e.getMessage().endsWith("(Permission denied)")) {
                System.err.println("Permission denied. Try running with 'sudo' or any other escalated privileges admin account.");
                System.exit(-1);
            } else {
                System.err.println("File not found: " + e.getMessage());
                System.exit(-1);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.print("Failed to install");
            System.exit(-1);
        }

        try {
            destFile.setExecutable(true, false);
        } catch (SecurityException e) {
            System.err.println("Failed to set executable permissions. Try running 'sudo chmod a+x " + BIN_DIR + "/" + destName + "'");
            System.exit(-1);
        }

        System.out.println("Done. Try 'rbx -h' for more info");
    }
}
