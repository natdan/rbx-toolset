package org.ah.robox;

import java.io.File;
import java.util.List;

public class InstallCommand {
    public static void execute(List<String> args) throws Exception {

        File jarPath = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());

        System.out.println(jarPath);

    }
}
