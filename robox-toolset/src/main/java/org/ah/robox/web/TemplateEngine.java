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
package org.ah.robox.web;

import java.util.HashMap;
import java.util.Map;

import org.ah.robox.ExtendedPrinterStatus;
import org.ah.robox.PrintStatusCommand.Estimate;
import org.ah.robox.PrintStatusCommand.EstimateState;
import org.ah.robox.comms.response.PrinterStatusResponse;

/**
 *
 *
 * @author Daniel Sendula
 */
public class TemplateEngine {

    private TemplateEngine() { }

    public static String template(String page, WebServer webServer, ExtendedPrinterStatus status, int printerNumber, String errorMsg) {

        PrinterStatusResponse ps = status != null ? status.getPrinterStatus() : null;
        Estimate estimate = status != null ? status.getEstimate() : null;

        Map<String, String> substitutions = new HashMap<String, String>();

        substitutions.put("error_msg", errorMsg);
        substitutions.put("status", status != null
                ? status.getOverallPrinterStatus().getText()
                : "Printer not found.");
        substitutions.put("busy", ps != null ? Boolean.toString(ps.isBusy()) : "");
        substitutions.put("job_id", ps != null && ps.getPrintJob() != null ? ps.getPrintJob() : "");
        String e = "";
        if (estimate != null) {
            e = estimate.toString();
            if (estimate.getPrintStatus() == EstimateState.IDLE) {
                e = "No print in progress";
            } else if (estimate.getPrintStatus() == EstimateState.NO_LINES) {
                e = "No lines submitted. Select a file: "
                        + "<form action=\"/upload\" enctype=\"multipart/form-data\" method=\"post\">"
                        + "<input type=\"file\" name=\"file\"><input type=\"submit\" value=\"Send\"></form>";
            } else if (estimate.getPrintStatus() == EstimateState.PREPARING) {
                e = "Too early to make estimate. Try again in a few moments.";
            }
        }

        boolean hasTotalLines = estimate != null && estimate.getTotalLines() >= 0;
        String currentLines = ps != null ? Integer.toString(ps.getLineNumber()) : "";
        String totalLines = hasTotalLines ? Integer.toString(estimate.getTotalLines()) :  "unknown";

        if (printerNumber > 1) {
            substitutions.put("all_printers_link", "<a class=\"all_printers\" href=\"/\">Show All Printers</a>");
        } else {
            substitutions.put("all_printers_link", "");
        }

        substitutions.put("estimate", e);
        substitutions.put("estimate_hours", estimate != null ? estimate.getHours() : "");
        substitutions.put("estimate_mins", estimate != null ? estimate.getMinutes() : "");
        substitutions.put("estimate_secs", estimate != null ? estimate.getSeconds() : "");
        substitutions.put("current_line", currentLines);
        substitutions.put("total_lines", totalLines);
        substitutions.put("current_line_and_total_line", currentLines + (hasTotalLines ? "&nbsp;/&nbsp;" + totalLines : ""));
        substitutions.put("capture_image_css_display", webServer.getImageCommand() != null && !"".equals(webServer.getImageCommand())
                ? "inline" : "none");
        substitutions.put("capture_image_tag", webServer.getImageCommand() != null && !"".equals(webServer.getImageCommand())
                ? "<img src=\"/capture.jpg\"/>" : "");
        substitutions.put("commands_css_display", webServer.isAllowCommandsFlag() ? "inline" : "none");
        substitutions.put("automatic-refresh", webServer.getAutomaticRefrehs() >= 0 ? "<meta http-equiv=\"refresh\" content=\"" + webServer.getAutomaticRefrehs() + "\" >" : "");
        return template(page, substitutions);
    }

