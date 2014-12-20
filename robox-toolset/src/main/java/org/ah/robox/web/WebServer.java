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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;

import org.ah.robox.ExtendedPrinterStatus;
import org.ah.robox.Main;
import org.ah.robox.PrintStatusCommand.Estimate;
import org.ah.robox.PrintStatusCommand.EstimateState;
import org.ah.robox.WebCommand;
import org.ah.robox.comms.PrinterDiscovery;
import org.ah.robox.comms.response.PrinterStatusResponse;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 *
 *
 * @author Daniel Sendula
 */
@SuppressWarnings("restriction")
public class WebServer {

    private int refreshInterval = 15; // 15 seconds
    private String postRefreshCommand = null;
    private String refreshCommandFormat = "%c: %h:%m";
    private String imageCommand = "";
    private int imageRefreshInterval = 5; // 5 seconds
    private int port = 8080;
    private String staticDir = null;
    private File templateFile = null;
    private String preferredPrinterId = null;
    private InetSocketAddress address;
    private HttpServer server;
    private boolean allowCommandsFlag = false;
    private PrinterDiscovery printerDiscovery;

    public WebServer(PrinterDiscovery printerDiscovery) {
        this.printerDiscovery = printerDiscovery;
    }

    public void init() throws IOException {

        final StatusManager status = new StatusManager(printerDiscovery, preferredPrinterId);
        status.setInterval(refreshInterval);
        status.setPostRefreshCommand(postRefreshCommand);
        if (refreshCommandFormat != null) {
            status.setFormat(refreshCommandFormat);
        }
        status.start();

        Image image = imageCommand != null ? new Image() : null;
        if (image != null) {
            image.setInterval(imageRefreshInterval);
            image.setImageCommand(imageCommand);
        }

        InetSocketAddress address = new InetSocketAddress(port);

        if (Main.verboseFlag) {
            System.out.println("Starting web server at " + address);
        }
        server = HttpServer.create(address, 0);

        File staticFiles = null;
        if (staticDir != null) {
            staticFiles = new File(staticDir);
        }

        WebServer.MainHandler mainHandler = new WebServer.MainHandler(status, templateFile, image, staticFiles);
        server.createContext("/", mainHandler);
        server.setExecutor(null); // creates a default executor
    }

    public void start() throws IOException {
        server.start();
    }

    public int getRefreshInterval() {
        return refreshInterval;
    }

