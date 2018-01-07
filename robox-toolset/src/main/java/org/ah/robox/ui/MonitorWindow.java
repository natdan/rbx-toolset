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

package org.ah.robox.ui;

import static javax.swing.SpringLayout.EAST;
import static javax.swing.SpringLayout.NORTH;
import static javax.swing.SpringLayout.SOUTH;
import static javax.swing.SpringLayout.WEST;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.font.TextAttribute;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.Timer;

import org.ah.robox.comms.response.PrinterPause;

public class MonitorWindow extends JFrame {

    private boolean uploading;
    private JPanel mainPanel;
    private JButton abortButton;
    private JButton pauseResumeButton;
    private JLabel uploadingLabel;
    private JProgressBar uploadingProgress;
    private JLabel linesLabel;
    private JProgressBar linesProgress;

    private Runnable abortUploading;
    private Runnable abortPrinting;
    private Runnable pausePrinting;

    private JTextField targetBedTemp;
    private JLabel bedTemp;
    private JTextField targetHead0Temp;
    private JLabel head0Temp;
    private JTextField targetHead1Temp;
    private JLabel head1Temp;
    private JLabel jobIdLabel;
    private JLabel statusText;
    private JLabel uploadingFileLabel;
    private JLabel uploadingFileText;
    private Timer postponeSavePosition;

    public MonitorWindow() {
        super("Robox Slicer Extension");

        setSize(640, 240);
        setLeaveAction(() -> System.exit(0));
        loadPositionAndSize();

        postponeSavePosition = new Timer(3000, e -> savePositionAndSize());
        postponeSavePosition.stop();
        postponeSavePosition.setCoalesce(true);
        postponeSavePosition.setRepeats(false);

        addComponentListener(new ComponentListener() {

            @Override public void componentShown(ComponentEvent e) {
                postponeSavePosition.stop();
            }

            @Override public void componentResized(ComponentEvent e) {
                positionAndSizeChanged();
            }

            @Override public void componentMoved(ComponentEvent e) {
                positionAndSizeChanged();
            }

            @Override public void componentHidden(ComponentEvent e) {
                postponeSavePosition.stop();
            }
        });

        createMainPanel();
    }

    private synchronized void positionAndSizeChanged() {
        postponeSavePosition.stop();
        postponeSavePosition.start();
    }

    private void savePositionAndSize() {
        File homeDir = new File(System.getProperty("user.home"));
        File confDir = new File(homeDir, ".robox");
        if (!confDir.exists()) {
            if (!confDir.mkdirs()) {
                // Cannot create conf dir
                return;
            }
        }
        File confFile = new File(confDir, "window.conf");

        try {
            FileWriter fileWriter = new FileWriter(confFile);
            try {
                fileWriter.write("x = " + getX() + "\n");
                fileWriter.write("y = " + getY() + "\n");
                fileWriter.write("width = " + getWidth() + "\n");
                fileWriter.write("height = " + getHeight() + "\n");
            } finally {
                fileWriter.close();
            }
        } catch (IOException ignore) {}
    }

    private void loadPositionAndSize() {
        File homeDir = new File(System.getProperty("user.home"));
        File confDir = new File(homeDir, ".robox");
        if (!confDir.exists()) {
            if (!confDir.mkdirs()) {
                // Cannot create conf dir
                return;
            }
        }

        File confFile = new File(confDir, "window.conf");
        if (confFile.exists()) {
            try {
                Properties properties = new Properties();
                FileInputStream inputStream = new FileInputStream(confFile);
                try {
                    properties.load(inputStream);

                    int x = getX();
                    int y = getY();
                    int w = getWidth();
                    int h = getHeight();
                    if (properties.containsKey("x")) { try { x = Integer.parseInt(properties.getProperty("x")); } catch (NumberFormatException ignore) {} }
                    if (properties.containsKey("y")) { try { y = Integer.parseInt(properties.getProperty("y")); } catch (NumberFormatException ignore) {} }
                    if (properties.containsKey("width")) { try { w = Integer.parseInt(properties.getProperty("width")); } catch (NumberFormatException ignore) {} }
                    if (properties.containsKey("height")) { try { h = Integer.parseInt(properties.getProperty("height")); } catch (NumberFormatException ignore) {} }
                    setBounds(x, y, w, h);
                } finally {
                    inputStream.close();
                }
            } catch (IOException ignore) {}
        }
    }

