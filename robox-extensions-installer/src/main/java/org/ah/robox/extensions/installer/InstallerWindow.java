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

import static javax.swing.SpringLayout.EAST;
import static javax.swing.SpringLayout.NORTH;
import static javax.swing.SpringLayout.SOUTH;
import static javax.swing.SpringLayout.WEST;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/**
 * Installer window
 *
 * @author Daniel Sendula
 */
public class InstallerWindow extends JFrame {

    private JPanel buttonsPanel;
    private JButton installButton;
    private JButton uninstallButton;
    private JButton installCancelButton;

    private JPanel informationPanel;

    private JPanel mainPanel;
    private JTextField amPath;
    private JLabel amPathError;
    private JButton browseButton;
    private JTextArea logArea;

    public InstallerWindow() {
        super("Robox Extensions Installer");

        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);

        setSize(640, 400);

        createInformationPanel();
        createButtonsPanel();
        createMainPanel();
    }

    public void setLeaveAction(Runnable action) {
        installCancelButton.addActionListener(e -> action.run());
        addWindowListener(new WindowListener() {
            @Override public void windowOpened(WindowEvent e) { }
            @Override public void windowIconified(WindowEvent e) { }
            @Override public void windowDeiconified(WindowEvent e) { }
            @Override public void windowDeactivated(WindowEvent e) { }
            @Override public void windowClosed(WindowEvent e) { }
            @Override public void windowActivated(WindowEvent e) { }

            @Override public void windowClosing(WindowEvent e) {
                action.run();
            }
        });
    }

    public void setInstallAction(Runnable action) {
        installButton.addActionListener(e -> action.run());
    }

    public void setUninstallAction(Runnable action) {
        uninstallButton.addActionListener(e -> action.run());
    }

    private void createInformationPanel() {
        informationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JLabel icon = new JLabel(UIManager.getIcon("OptionPane.warningIcon"));
        JLabel label = new JLabel("This installer is going to install Robox Slicer Extension "
                + "from https://github.com/nebbian/RoboxSlicerExtension/\n"
                + "Slic3r configuration file needed for Slicer Extension will be "
                + "stored in <AM install dir>/Slic3r/Config.");
        informationPanel.add(icon);
        informationPanel.add(label);

        this.add(informationPanel, BorderLayout.NORTH);
    }

    private void createButtonsPanel() {
        buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        installButton = new JButton("Install");
        uninstallButton = new JButton("Uninstall");
        installCancelButton = new JButton("Cancel");
        uninstallButton.setVisible(false);

        buttonsPanel.add(installCancelButton);
        buttonsPanel.add(uninstallButton);
        buttonsPanel.add(installButton);
        add(buttonsPanel, BorderLayout.SOUTH);
    }

    private void createMainPanel() {
        mainPanel = new JPanel();
        SpringLayout layout = new SpringLayout();

        mainPanel.setLayout(layout);
        JLabel amPathLabel = new JLabel("AutoMaker Path:");
        amPath = new JTextField("");
        amPathError = new JLabel("");
        browseButton = new JButton("Browse...");
        logArea = new JTextArea("");

        JScrollPane logAreaScroller = new JScrollPane();
        logAreaScroller.setViewportView(logArea);
        logAreaScroller.setBorder(BorderFactory.createTitledBorder("Output"));

        mainPanel.add(amPathLabel);
        mainPanel.add(amPath);
        mainPanel.add(browseButton);
        mainPanel.add(amPathError);
        mainPanel.add(logAreaScroller);

        layout.putConstraint(NORTH, amPathLabel, 5, NORTH, mainPanel);
        layout.putConstraint(WEST, amPathLabel, 5, WEST, mainPanel);
        layout.putConstraint(NORTH, amPath, 5, SOUTH, amPathLabel);
        layout.putConstraint(WEST, amPath, 5, WEST, mainPanel);
        layout.putConstraint(NORTH, browseButton, 5, SOUTH, amPathLabel);
        layout.putConstraint(EAST, browseButton, -5, EAST, mainPanel);
        layout.putConstraint(EAST, amPath, 5, WEST, browseButton);
        layout.putConstraint(NORTH, amPathError, 5, SOUTH, amPath);
        layout.putConstraint(WEST, amPathError, 5, WEST, mainPanel);
        layout.putConstraint(NORTH, logAreaScroller, 15, SOUTH, amPathError);
        layout.putConstraint(WEST, logAreaScroller, 5, WEST, mainPanel);
        layout.putConstraint(EAST, logAreaScroller, -5, EAST, mainPanel);
        layout.putConstraint(SOUTH, logAreaScroller, -5, SOUTH, mainPanel);

        add(mainPanel, BorderLayout.CENTER);

        browseButton.addActionListener(e -> invokeFileSelector());
    }

    public void setInstallerButtonEnable(boolean enable) {
        installButton.setEnabled(enable);
    }

    public void setInstallerButtonText(String text) {
        installButton.setText(text);
    }

    public void setUninstallerButtonEnable(boolean enable) {
        uninstallButton.setEnabled(enable);
    }

    public void setUninstallButtonVisible(boolean visible) {
        uninstallButton.setVisible(visible);
    }

    public void setCancelButtonText(String text) {
        installCancelButton.setText(text);
    }

    public void setAMPathError(String error) {
        amPathError.setText(error);
    }

    public void setAMPath(String path) {
        amPath.setText(path);
    }

    public String getAMPath() {
        return amPath.getText();
    }

    public void setAMPathChanged(PathChanged callback) {
        amPath.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void changedUpdate(DocumentEvent e) { callback.changed(amPath.getText()); }
            @Override public void removeUpdate(DocumentEvent e) { callback.changed(amPath.getText()); }
            @Override public void insertUpdate(DocumentEvent e) { callback.changed(amPath.getText()); }
        });
    }

    public interface PathChanged { void changed(String s); }

    public void log(String s) {
        Document document = logArea.getDocument();
        try {
            document.insertString(document.getLength(), s + "\n", null);
        } catch (BadLocationException ignore) { }
    }

    public void hideInstallButtons() {
        installButton.setVisible(false);
        uninstallButton.setVisible(false);
    }

    private void invokeFileSelector() {
        File f = new File(amPath.getText());

        while (f != null && f.toPath() != null && f.toPath().getNameCount() > 0 && !f.exists()) {
            f = f.getParentFile();
        }

        JFileChooser fc = new JFileChooser();

        if (f != null) {
            fc.setSelectedFile(f);
        }
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int res = fc.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();

            amPath.setText(file.getAbsolutePath());
        }
    }
}