    public static String template2(String page, WebServer webServer, ExtendedPrinterStatus status, String errorMsg) {
        StringBuilder res = new StringBuilder();

        int ptr = 0;

        PrinterStatusResponse ps = status != null ? status.getPrinterStatus() : null;
        Estimate estimate = status != null ? status.getEstimate() : null;

        int i = page.indexOf("${", ptr);
        while (i >= 0) {
            res.append(page.substring(ptr, i));
            int j = page.indexOf("}", i);
            if (j >= 0) {
                String var = page.substring(i + 2, j);
                ptr = j + 1;

                if ("error_msg".equals(var)) {
                    res.append(errorMsg);
                } else if ("status".equals(var)) {
                    if (status != null) {
                        res.append(status.getOverallPrinterStatus().getText());
                    } else {
                        res.append("Printer not found.");
                    }
                } else if ("busy".equals(var)) {
                    if (ps != null) {
                        res.append(ps.isBusy());
                    } else {
                        res.append("");
                    }
                } else if ("job_id".equals(var)) {
                    if (ps != null) {
                        res.append(ps.getPrintJob());
                    } else {
                        res.append("");
                    }
                } else if ("estimate".equals(var)) {
                    if (estimate != null) {
                        String e = estimate.toString();
                        if (estimate.getPrintStatus() == EstimateState.IDLE) {
                            e = "No print in progress";
                        } else if (estimate.getPrintStatus() == EstimateState.NO_LINES) {
                            e = "No lines submitted. Select a file: "
                                    + "<form action=\"/upload\" enctype=\"multipart/form-data\" method=\"post\">"
                                    + "<input type=\"file\" name=\"file\"><input type=\"submit\" value=\"Send\"></form>";
                        } else if (estimate.getPrintStatus() == EstimateState.PREPARING) {
                            e = "Too early to make estimate. Try again in a few moments.";
                        }
                        res.append(e);
                    } else {
                        res.append("");
                    }
                } else if ("estimate_hours".equals(var)) {
                    if (estimate != null) {
                        res.append(estimate.getHours());
                    } else {
                        res.append("");
                    }
                } else if ("estimate_mins".equals(var)) {
                    if (estimate != null) {
                        res.append(estimate.getMinutes());
                    } else {
                        res.append("");
                    }
                } else if ("estimate_secs".equals(var)) {
                    if (estimate != null) {
                        res.append(estimate.getSeconds());
                    } else {
                        res.append("");
                    }
                } else if ("current_line".equals(var)) {
                    if (ps != null) {
                        res.append(ps.getLineNumber());
                    } else {
                        res.append("");
                    }
                } else if ("total_lines".equals(var)) {
                    if (estimate != null && estimate.getTotalLines() >= 0) {
                        res.append(estimate.getTotalLines());
                    } else {
                        res.append("unknown");
                    }
                } else if ("capture_image_css_display".equals(var)) {
                    if (webServer.getImageCommand() != null && !"".equals(webServer.getImageCommand())) {
                        res.append("inline");
                    } else {
                        res.append("none");
                    }
                } else if ("commands_css_display".equals(var)) {
                    if (webServer.isAllowCommandsFlag()) {
                        res.append("inline");
                    } else {
                        res.append("none");
                    }
//                } else if ("".equals(var)) {
                } else {
                    res.append("UNKNOWN SUBSTITUTION VARIABLE \"" + var + "\"");
                }
                i = page.indexOf("${", ptr);
            } else {
                i = -1;
            }
        }
        if (ptr < page.length()) {
            res.append(page.substring(ptr));
        }

        return res.toString();
    }

    public static String template(String page, Map<String, String> substitutions) {
        StringBuilder res = new StringBuilder();

        int ptr = 0;

        int i = page.indexOf("${", ptr);
        while (i >= 0) {
            res.append(page.substring(ptr, i));
            int j = page.indexOf("}", i);
            if (j >= 0) {
                String var = page.substring(i + 2, j);
                ptr = j + 1;

                String substitution = substitutions.get(var);
                if (substitution == null) {
                    res.append("UNKNOWN SUBSTITUTION VARIABLE \"" + var + "\"");
                } else {
                    res.append(substitution);
                }
                i = page.indexOf("${", ptr);
            } else {
                i = -1;
            }
        }
        if (ptr < page.length()) {
            res.append(page.substring(ptr));
        }

        return res.toString();
    }

}