    public void setRefreshInterval(int refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    public String getPostRefreshCommand() {
        return postRefreshCommand;
    }

    public void setPostRefreshCommand(String postRefreshCommand) {
        this.postRefreshCommand = postRefreshCommand;
    }

    public String getRefreshCommandFormat() {
        return refreshCommandFormat;
    }

    public void setRefreshCommandFormat(String refreshCommandFormat) {
        this.refreshCommandFormat = refreshCommandFormat;
    }

    public String getImageCommand() {
        return imageCommand;
    }

    public void setImageCommand(String imageCommand) {
        this.imageCommand = imageCommand;
    }

    public int getImageRefreshInterval() {
        return imageRefreshInterval;
    }

    public void setImageRefreshInterval(int imageRefreshInterval) {
        this.imageRefreshInterval = imageRefreshInterval;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getStaticDir() {
        return staticDir;
    }

    public void setStaticDir(String staticDir) {
        this.staticDir = staticDir;
    }

    public File getTemplateFile() {
        return templateFile;
    }

    public void setTemplateFile(File templateFile) {
        this.templateFile = templateFile;
    }

    public String getPreferredPrinterId() {
        return preferredPrinterId;
    }

    public void setPreferredPrinterId(String printerId) {
        this.preferredPrinterId = printerId;
    }

    public InetSocketAddress getAddress() {
        return address;
    }

    public void setAddress(InetSocketAddress address) {
        this.address = address;
    }

    public boolean isAllowCommandsFlag() {
        return allowCommandsFlag;
    }

    public void setAllowCommandsFlag(boolean allowCommandsFlag) {
        this.allowCommandsFlag = allowCommandsFlag;
    }

    /**
     * @param templateFile
     * @return
     */
    public static String loadMainResponseTemplate(File templateFile) throws IOException {
        StringBuilder res = new StringBuilder();
        InputStream is;
        if (templateFile == null) {
            is = WebCommand.class.getResourceAsStream("/index.html");
        } else {
            is = new FileInputStream(templateFile);
        }
        try {
            byte[] buf = new byte[10240];
            int r = is.read(buf);
            while (r > 0) {
                res.append(new String(buf, 0, r));
                r = is.read(buf);
            }
        } finally {
            is.close();
        }
        return res.toString();
    }

    public static String templateSubstitution(String page, ExtendedPrinterStatus status) {
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

                    if ("status".equals(var)) {
                        if (status != null) {
                            res.append(status.getOverallPrinterStatus().getText());
                        } else {
                            res.append("No printers detected.");
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
                                        + "<form action=\"/upload\"> enctype=\"multipart/form-data\" method=\"post\">"
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

    public static class MainHandler implements HttpHandler {

        private File templateFile;
        private String mainResponse;
        private long mainResponseLastChanged = 0;
        private StatusManager statusManager;
        private Image image;
        private File staticFiles;

        public MainHandler(StatusManager status, File templateFile, Image image, File staticFiles) {
            this.statusManager = status;
            this.templateFile = templateFile;
            this.image = image;
            this.staticFiles = staticFiles;
        }

        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            int returnContent = WebCommand.MAIN_BODY;

            File resourceFile = null;
            InputStream resourceInputStream = null;

            if ("GET".equals(method)) {
                if (path.equals("/capture.jpg")) {
                    returnContent = WebCommand.CAPTURE_IMAGE;
                } else if (path.equals("/") || path.equals("/index.html") || path.equals("index.htm")) {
                    returnContent = WebCommand.MAIN_BODY;
                } else if (staticFiles != null) {
                    resourceFile = new File(staticFiles, path.substring(1));
                    if (resourceFile.exists() && resourceFile.isFile()) {
                        returnContent = WebCommand.STATIC_FILE;
                        resourceInputStream = new FileInputStream(resourceFile);
                    }
                } else {
                    resourceInputStream = getClass().getResourceAsStream("/static_files" + path);
                    if (resourceInputStream != null) {
                        returnContent = WebCommand.STATIC_FILE;
                    }
                }
            } else if ("POST".equals(method)) {
                if (path.equals("/pause")) {
                    // pause printer
                } else if (path.equals("/resume")) {

                } else if (path.equals("/abort")) {

                }
            }

            if (returnContent == WebCommand.MAIN_BODY) {
                ExtendedPrinterStatus status = statusManager.selectOnePrinter();
                // TODO - detect no/one/more printers

                if ((templateFile != null && templateFile.lastModified() != mainResponseLastChanged)
                        || mainResponse == null) {
                    mainResponse = WebServer.loadMainResponseTemplate(templateFile);
                    if (templateFile != null) {
                        mainResponseLastChanged = templateFile.lastModified();
                    }
                }

                String body = WebServer.templateSubstitution(mainResponse, status);
                exchange.getResponseHeaders().add("Content-Length", Integer.toString(body.length()));
                exchange.getResponseHeaders().add("Content-Type", "text/html");
                exchange.sendResponseHeaders(200, body.length());
                exchange.getResponseBody().write(body.getBytes());
                exchange.getResponseBody().close();
            } else if (returnContent == WebCommand.STATIC_FILE) {
                exchange.getResponseHeaders().add("Content-Type", contentTypeFromFileName(path));
                if (resourceFile != null) {
                    exchange.getResponseHeaders().add("Content-Length", Long.toString(resourceFile.length()));
                    exchange.sendResponseHeaders(200, resourceFile.length());
                } else {
                    exchange.sendResponseHeaders(200, 0);
                }
                byte[] buf = new byte[10240];
                int r = resourceInputStream.read(buf);
                while (r > 0) {
                    exchange.getResponseBody().write(buf, 0, r);
                    r = resourceInputStream.read(buf);
                }
                exchange.getResponseBody().close();
            } else if (returnContent == WebCommand.CAPTURE_IMAGE) {
                if (image != null) {
                    byte[] imageContent = image.getImage();
                    if (imageContent != null) {
                        exchange.getResponseHeaders().add("Content-Length", Integer.toString(imageContent.length));
                        exchange.getResponseHeaders().add("Content-Type", "image/jpeg");
                        exchange.sendResponseHeaders(200, imageContent.length);
                        exchange.getResponseBody().write(imageContent);
                        exchange.getResponseBody().close();
                    } else {
                        returnContent = WebCommand.NOT_FOUND;
                    }
                } else {
                    returnContent = WebCommand.NOT_FOUND;
                }
            }
            if (returnContent == WebCommand.NOT_FOUND) {
                String response = "<html><body>Resource not found</body></html>";
                exchange.getResponseHeaders().add("Content-Length", Integer.toString(response.length()));
                exchange.getResponseHeaders().add("Content-Type", "text/html");
                exchange.sendResponseHeaders(200, response.length());
                exchange.getResponseBody().write(response.getBytes());
                exchange.getResponseBody().close();
            }
        }

        /**
         * @param path
         * @return
         */
        private String contentTypeFromFileName(String path) {
            if (path.endsWith(".txt") || path.endsWith(".")) {
                return "text/plain";
            } else if (path.endsWith(".html") || path.endsWith(".htm")) {
                return "text/html";
            } else if (path.endsWith(".css")) {
                return "text/css";
            } else if (path.endsWith(".jpg") || path.endsWith(".jpeg")) {
                return "image/jpeg";
            } else if (path.endsWith(".png")) {
                return "image/png";
            } else if (path.endsWith(".gif")) {
                return "image/gif";
            } else if (path.endsWith(".gif")) {
                return "image/gif";
            } else if (path.endsWith(".bmp")) {
                return "image/bmp";
            } else if (path.endsWith(".js")) {
                return "application/javascript";
            }
            return "application/octet-stream";
        }
    }
}
