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

import java.util.ArrayList;
import java.util.List;
/**
 *
 *
 * @author Daniel Sendula
 */
public class PrintJobsResponse implements Response {

    private List<String> printJobs = new ArrayList<String>();

    public List<String> getPrintJobs() {
        return printJobs;
    }

    public void setPrintJobs(List<String> printJobs) {
        this.printJobs = printJobs;
    }


}