    public void setLeaveAction(Runnable action) {
//        cancelButton.addActionListener(e -> action.run());
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

    private void createMainPanel() {
        mainPanel = new JPanel(new BorderLayout());
        SpringLayout layout = new SpringLayout();
        mainPanel.setLayout(layout);

        JLabel jobLabel = new JLabel("Job:");
        jobIdLabel = new JLabel("");
        jobIdLabel.setMinimumSize(new Dimension(120, 10));
        Font boldFont = jobIdLabel.getFont().deriveFont(Collections.singletonMap(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD));
        jobIdLabel.setFont(boldFont);

        JLabel statusLabel = new JLabel("Status: ");
        statusText = new JLabel("");
        statusText.setFont(boldFont);

        mainPanel.add(jobLabel);
        mainPanel.add(jobIdLabel);
        mainPanel.add(statusLabel);
        mainPanel.add(statusText);

        JLabel targetLabel = new JLabel("Target");
        JLabel actualLabel = new JLabel("Actual");
        targetLabel.setMinimumSize(new Dimension(100, 10));
        targetLabel.setPreferredSize(targetLabel.getMinimumSize());
        actualLabel.setMinimumSize(new Dimension(70, 10));
        actualLabel.setPreferredSize(actualLabel.getMinimumSize());

        mainPanel.add(targetLabel);
        mainPanel.add(actualLabel);

        JLabel bedLabel = new JLabel("Bed");
        JLabel head0Label = new JLabel("Head 0");
        JLabel head1Label = new JLabel("Head 1");
        mainPanel.add(bedLabel);
        mainPanel.add(head0Label);
        mainPanel.add(head1Label);

        targetBedTemp = new JTextField("");
        targetHead0Temp = new JTextField("");
        targetHead1Temp = new JTextField("");

        bedTemp = new JLabel();
        head0Temp = new JLabel();
        head1Temp = new JLabel();
        mainPanel.add(targetBedTemp);
        mainPanel.add(targetHead0Temp);
        mainPanel.add(targetHead1Temp);
        mainPanel.add(bedTemp);
        mainPanel.add(head0Temp);
        mainPanel.add(head1Temp);

        createButtons();

        linesLabel = new JLabel("Progress");
        linesProgress = new JProgressBar(JProgressBar.HORIZONTAL);
        linesProgress.setMinimumSize(new Dimension(0, 20));

        mainPanel.add(linesLabel);
        mainPanel.add(linesProgress);

        uploadingLabel = new JLabel("Uploading");
        uploadingFileLabel = new JLabel("File: ");
        uploadingFileText = new JLabel("");
        uploadingFileText.setFont(boldFont);
        uploadingProgress = new JProgressBar(JProgressBar.HORIZONTAL);
        uploadingProgress.setPreferredSize(uploadingProgress.getMinimumSize());

        mainPanel.add(uploadingLabel);
        mainPanel.add(uploadingFileLabel);
        mainPanel.add(uploadingFileText);
        mainPanel.add(uploadingProgress);

        layout.putConstraint(NORTH, jobLabel, 5, NORTH, mainPanel);
        layout.putConstraint(WEST, jobLabel, 5, WEST, mainPanel);

        layout.putConstraint(NORTH, jobIdLabel, 0, NORTH, jobLabel);
        layout.putConstraint(WEST, jobIdLabel, 5, EAST, jobLabel);

        layout.putConstraint(NORTH, statusLabel, 0, NORTH, jobLabel);
        layout.putConstraint(WEST, statusLabel, 5, EAST, jobIdLabel);

        layout.putConstraint(NORTH, statusText, 0, NORTH, jobLabel);
        layout.putConstraint(WEST, statusText, 5, EAST, statusLabel);

        layout.putConstraint(NORTH, targetLabel, 5, NORTH, mainPanel);
        layout.putConstraint(EAST, targetLabel, -5, EAST, mainPanel);

        layout.putConstraint(NORTH, actualLabel, 5, NORTH, mainPanel);
        layout.putConstraint(EAST, actualLabel, -5, WEST, targetLabel);

        layout.putConstraint(NORTH, bedLabel, 5, SOUTH, actualLabel);

        layout.putConstraint(NORTH, targetBedTemp, 5, SOUTH, targetLabel);
        layout.putConstraint(EAST, targetBedTemp, 0, EAST, targetLabel);
        layout.putConstraint(WEST, targetBedTemp, 0, WEST, targetLabel);

        layout.putConstraint(NORTH, targetHead0Temp, 0, NORTH, head0Temp);
        layout.putConstraint(EAST, targetHead0Temp, 0, EAST, targetLabel);
        layout.putConstraint(WEST, targetHead0Temp, 0, WEST, targetLabel);

        layout.putConstraint(NORTH, targetHead1Temp, 0, NORTH, head1Temp);
        layout.putConstraint(EAST, targetHead1Temp, 0, EAST, targetLabel);
        layout.putConstraint(WEST, targetHead1Temp, 0, WEST, targetLabel);


        layout.putConstraint(NORTH, bedTemp, 5, NORTH, targetBedTemp);
        layout.putConstraint(EAST, bedTemp, 0, EAST, actualLabel);
        layout.putConstraint(WEST, bedTemp, 0, WEST, actualLabel);

        layout.putConstraint(NORTH, head0Temp, 5, SOUTH, bedTemp);
        layout.putConstraint(EAST, head0Temp, 0, EAST, actualLabel);
        layout.putConstraint(WEST, head0Temp, 0, WEST, actualLabel);

        layout.putConstraint(NORTH, head1Temp, 5, SOUTH, head0Temp);
        layout.putConstraint(EAST, head1Temp, 0, EAST, actualLabel);
        layout.putConstraint(WEST, head1Temp, 0, WEST, actualLabel);


        layout.putConstraint(NORTH, bedLabel, 0, NORTH, bedTemp);
        layout.putConstraint(NORTH, head0Label, 0, NORTH, head0Temp);
        layout.putConstraint(NORTH, head1Label, 0, NORTH, head1Temp);

        layout.putConstraint(EAST, head0Label, -15, WEST, actualLabel);
        layout.putConstraint(WEST, head1Label, 0, WEST, head0Label);
        layout.putConstraint(WEST, bedLabel, 0, WEST, head0Label);


        layout.putConstraint(NORTH, linesLabel, 15, SOUTH, jobLabel);
        layout.putConstraint(WEST, linesLabel, 5, WEST, mainPanel);

        layout.putConstraint(NORTH, linesProgress, 5, SOUTH, linesLabel);
        layout.putConstraint(WEST, linesProgress, 5, WEST, mainPanel);
        layout.putConstraint(EAST, linesProgress, -30, WEST, head0Label);

        layout.putConstraint(NORTH, uploadingLabel, 5, SOUTH, linesProgress);
        layout.putConstraint(WEST, uploadingLabel, 5, WEST, mainPanel);

        layout.putConstraint(NORTH, uploadingFileLabel, 0, NORTH, uploadingLabel);
        layout.putConstraint(WEST, uploadingFileLabel, 15, EAST, uploadingLabel);

        layout.putConstraint(NORTH, uploadingFileText, 0, NORTH, uploadingFileLabel);
        layout.putConstraint(WEST, uploadingFileText, 5, EAST, uploadingFileLabel);

        layout.putConstraint(NORTH, uploadingProgress, 5, SOUTH, uploadingLabel);
        layout.putConstraint(WEST, uploadingProgress, 5, WEST, mainPanel);
        layout.putConstraint(EAST, uploadingProgress, -30, WEST, head0Label);


        layout.putConstraint(SOUTH, abortButton, -5, SOUTH, mainPanel);
        layout.putConstraint(EAST, abortButton, -5, EAST, mainPanel);

        layout.putConstraint(SOUTH, pauseResumeButton, -5, SOUTH, mainPanel);
        layout.putConstraint(EAST, pauseResumeButton, -5, WEST, abortButton);

        add(mainPanel, BorderLayout.CENTER);
    }

    private void createButtons() {

        pauseResumeButton = new JButton("Pause");
        pauseResumeButton.addActionListener(e -> pauseAction());
        setStatus(null);

        abortButton = new JButton("Abort");
        abortButton.addActionListener(e -> abortAction());

        mainPanel.add(pauseResumeButton);
        mainPanel.add(abortButton);
    }

    public void setStatus(PrinterPause status) {
        if (status == null) {
            statusText.setText("---");
        } else {
            statusText.setText(status.getText());
        }
        if (status == null) {
            pauseResumeButton.setText("Pause");
            pauseResumeButton.setEnabled(false);
        } else if (status == PrinterPause.IDLE) {
            pauseResumeButton.setText("Pause");
            pauseResumeButton.setEnabled(false);
        } else if (status == PrinterPause.WORKING) {
            pauseResumeButton.setText("Pause");
            pauseResumeButton.setEnabled(true);
        } else if (status == PrinterPause.PAUSED) {
            pauseResumeButton.setText("Resume");
            pauseResumeButton.setEnabled(true);
        } else if (status == PrinterPause.PAUSING) {
            pauseResumeButton.setText("Pausing");
            pauseResumeButton.setEnabled(false);
        } else if (status == PrinterPause.RESUMING) {
            pauseResumeButton.setText("Resuming");
            pauseResumeButton.setEnabled(false);
        }
    }

    public void setUploadFileName(String uploadFileName) {
        this.uploadingFileText.setText(uploadFileName);
    }

    public void setUploading(boolean uploading) {
        this.uploading = uploading;
//        uploadingLabel.setVisible(uploading);
//        uploadingProgress.setVisible(uploading);
//        uploadingFileLabel.setVisible(uploading);
//        uploadingFileText.setVisible(uploading);
    }

    public void setUploadTotal(long length) {
        uploadingProgress.setMaximum((int)length);
    }

    public void setUploadProgress(int totalBytes) {
        uploadingProgress.setValue(totalBytes);
    }

    public void setPrintingTotal(int progress) {
        linesProgress.setMaximum(progress);
    }

    public void setPrintingProgress(int progress) {
        linesProgress.setValue(progress);
    }

    private void abortAction() {
        if (uploading && abortUploading != null) {
            abortUploading.run();
        }
        if (abortPrinting != null) {
            abortPrinting.run();
        }
    }

    private void pauseAction() {
        if (pausePrinting != null) {
            pausePrinting.run();
        }
    }

    public void setAbortUploadingAction(Runnable abortPrinting) {
        this.abortPrinting = abortPrinting;
    }

    public void setAbortPrintingAction(Runnable abortUploading) {
        this.abortUploading = abortUploading;
    }

    public void setPausePrintingAction(Runnable pausePrinting) {
        this.pausePrinting = pausePrinting;
    }

    public void setPrinting(boolean printing) {
        if (printing) {
            pauseResumeButton.setEnabled(true);
        } else {
            pauseResumeButton.setEnabled(false);
        }
    }

    public void setExceptionError(String string, IOException e) {
        // TODO Auto-generated method stub

    }

    public void setJobId(String jobId) {
        jobIdLabel.setText(jobId);
    }

    public void setBedTemperature(String bedTemperature) {
        this.bedTemp.setText(bedTemperature);
    }

    public void setHead0Temperature(String nozzle0Temperature) {
        this.head0Temp.setText(nozzle0Temperature);
    }

    public void setHead1Temperature(String nozzle1Temperature) {
        this.head1Temp.setText(nozzle1Temperature);
    }

    public void setBedSetTemperature(String bedSetTemperature) {
        this.targetBedTemp.setText(bedSetTemperature);
    }

    public void setHead0SetTemperature(String nozzle0SetTemperature) {
        this.targetHead0Temp.setText(nozzle0SetTemperature);
    }

    public void setHead1SetTemperature(String nozzle1SetTemperature) {
        this.targetHead1Temp.setText(nozzle1SetTemperature);
    }
}
