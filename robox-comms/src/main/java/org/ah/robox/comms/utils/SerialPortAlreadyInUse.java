/*******************************************************************************
 * Copyright (c) 2014-2017 Creative Sphere Limited.
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
package org.ah.robox.comms.utils;

import java.io.IOException;

/**
 *
 * @author Daniel Sendula
 */
public class SerialPortAlreadyInUse extends IOException {

    private static final long serialVersionUID = -4112970370930215315L;

    public SerialPortAlreadyInUse(String message) {
        super(message);
    }

    public SerialPortAlreadyInUse(String message, Throwable cause) {
        super(message, cause);
    }
}
