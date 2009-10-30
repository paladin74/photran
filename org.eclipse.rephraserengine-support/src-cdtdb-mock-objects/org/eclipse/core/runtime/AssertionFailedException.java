/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.runtime;

/**
 * <code>AssertionFailedException</code> is a runtime exception thrown
 * by some of the methods in <code>Assert</code>.
 * <p>
 * This class can be used without OSGi running.
 * </p><p>
 * This class is not intended to be instantiated or sub-classed by clients.
 * </p>
 * @see Assert
 * @since org.eclipse.equinox.common 3.2
 */
public class AssertionFailedException extends RuntimeException {

    /**
     * All serializable objects should have a stable serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    /** 
     * Constructs a new exception with the given message.
     * 
     * @param detail the message
     */
    public AssertionFailedException(String detail) {
        super(detail);
    }
}
