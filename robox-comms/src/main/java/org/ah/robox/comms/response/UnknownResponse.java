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
package org.ah.robox.comms.response;

/**
 *
 * @author Daniel Sendula
 */
public class UnknownResponse implements Response {

    private int received;

    public UnknownResponse(int received) {
        this.received = received;
    }

    public int getReceivedByte() {
        return received;
    }

    public String toString() {
        return "Unknown response. Received byte 0x" + Integer.toString(received, 16) + " and no more bytes were read";
    }
}
