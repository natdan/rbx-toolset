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
package org.ah.robox.comms;

import java.io.IOException;
import java.util.List;

/**
 *
 * @author Daniel Sendula
 */
public interface PrinterDiscovery {

    List<PrinterChannel> findAllPrinterChannels() throws IOException;

    List<Printer> findAllPrinters() throws IOException;

    Printer getPrinterForChannel(PrinterChannel printerChannel) throws IOException;

    boolean hasChannel(PrinterChannel printerChannel);
}
