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
public class StandardResponse implements Response {

    private boolean tooLongLine;
    private boolean unknownCommand;
    private boolean bufferOverFlow;

    public boolean isTooLongLine() {
        return tooLongLine;
    }
    public void setTooLongLine(boolean tooLongLine) {
        this.tooLongLine = tooLongLine;
    }
    public boolean isUnknownCommand() {
        return unknownCommand;
    }
    public void setUnknownCommand(boolean unknownCommand) {
        this.unknownCommand = unknownCommand;
    }
    public boolean isBufferOverFlow() {
        return bufferOverFlow;
    }
    public void setBufferOverFlow(boolean bufferOverFlow) {
        this.bufferOverFlow = bufferOverFlow;
    }


}
