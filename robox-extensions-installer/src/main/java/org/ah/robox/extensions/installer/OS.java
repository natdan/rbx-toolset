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

/**
*
* @author Daniel Sendula
*/
public enum OS {

    WINDOWS("Windows", "C:\\Program Files\\CEL", "CuraEngine.exe", "CuraEngine_orig.exe"),
    LINUX("Linux", "~", "CuraEngine", "CuraEngine_exe"),
    OSX("OSX", "/Applications/CEL", "CuraEngine", "CuraEngine_exe"),
    UNKNOWN("Unknown", "", "", "");

    private String label;
    private String defaultInstallationPath;
    private String curaEnginePath;
    private String curaEngineOrigPath;

    OS(String label, String defaultInstallationPath, String curaEnginePath, String curaEngineOrigPath) {
        this.label = label;
        this.defaultInstallationPath = defaultInstallationPath;
        this.curaEnginePath = curaEnginePath;
        this.curaEngineOrigPath = curaEngineOrigPath;
    }

    public String getLabel() { return label; }

    public String getDefaultInstallationPath() { return defaultInstallationPath; }

    public String getCuraEnginePath() { return curaEnginePath; }

    public String getCuraEngineOrigPath() { return curaEngineOrigPath; }

    public static OS detect() {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.indexOf("win") >= 0) {
            return WINDOWS;
        }
        if (os.indexOf("linux") >= 0) {
            return LINUX;
        }
        if (os.indexOf("mac") >= 0) {
            return OSX;
        }

        return UNKNOWN;
    }
}
