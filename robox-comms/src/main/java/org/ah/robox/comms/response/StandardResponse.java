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

    private boolean error1;
    private boolean sequenceError;
    private boolean fileTooLargeError;
    private boolean tooLongLine;
    private boolean error5;
    private boolean error6;
    private boolean unknownCommand;
    private boolean error8;
    private boolean error9;
    private boolean error10;
    private boolean bufferOverFlow;
    private boolean error12;
    private boolean error13;
    private boolean error14;
    private boolean error15;
    private boolean error16;
    private boolean error17;
    private boolean error18;
    private boolean error19;
    private boolean error20;


    public boolean isError() {
        return tooLongLine || unknownCommand || bufferOverFlow || sequenceError || fileTooLargeError
                || error1 || error5 || error6 || error8 || error9 || error10
                || error12 || error13 || error14 || error15 || error16
                || error17 || error18 || error19 || error20;
    }

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

    public boolean isError1() {
        return error1;
    }

    public void setError1(boolean error1) {
        this.error1 = error1;
    }

    public boolean isSequenceError() {
        return sequenceError;
    }

    public void setSequenceError(boolean sequenceError) {
        this.sequenceError = sequenceError;
    }

    public boolean isFileTooLargeError() {
        return fileTooLargeError;
    }

    public void setFileTooLargeError(boolean fileTooLargeError) {
        this.fileTooLargeError = fileTooLargeError;
    }

    public boolean isError5() {
        return error5;
    }

    public void setError5(boolean error5) {
        this.error5 = error5;
    }

    public boolean isError6() {
        return error6;
    }

    public void setError6(boolean error6) {
        this.error6 = error6;
    }

    public boolean isError8() {
        return error8;
    }

    public void setError8(boolean error8) {
        this.error8 = error8;
    }

    public boolean isError9() {
        return error9;
    }

    public void setError9(boolean error9) {
        this.error9 = error9;
    }

    public boolean isError10() {
        return error10;
    }

    public void setError10(boolean error10) {
        this.error10 = error10;
    }

    public boolean isError12() {
        return error12;
    }

    public void setError12(boolean error12) {
        this.error12 = error12;
    }

    public boolean isError13() {
        return error13;
    }

    public void setError13(boolean error13) {
        this.error13 = error13;
    }

    public boolean isError14() {
        return error14;
    }

    public void setError14(boolean error14) {
        this.error14 = error14;
    }

    public boolean isError15() {
        return error15;
    }

    public void setError15(boolean error15) {
        this.error15 = error15;
    }

    public boolean isError16() {
        return error16;
    }

    public void setError16(boolean error16) {
        this.error16 = error16;
    }

    public boolean isError17() {
        return error17;
    }

    public void setError17(boolean error17) {
        this.error17 = error17;
    }

    public boolean isError18() {
        return error18;
    }

    public void setError18(boolean error18) {
        this.error18 = error18;
    }

    public boolean isError19() {
        return error19;
    }

    public void setError19(boolean error19) {
        this.error19 = error19;
    }

    public boolean isError20() {
        return error20;
    }

    public void setError20(boolean error20) {
        this.error20 = error20;
    }
}
