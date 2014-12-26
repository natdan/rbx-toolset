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

import java.text.SimpleDateFormat;
import java.util.Date;

import org.ah.robox.web.CookiesUtility.ParseException;

/**
 *
 *
 * @author Daniel Sendula
 */
public class Cookie {

    /** Format of the cookie date */
    public static final SimpleDateFormat cookieFormat = new SimpleDateFormat("EEE dd-MMM-yyyy HH:mm:ss");

    /** Cookie name */
    protected String name;

    /** Cookie value */
    protected String value;

    /** Date cookie is going expire */
    protected Date expires;

    /** Cookie domain */
    protected String domain;

    /** Cookie path */
    protected String path;

    /** Is cookie secure */
    protected boolean secure = false;

    /**
     * Creates new cookie to be sent to the browser
     */
    public Cookie() {
    }

    /**
     * Creates cooke from the request headers
     * @param header request header
     * @throws ParseException thrown if &quot;=&quot; is missing
     */
    public Cookie(String header) throws ParseException {
        parseCookie(header);
    }

    /**
     * Returns cookie's domain
     * @return the domain
     */
    public String getDomain() {
        return domain;
    }

    /**
     * Sets cookie's domain
     * @param domain the domain to set
     */
    public void setDomain(String domain) {
        this.domain = domain;
    }

    /**
     * Returns expire date of the cookie
     * @return the expires
     */
    public Date getExpires() {
        return expires;
    }

    /**
     * Sets expire date of the cookie
     * @param expires the expires to set
     */
    public void setExpires(Date expires) {
        this.expires = expires;
    }

    /**
     * Returns cookie name
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets cookie name
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Return cookie's path
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * Sets cookie's path
     * @param path the path to set
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Returns is cookie is secured
     * @return the secure
     */
    public boolean isSecure() {
        return secure;
    }

    /**
     * Sets is cookie secure
     * @param secure the secure to set
     */
    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    /**
     * Returns cookie value
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets cookie value
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    public Cookie withName(String name) {
        this.name = name;
        return this;
    }

    public Cookie withValue(String value) {
        this.value = value;
        return this;
    }

    public Cookie withDomain(String domain) {
        this.domain = domain;
        return this;
    }

    public Cookie withPath(String path) {
        this.path = path;
        return this;
    }

    public Cookie withExpires(Date expires) {
        this.expires = expires;
        return this;
    }

    public Cookie withExpires(boolean secure) {
        this.secure = secure;
        return this;
    }

    /**
     * Returns cookie as string
     * @return cookie as string
     */
    public String toString() {
        StringBuffer res = new StringBuffer();
        res.append(name).append('=').append(value);

        if (getExpires() != null) {
            // !!! convert time to GMT first!!!
            res.append("; expires=" + cookieFormat.format(getExpires()));
        }

        if (getPath() != null) {
            res.append("; path=" + getPath());
        }

        if (getDomain() != null) {
            res.append("; domain=" + getDomain());
        }

        if (isSecure()) {
            res.append("; secure");
        }
        return res.toString();
    }

    /**
     * Parses cookie definition (in format &lt;name&gt;=&lt;value&gt) and stores
     * name and value to this cookie.
     * @param header cookie as given in a header
     * @throws ParseException thrown if &quot;=&quot; is missing
     */
    protected void parseCookie(String header) throws ParseException {
        int i = header.indexOf('=');
        if (i < 0) {
            throw new ParseException("Missing '='");
        }
        setName(header.substring(0, i));
        setValue(header.substring(i+1).trim());
    }


}
