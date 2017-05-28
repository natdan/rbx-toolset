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
public enum InstallationFile {

    CuraEngine("Common/Cura/CuraEngine", "shell%20scripts/CuraEngine", false, false),
    Slic3rPostprocessPl("Common/Cura/slic3r_postprocess.pl", "shell%20scripts/slic3r_postprocess.pl", false, true),
    Slic3rDir("Common/Slic3r", null, true, true),
    Slic3rConfigDir("Common/Slic3r/Config", null, true, true),
    Slic3rConfigFile("Common/Slic3r/Config/Robox_slic3r_config_bundle.ini", "slic3r/config/Robox_slic3r_config_bundle.ini", false, true);

    private String localPath;
    private String repoPath;
    private boolean isDir;
    private boolean delete;

    InstallationFile(String localPath, String repoPath, boolean isDir, boolean delete) {
        this.localPath = localPath;
        this.repoPath = repoPath;
        this.isDir = isDir;
        this.delete = delete;
    }

    public String getLocalPath() { return localPath; }

    public String getRepoPath() { return repoPath; }

    public boolean isDir() { return isDir; }

    public boolean isDelete() { return delete; }
}
