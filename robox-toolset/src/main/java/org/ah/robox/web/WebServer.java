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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.ah.robox.ExtendedPrinterStatus;
import org.ah.robox.Main;
import org.ah.robox.comms.Printer;
import org.ah.robox.comms.PrinterDiscovery;
import org.ah.robox.web.UploadFile.Result;

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
    private int automaticRefresh = -1;
    private StatusManager statusManager;
    private ImageCache imageCache;

    public WebServer(PrinterDiscovery printerDiscovery) {
        this.printerDiscovery = printerDiscovery;
    }

    public void init() throws IOException {

        statusManager = new StatusManager(printerDiscovery, preferredPrinterId);
        statusManager.setInterval(refreshInterval);
        statusManager.setPostRefreshCommand(postRefreshCommand);
        if (refreshCommandFormat != null) {
            statusManager.setFormat(refreshCommandFormat);
        }
        statusManager.start();

        imageCache = imageCommand != null ? new ImageCache() : null;
        if (imageCache != null) {
            imageCache.setInterval(imageRefreshInterval);
            imageCache.setImageCommand(imageCommand);
        }

        address = new InetSocketAddress(port);

        if (Main.verboseFlag) {
            System.out.println("Starting web server at " + address);
        }
        server = HttpServer.create(address, 0);

        File staticFiles = null;
        if (staticDir != null) {
            staticFiles = new File(staticDir);
        }

        // TODO add other templates
        WebServer.MainHandler mainHandler = new WebServer.MainHandler(statusManager, templateFile, null, null, null, imageCache, staticFiles);
        server.createContext("/", mainHandler);
        server.setExecutor(Executors.newCachedThreadPool(new ThreadFactory() {
            public Thread newThread(Runnable runnable) {
                Thread thread = new Thread(runnable);
                thread.setDaemon(true);
                return thread;
            }
        }));
    }

    public void start() throws IOException {
        server.start();
    }

    public void stop() throws IOException {
        imageCache.stop();
        statusManager.stop();
    }

    /**
     *
     */
    public void stopAndWaitForStopped() throws IOException {
        stop();

        long now = System.currentTimeMillis();
        server.stop(3);
        while ((System.currentTimeMillis() - now < 3500)
                && (imageCache.isRunning() || statusManager.isRunning())) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException ignore) { }
        }
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

    public int getAutomaticRefrehs() {
        return automaticRefresh ;
    }

    public void setAutomaticRefresh(int automaticRefresh) {
        this.automaticRefresh = automaticRefresh;
    }


    private static enum ReturnContent {
        MAIN_BODY, CAPTURE_IMAGE, STATIC_FILE, NOT_FOUND, REDIRECT_TO_MAIN_BODY, REDIRECT_TO_ONE_PRINTER, LIST_PRINTERS, NO_PRINTERS
    }

    public class MainHandler implements HttpHandler {

        private static final long DAY = 1000 * 60 * 60 * 24;
        private Template mainResponse;
        private Template listPrintersResponse;
        private Template noPrintersResponse;
        private Template notFoundResponse;
        private Template exceptionResponse;
        private StatusManager statusManager;
        private ImageCache imageCache;
        private File staticFiles;

        public MainHandler(StatusManager status, File mainTemplateFile, File listPrintersTemplateFile,
                File noPrintersTemplateFile, File notFoundTemplateFile,
                ImageCache imageCache, File staticFiles) {
            this.statusManager = status;
            this.mainResponse = new Template(mainTemplateFile, "/index.html");
            this.listPrintersResponse = new Template(mainTemplateFile, "/list-printers.html");
            this.noPrintersResponse = new Template(mainTemplateFile, "/no-printers.html");
            this.notFoundResponse = new Template(mainTemplateFile, "/404.html");
            this.exceptionResponse = new Template(null, "/500.html");
            this.imageCache = imageCache;
            this.staticFiles = staticFiles;
        }

        public void handle(HttpExchange exchange) throws IOException {
            try {
                String method = exchange.getRequestMethod();
                String path = exchange.getRequestURI().getPath();

                ReturnContent returnContent = ReturnContent.MAIN_BODY;

                String errorMsg = "";
                String previousErrorMsg = "";
                Map<String, Cookie> requestCookies = CookiesUtility.getRequestCookies(exchange);
                if (requestCookies.containsKey("errorMsg")) {
                    previousErrorMsg = requestCookies.get("errorMsg").getValue();
                }
                File resourceFile = null;
                InputStream resourceInputStream = null;

                ExtendedPrinterStatus status = null;

                String printerId = null;
                int i = path.substring(1).indexOf('/');
                if (i > 0) {
                    printerId = path.substring(1, i + 1);
                    path = path.substring(i);
                    status = statusManager.getPrinterStatus(printerId);
                } else if (path.length() > 1 && !"/list".equals(path)) {
                    printerId = path.substring(1);
                    status = statusManager.getPrinterStatus(printerId);
                    if (status != null) {
                        path = "/";
                    } else {
                        printerId = null;
                    }
                }

                if ("GET".equals(method)) {
                    if (path.equals("/capture.jpg")) {
                        returnContent = ReturnContent.CAPTURE_IMAGE;
                    } else if (path.equals("/list")) {
                        returnContent = ReturnContent.LIST_PRINTERS;
                    } else if (printerId != null) {
                        if (status != null) {
                            if (path.equals("/") || path.equals("/index.html") || path.equals("index.htm")) {
                                returnContent = ReturnContent.MAIN_BODY;
                            }  else if (staticFiles != null) {
                                resourceFile = new File(staticFiles, path);
                                if (resourceFile.exists() && resourceFile.isFile()) {
                                    returnContent = ReturnContent.STATIC_FILE;
                                    resourceInputStream = new FileInputStream(resourceFile);
                                }
                            } else {
                                resourceInputStream = getClass().getResourceAsStream("/static_files" + path);
                                if (resourceInputStream != null) {
                                    returnContent = ReturnContent.STATIC_FILE;
                                }
                            }
                        } else {
                            returnContent = ReturnContent.MAIN_BODY;
                        }
                    } else if ("/".equals(path) || path.equals("/index.html") || path.equals("index.htm")) {
                        returnContent = ReturnContent.REDIRECT_TO_MAIN_BODY;
                    } else {
                        returnContent = ReturnContent.NOT_FOUND;
                    }
                } else if ("POST".equals(method)) {
                    if (path.equals("/upload")) {
                        Result result = UploadFile.uploadFile(exchange.getRequestBody());
                        if (result == Result.UPLOADED) {
                            errorMsg = "File successfully uploaded";
                        } else if (result == Result.BAD_INPUT) {
                            errorMsg = "Bad input";
                        } else if (result == Result.NO_FILENAME) {
                            errorMsg = "Cannot deduct filename";
                        } else if (result == Result.NOT_TEXT) {
                            errorMsg = "Cannot upload non text file";
                        }
                        if (printerId != null) {
                            returnContent = ReturnContent.REDIRECT_TO_MAIN_BODY;
                        } else {
                            returnContent = ReturnContent.REDIRECT_TO_ONE_PRINTER;
                        }
                    } else {
                        if (printerId != null) {
                            if (status != null && !status.getOverallPrinterStatus().isError()) {
                                if (allowCommandsFlag) {
                                    Printer printer = status.getPrinter();
                                    if (path.equals("/pause")) {
                                        printer.pausePrinter();
                                        // TODO read response
                                        errorMsg = "Sent pause command to the printer";
                                    } else if (path.equals("/resume")) {
                                        printer.resumePrinter();
                                        // TODO read response
                                        errorMsg = "Sent resume command to the printer";
                                    } else if (path.equals("/abort")) {
                                        printer.abortPrint();
                                        // TODO read response
                                        errorMsg = "Sent abort print command to the printer";
                                    }
                                } else {
                                    errorMsg = "Issuing commands to printer not allowed";
                                }
                                returnContent = ReturnContent.REDIRECT_TO_MAIN_BODY;
                            } else {
                                errorMsg = "Printer not available";
                                returnContent = ReturnContent.REDIRECT_TO_MAIN_BODY;
                            }
                        } else {
                            returnContent = ReturnContent.NOT_FOUND;
                        }
                    }
                }

                String templateErrorMsg = errorMsg;
                if ((errorMsg == null || "".equals(errorMsg))
                        && (previousErrorMsg != null && !"".equals(previousErrorMsg))) {
                    templateErrorMsg = previousErrorMsg;
                }

                if (returnContent == ReturnContent.MAIN_BODY) {

                    String body = mainResponse.getBody(WebServer.this, status, statusManager.getPrinters().size(), templateErrorMsg);

                    exchange.getResponseHeaders().add("Content-Length", Integer.toString(body.length()));
                    exchange.getResponseHeaders().add("Content-Type", "text/html");

                    // remove errorMsg cookie
                    CookiesUtility.addResponseCookies(exchange, new Cookie().withName("errorMsg").withValue(errorMsg).withExpires(new Date(0)));

                    exchange.sendResponseHeaders(200, body.length());
                    exchange.getResponseBody().write(body.getBytes());
                    exchange.getResponseBody().close();
                } else if (returnContent == ReturnContent.STATIC_FILE) {
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
                } else if (returnContent == ReturnContent.CAPTURE_IMAGE) {
                    if (imageCache != null) {
                        byte[] imageContent = imageCache.getImage();
                        if (imageContent != null) {
                            exchange.getResponseHeaders().add("Content-Length", Integer.toString(imageContent.length));
                            exchange.getResponseHeaders().add("Content-Type", "image/jpeg");
                            exchange.sendResponseHeaders(200, imageContent.length);
                            exchange.getResponseBody().write(imageContent);
                            exchange.getResponseBody().close();
                        } else {
                            returnContent = ReturnContent.NOT_FOUND;
                        }
                    } else {
                        returnContent = ReturnContent.NOT_FOUND;
                    }
                }

                if (returnContent == ReturnContent.REDIRECT_TO_MAIN_BODY) {
                    if (status == null) {
                        status = statusManager.selectOnePrinter();
                    }
                    if (status != null) {
                        printerId = status.getPrinter().getPrinterId();
                        String host = exchange.getRequestHeaders().getFirst("Host");
                        String locationUrl = "http://" + host + "/" + printerId;
                        if (errorMsg != null && !"".equals(errorMsg)) {
                            CookiesUtility.addResponseCookies(exchange, new Cookie()
                                        .withName("errorMsg").withValue(errorMsg)
                                        .withExpires(new Date(System.currentTimeMillis() + DAY * 2))); // two days in future as we're not calculating GMT time
                        }
                        exchange.getResponseHeaders().add("Location", locationUrl);
                        exchange.sendResponseHeaders(302, 0);
                        exchange.getResponseBody().close();
                    } else if (statusManager.getPrinters().size() > 0) {
                        returnContent = ReturnContent.LIST_PRINTERS;
                    } else {
                        returnContent = ReturnContent.NO_PRINTERS;
                    }
                }
                if (returnContent == ReturnContent.LIST_PRINTERS) {

                    StringBuilder list = new StringBuilder();

                    for (Printer printer : statusManager.getPrinters().values()) {
                        String id = printer.getPrinterId();
                        ExtendedPrinterStatus s = statusManager.getPrinterStatus(id);
                        if (s != null && !s.getOverallPrinterStatus().isError()) {
                            list.append("<li><a href=\"/" + id + "/\"><div class=\"printer\">" + printer.getPrinterName() + "</div></a></li>\n");
                        } else {
                            list.append("<li><div class=\"printer\">" + printer.getPrinterName() + "</div></li>\n");
                        }
                    }
                    Map<String, String> substitutions = new HashMap<String, String>();
                    substitutions.put("printers_list", list.toString());
                    String body = listPrintersResponse.getBody(substitutions);

                    exchange.getResponseHeaders().add("Content-Length", Integer.toString(body.length()));
                    exchange.getResponseHeaders().add("Content-Type", "text/html");

                    // remove errorMsg cookie
                    CookiesUtility.addResponseCookies(exchange, new Cookie().withName("errorMsg").withValue(errorMsg).withExpires(new Date(0)));

                    exchange.sendResponseHeaders(200, body.length());
                    exchange.getResponseBody().write(body.getBytes());
                    exchange.getResponseBody().close();

                }

                if (returnContent == ReturnContent.NO_PRINTERS) {
                    String body = noPrintersResponse.getBody(WebServer.this, status, statusManager.getPrinters().size(), templateErrorMsg);

                    exchange.getResponseHeaders().add("Content-Length", Integer.toString(body.length()));
                    exchange.getResponseHeaders().add("Content-Type", "text/html");

                    // remove errorMsg cookie
                    CookiesUtility.addResponseCookies(exchange, new Cookie().withName("errorMsg").withValue(errorMsg).withExpires(new Date(0)));

                    exchange.sendResponseHeaders(200, body.length());
                    exchange.getResponseBody().write(body.getBytes());
                    exchange.getResponseBody().close();
                }

                if (returnContent == ReturnContent.NOT_FOUND) {
                    String body = notFoundResponse.getBody(WebServer.this, status, statusManager.getPrinters().size(), exchange.getRequestURI().toString());

                    exchange.getResponseHeaders().add("Content-Length", Integer.toString(body.length()));
                    exchange.getResponseHeaders().add("Content-Type", "text/html");

                    // remove errorMsg cookie
                    CookiesUtility.addResponseCookies(exchange, new Cookie().withName("errorMsg").withValue(errorMsg).withExpires(new Date(0)));

                    exchange.sendResponseHeaders(404, body.length());
                    exchange.getResponseBody().write(body.getBytes());
                    exchange.getResponseBody().close();
                }
            } catch (Throwable t) {
                StringWriter error = new StringWriter();
                PrintWriter out = new PrintWriter(error);
                t.printStackTrace(out);

                String body = exceptionResponse.getBody(WebServer.this, null, statusManager.getPrinters().size(), error.toString());

                exchange.getResponseHeaders().add("Content-Length", Integer.toString(body.length()));
                exchange.getResponseHeaders().add("Content-Type", "text/html");

                // remove errorMsg cookie
                CookiesUtility.addResponseCookies(exchange, new Cookie().withName("errorMsg").withValue("").withExpires(new Date(0)));

                exchange.sendResponseHeaders(500, body.length());
                exchange.getResponseBody().write(body.getBytes());
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
