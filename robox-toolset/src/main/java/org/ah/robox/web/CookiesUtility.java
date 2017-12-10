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
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

/**
 *
 *
 * @author Daniel Sendula
 */
@SuppressWarnings("restriction")
public class CookiesUtility {

    public static final String RESPONSE_COOKIE_HEADER = "Set-Cookie";

    public static final String REQUEST_COOKIE_HEADER = "Cookie";

    /**
     * Adds response cookie
     * @param connection http connection
     * @param cookies cookie
     */
    public static void addResponseCookies(HttpExchange exchange, Cookie... cookies) {
        Headers responseHeaders = exchange.getResponseHeaders();

        for (Cookie cookie : cookies) {
            String cookieRepresentation = cookie.toString();
            responseHeaders.add(RESPONSE_COOKIE_HEADER, cookieRepresentation);
        }
    }

    /**
     * Obtains request cookies as a map
     * @param connection http connection
     * @return request cookies as a map
     */
    public static Map<String, Cookie> getRequestCookies(HttpExchange exchange) {
        Map<String, Cookie> cookies = new HashMap<String, Cookie>();

        List<String> headers = exchange.getRequestHeaders().get(REQUEST_COOKIE_HEADER);

        if (headers != null) {
            for (String header : exchange.getRequestHeaders().get(REQUEST_COOKIE_HEADER)) {
                StringTokenizer tokenizer = new StringTokenizer(header, ";");
                while (tokenizer.hasMoreTokens()) {
                    try {
                        String token = tokenizer.nextToken();
                        Cookie cookie = new Cookie(token.trim());
                        cookies.put(cookie.getName(), cookie);
                    } catch (ParseException ignore) {
                    }
                }
            }
        }
        return cookies;
    }

    public static class ParseException extends RuntimeException {

        private static final long serialVersionUID = -3893933615037565747L;

        public ParseException(String msg) {
            super(msg);
        }
    }
}
