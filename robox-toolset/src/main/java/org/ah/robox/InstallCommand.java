package org.ah.robox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public class InstallCommand {

    private static final Logger logger = Logger.getLogger(InstallCommand.class.getName());

    public static final String BIN_DIR = "/usr/local/bin";

    public static void execute(List<String> args) throws Exception {
        boolean isLinux = System.getProperty("os.name").contains("Linux");
        boolean isOSX = System.getProperty("os.name").contains("Mac");
        if (!isLinux && !isOSX) {
            logger.severe("Install currently works only on Linux or OSX operating systems");
            System.exit(-1);;
        }

        String version = System.getenv().get("RBX_VERSION");
        String timestamp = System.getenv().get("RBX_TIMESTAMP");

        if (version == null) { version = "NOT_DETECTED"; }
        if (timestamp == null) { timestamp = "NOT_DETECTED"; }

        logger.info("Detected version: " + version + " and timestamp " + timestamp);
        logger.info("");

        File thisPath = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());

//        System.out.println(thisPath);

        if (thisPath.getAbsolutePath().startsWith(BIN_DIR)) {
            logger.severe("Already installed in " + thisPath.getAbsolutePath());
            System.exit(-1);
        }

        String destName = "rbx";

        File destDir = new File(BIN_DIR);

        if (!destDir.exists()) {
            logger.severe("Cannot install as there is no " + BIN_DIR);
            System.exit(-1);
        }

        byte[] buf = new byte[10240];

        File destFile = new File(destDir, destName);

        if (destFile.exists()) {
            tryToReadVersion(destFile);
        }

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
                logger.severe("Permission denied. Try running with 'sudo' or any other escalated privileges admin account.");
                System.exit(-1);
            } else {
                logger.severe("File not found: " + e.getMessage());
                System.exit(-1);
            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.severe("Failed to install");
            System.exit(-1);
        }

        try {
            destFile.setExecutable(true, false);
        } catch (SecurityException e) {
            logger.severe("Failed to set executable permissions. Try running 'sudo chmod a+x " + BIN_DIR + "/" + destName + "'");
            System.exit(-1);
        }

        logger.info("Done. Try 'rbx -h' for more info");
    }

    private static void tryToReadVersion(File destFile) {
        // TODO Auto-generated method stub

    }
}
