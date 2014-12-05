/*******************************************************************************
 * Copyright (c) 2014 Creative Sphere Limited.
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
package org.ah.robox.comms.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;

/**
 *
 *
 * @author Daniel Sendula
 */
public class SharedLibraries {

    public static final String JAVA_NATIVE_LIB_TEMP_DIR = "/java-native-libs/";

    public static void load(String name) {

        try {
            boolean isWindows = System.getProperty("os.name").contains("Windows");
            boolean isLinux = System.getProperty("os.name").contains("Linux");
            boolean isOSX = System.getProperty("os.name").contains("Mac");
            boolean isArm = System.getProperty("os.arch").equalsIgnoreCase("arm");
            boolean is64Bit = System.getProperty("os.arch").equalsIgnoreCase("amd64") || System.getProperty("os.arch").equalsIgnoreCase("x86_64");

            if (isWindows) {
                addPath(System.getProperty("java.io.tmpdir") + JAVA_NATIVE_LIB_TEMP_DIR + "windows");

                if (!is64Bit) {
                    loadLibrary("windows/" + name + ".dll");
                } else {
                    loadLibrary("windows/" + name + "64.dll");
                }
            }
            if (isLinux) {
                if (isArm) {
                    addPath(System.getProperty("java.io.tmpdir") + JAVA_NATIVE_LIB_TEMP_DIR + "linux-arm");
                    if (!is64Bit) {
                        loadLibrary("linux-arm/lib" + name + ".so");
                    } else {
                        loadLibrary("linux-arm/lib" + name + "64.so");
                    }
                } else {
                    addPath(System.getProperty("java.io.tmpdir") + JAVA_NATIVE_LIB_TEMP_DIR + "linux-intel");
                    if (!is64Bit) {
                        loadLibrary("linux-intel/lib" + name + ".so");
                    } else {
                        loadLibrary("linux-intel/lib" + name + "64.so");
                    }
                }
            }
            if (isOSX) {
                addPath(System.getProperty("java.io.tmpdir") + JAVA_NATIVE_LIB_TEMP_DIR + "osx");
                if (!is64Bit) {
                    loadLibrary("osx/lib" + name + ".dylib");
                } else {
                    loadLibrary("osx/lib" + name + "64.dylib");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void loadLibrary(String name) throws IOException {
        String path = provideTempFile(name);
        System.load(path);
    }

    private static String provideTempFile(String name) throws IOException {

        File libFile = new File(System.getProperty("java.io.tmpdir") + JAVA_NATIVE_LIB_TEMP_DIR + name);
        File classFile = getClassOrJarFile();

        if (!libFile.exists() || libFile.lastModified() != classFile.lastModified()) {
            try {
                // Extract native from classpath to temp dir.
                InputStream input = SharedLibraries.class.getResourceAsStream("/" + name);
                if (input == null) {
                    return null;
                }
                try {
                    libFile.getParentFile().mkdirs();
                    FileOutputStream output = new FileOutputStream(libFile);
                    try {
                        byte[] buffer = new byte[10240];
                        int r = input.read(buffer);
                        while (r > 0) {
                            output.write(buffer, 0, r);
                            r = input.read(buffer);
                        }
                    } finally {
                        output.close();
                    }
                } finally {
                    input.close();
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        return libFile.getAbsolutePath();
    }

    protected static File getClassOrJarFile() throws IOException {
        Class<?> cls = SharedLibraries.class;
        String classNamePath = cls.getName().replace(".", "/") + ".class";
        URL thisClassResource = cls.getClassLoader().getResource(classNamePath);

        String path = null;
        if (thisClassResource.toString().startsWith("jar:")) {
            String jarFileName = thisClassResource.getPath();
            if (jarFileName.contains("!")) {
                jarFileName = jarFileName.substring(0, jarFileName.lastIndexOf('!'));
            }

            path = jarFileName;
        } else if (thisClassResource.toString().startsWith("file:")) {
            path = thisClassResource.toString();
        } else {
            System.err.println("Cannot work with classes started from URL: " + thisClassResource);
            System.exit(1);
            path = "";
        }

        try {
            URI pathUri = new URL(path).toURI();
            if (pathUri.getAuthority() != null && pathUri.getAuthority().length() > 0) {
                pathUri = (new URL("file://" + path.substring("file:".length()))).toURI();
            }
            return new File(pathUri);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static void addPath(String filePath) {
        System.setProperty("java.library.path", filePath);

        try {
            Field usrPathsField = ClassLoader.class.getDeclaredField("usr_paths");
            usrPathsField.setAccessible(true);

            String[] paths = (String[]) usrPathsField.get(null);

            boolean found = false;
            for (String path : paths) {
                if (path.equals(filePath)) {
                    found = true;
                }
            }

            if (!found) {
                String[] newPaths = Arrays.copyOf(paths, paths.length + 1);
                newPaths[newPaths.length - 1] = filePath;
                usrPathsField.set(null, newPaths);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialise native library " + filePath, e);
        }
    }
}
