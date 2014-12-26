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
import java.util.Map;

import org.ah.robox.ExtendedPrinterStatus;
import org.ah.robox.WebCommand;

/**
 *
 *
 * @author Daniel Sendula
 */
public class Template {

    private File templateFile;
    private String resourceName;
    private long responseLastChanged;
    private String response;

    public Template(File templateFile, String resourceName) {
        this.templateFile = templateFile;
        this.resourceName = resourceName;
    }

    public String getBody(WebServer webServer, ExtendedPrinterStatus status, int printerNumber, String error) throws IOException {
        if ((templateFile != null && templateFile.lastModified() != responseLastChanged)
                || response == null) {
            response = loadMainResponseTemplate();
            if (templateFile != null) {
                responseLastChanged = templateFile.lastModified();
            }
        }

        String body = TemplateEngine.template(response, webServer, status, printerNumber, error);

        return body;
    }

    public String getBody(Map<String, String> substitutions) throws IOException {
        if ((templateFile != null && templateFile.lastModified() != responseLastChanged)
                || response == null) {
            response = loadMainResponseTemplate();
            if (templateFile != null) {
                responseLastChanged = templateFile.lastModified();
            }
        }

        String body = TemplateEngine.template(response, substitutions);

        return body;
    }

    private String loadMainResponseTemplate() throws IOException {
        StringBuilder res = new StringBuilder();
        InputStream is;
        if (templateFile == null) {
            is = WebCommand.class.getResourceAsStream(resourceName);
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

}
