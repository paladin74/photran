/*******************************************************************************
 * Copyright (c) 2009 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.tests.parser.failing;

import java.io.FileNotFoundException;
import java.io.IOException;

import junit.framework.Test;

import org.eclipse.photran.internal.tests.parser.MultiTestSuite.FailingParserTestSuite;

public class ParseBrainerdTestSuiteFixed
{
    public static Test suite() throws FileNotFoundException, IOException
    {
        return new FailingParserTestSuite("../../org.eclipse.photran-projects.confidential.walt-brainerd-test-suite",
            true, false) {};
    }
}