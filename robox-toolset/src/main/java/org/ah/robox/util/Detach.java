/*******************************************************************************
 * Copyright (c) 2018 Creative Sphere Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *    Creative Sphere - initial API and implementation
 *
 *
 *******************************************************************************/

package org.ah.robox.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.ah.robox.Main;
import org.ah.robox.ui.MonitorWindow;

public class Detach {

    public static void detach(String mainClass) {


        try {
            String javaHome = System.getProperty("java.home");
            String classPath = System.getProperty("java.class.path");

            File thisPath = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());

            @SuppressWarnings("unused")
            Process process;
            if (thisPath.getName().endsWith(".jar")) {

                List<String> args = new ArrayList<String>();
                args.add(javaHome + "/bin/java");
                args.add("-cp");
                args.add(classPath);
                args.add("-jar");
                args.add(thisPath.getAbsolutePath());
                args.add("---detached");
                for (String a : Main.originalArgs) {
                    args.add(a);
                }

                ProcessBuilder processBuilder = new ProcessBuilder(args);
                process = processBuilder.start();
            } else {

                List<String> args = new ArrayList<String>();
                args.add(javaHome + "/bin/java");
                args.add("-cp");
                args.add(classPath);
                args.add(mainClass);
                args.add("---detached");
                for (String a : Main.originalArgs) {
                    args.add(a);
                }

                ProcessBuilder processBuilder = new ProcessBuilder(args);
                process = processBuilder.start();
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static void main(String[] args) throws Exception {
        Main.originalArgs = args;
        if (args.length > 0 && "---detached".equals(args[0])) {
            MonitorWindow monitorWindow = new MonitorWindow();
            monitorWindow.setVisible(true);
        } else {
            Properties properties = System.getProperties();
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                System.out.println(entry.getKey() + " : " + entry.getValue());
            }
//            String mainClass = System.getProperty("sun.java.command");

            detach(Detach.class.getName());
        }
    }

}
