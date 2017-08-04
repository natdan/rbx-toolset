/*
 * This file is part of rbx-toolset.
 *
 * rbx-toolset is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * rbx-toolset is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with rbx-toolset.  If not, see <http://www.gnu.org/licenses/>.
 *
*/
package org.ah.robox.extensions.installer;

import static javax.swing.SwingUtilities.invokeLater;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Stack;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author Daniel Sendula
 */
public class Installer {

    private InstallerWindow installerWindow;
    private OS os;
    private boolean exterimental = false;
    private boolean permanentError = false;
    private File amDir;
    private File commonDir;
    private File curaDir;
    private File curaEngineFile;
    private File curaEngineOrigFile;

    public Installer() {
        installerWindow = new InstallerWindow();
    }

    public void start() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        installerWindow.setVisible(true);
        installerWindow.setLeaveAction(() -> System.exit(1));
        installerWindow.setAMPathChanged(s -> pathChanged(s));
        installerWindow.setInstallerButtonEnable(false);
        installerWindow.setInstallAction(() -> inSeparateThread(() -> install()));
        installerWindow.setUninstallAction(() -> inSeparateThread(() -> uninstall()));

        os = OS.detect();
        if (os == OS.UNKNOWN) {
            permanentError = true;
            installerWindow.log("Cannot determine OS");
        } else {
            installerWindow.log("Detected " + os.getLabel() + ".");
            invokeLater(() -> installerWindow.setAMPath(os.getDefaultInstallationPath()));
        }
    }

    private void pathChanged(String amPath) {
        File path = new File(amPath).getAbsoluteFile();
        File file = isAMInstallation(path);
        if (file != null) {
            if (!file.getAbsolutePath().equals(path.getAbsolutePath())) {
                invokeLater(() -> installerWindow.setAMPath(file.getAbsolutePath()));
            } else {
                // Above branch of if statement is going to invoke this again and we'll end up here

                prepareForInstallation(file);
            }
        } else {
            installerWindow.setUninstallButtonVisible(false);
            installerWindow.setInstallerButtonText("Install");

            if (path.exists()) {
                installerWindow.setAMPathError("Path does not represent AutoMaker install directory.");
            } else {
                installerWindow.setAMPathError("Path does not exist.");
            }
            installerWindow.setInstallerButtonEnable(false);
        }
    }

    private File isAMInstallation(File amDir) {
        if (amDir == null || !amDir.exists()) {
            return null;
        }

        if (!amDir.isDirectory()) {
            return isAMInstallation(amDir.getParentFile());
        }

        if (amDir.toPath().getNameCount() < 1) {
            return null;
        }

        File autoMaker = new File(amDir, "AutoMaker");
        File common = new File(amDir, "Common");

        if (!autoMaker.exists() || !common.exists()) {
            return isAMInstallation(amDir.getParentFile());
        }

        return amDir;
    }

    private void prepareForInstallation(File amDir) {
        this.amDir = amDir;
        this.commonDir = new File(amDir, "Common");
        this.curaDir = new File(commonDir, "Cura");

        if (!curaDir.exists()) {
            installerWindow.log("Failed to find '<AMInstallDir>/Common/Cura' subfolder. ");
            return;
        }

        this.curaEngineFile = new File(curaDir, os.getCuraEnginePath());
        this.curaEngineOrigFile = new File(curaDir, os.getCuraEngineOrigPath());

        if (!curaEngineFile.exists()) {
            installerWindow.log("Failed to find '<AMInstallDir>/Common/Cura/" + os.getCuraEnginePath() + "' file. ");
            return;
        }

        if (curaEngineOrigFile.exists()) {
            installerWindow.log("Detected already installed version Robox Slicer Extension.");
            installerWindow.setUninstallButtonVisible(true);
            installerWindow.setInstallerButtonText("Update");
        }

        installerWindow.setInstallerButtonEnable(true && !permanentError);
        installerWindow.setAMPathError(" ");
    }

    private void inSeparateThread(Runnable body) {
        Thread thread = new Thread(body);
        thread.setDaemon(true);
        thread.start();
    }

    private void uninstall() {
        boolean success = false;
        try {
            installerWindow.hideInstallButtons();
            if (!curaEngineFile.delete()) {
                installerWindow.log("Failed to remove " + curaEngineFile.getAbsolutePath());
                return;
            }

            if (!curaEngineOrigFile.renameTo(curaEngineFile)) {
                installerWindow.log("Failed to rename " + curaEngineOrigFile.getAbsolutePath() + " to " + curaEngineFile.getAbsolutePath());
                return;
            }

            success = true;

            Stack<InstallationFile> dirDelStack = new Stack<>();

            for (InstallationFile file : os.getInstallationFiles()) {
                if (file.isDelete()) {
                    if (file.isDir()) {
                        dirDelStack.push(file);
                    } else {
                        installerWindow.log("Deleting file " + file.getLocalPath());
                        File f = new File(amDir, file.getLocalPath());
                        if (f.exists() && !f.delete()) {
                            installerWindow.log("Failed to remove " + f.getAbsolutePath());
                            success = false;
                        }
                    }
                }
            }
            while (!dirDelStack.isEmpty()) {
                InstallationFile file = dirDelStack.pop();
                installerWindow.log("Deleting folder " + file.getLocalPath());
                File f = new File(amDir, file.getLocalPath());
                if (f.exists() && !f.delete()) {
                    installerWindow.log("Failed to remove folder " + f.getAbsolutePath());
                    success = false;
                }
            }
        } finally {
            if (success) {
                installerWindow.log("Successfully uninstalled Robox Slicer Extension.");
            } else {
                installerWindow.log("There were errors uninstalling Robox Slicer Extension.");
            }
            installerWindow.setCancelButtonText("Done");
        }
    }

    private void install() {
        boolean success = false;
        try {
            installerWindow.hideInstallButtons();

            if (curaEngineOrigFile.exists()) {
                installerWindow.log("Already has " + curaEngineOrigFile.getAbsolutePath() + " - it will stay.");
                if (!curaEngineFile.delete()) {
                    installerWindow.log("Failed to delete " + curaEngineFile.getAbsolutePath());
                    return;
                }
            } else {
                if (!curaEngineFile.renameTo(curaEngineOrigFile)) {
                    installerWindow.log("Failed to rename " + curaEngineFile.getAbsolutePath() + " to " + curaEngineOrigFile.getAbsolutePath());
                    return;
                }
            }

            success = true;
            for (InstallationFile file : os.getInstallationFiles()) {
                File f = new File(amDir, file.getLocalPath());
                if (file.isDir()) {
                    if (f.exists()) {
                        if (f.isFile()) {
                            if (!f.delete()) {
                                installerWindow.log("Failed create dir because failed to remove file " + f.getAbsolutePath());
                                success = false;
                            } else if (!f.mkdirs()) {
                                installerWindow.log("Failed to create " + f.getAbsolutePath());
                                success = false;
                            } else {
                                installerWindow.log("Created file " + file.getLocalPath());
                            }
                        }
                    } else if (!f.mkdirs()) {
                        installerWindow.log("Failed to create " + f.getAbsolutePath());
                        success = false;
                    }
                } else {
                    installerWindow.log("Dowloading file " + file.getLocalPath());
                    success = success && download(file, f);
                    if (file.isExecutable() && (os == OS.LINUX || os == OS.OSX) && !curaEngineFile.setExecutable(true)) {
                        installerWindow.log("Failed to make " + file.getLocalPath() + " executable.");
                        success = false;
                    }
                }
            }
        } finally {
            if (success) {
                installerWindow.log("Successfully installed Robox Slicer Extension.");
            } else {
                installerWindow.log("There were errors uninstalling Robox Slicer Extension.");
            }
            installerWindow.setCancelButtonText("Done");
        }
    }

    private boolean download(InstallationFile file, File f) {
        try {
            f.createNewFile();

            byte[] buf = new byte[10240];
            URL url = getURL(file.getRepoPath());
            try (InputStream in = url.openStream()) {
                try (FileOutputStream out = new FileOutputStream(f)) {
                    int r = in.read(buf);
                    while (r > 0) {
                        out.write(buf, 0, r);
                        r = in.read(buf);
                    }
                }
            } catch (IOException e) {
                installerWindow.log("There were errors downloading file from " + url.toString());
                e.printStackTrace();
                return false;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            installerWindow.log("Could not create file " + f.getAbsolutePath());
        }
        return true;
    }

    private URL getURL(String repoPath) throws MalformedURLException {
        if (exterimental) {
            return new URL("https://raw.githubusercontent.com/nebbian/RoboxSlicerExtension/Initial-Flow-Code/" + repoPath);
        } else {
            return new URL("https://github.com/nebbian/RoboxSlicerExtension/blob/master/" + repoPath + "?raw=true");
        }
    }
}
