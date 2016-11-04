/*
 * Copyright 2016 ANI Technologies Pvt. Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.olacabs.fabric.manager.exception;

/**
 * Todo .
 */
public class UnProcessableException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private static final String DEFAULT_MSG = "Unable to process...Something invalid here ?";

    public UnProcessableException() {
        super(DEFAULT_MSG);
    }

    /**
     * @param message to throw
     * @param cause to throw
     */
    public UnProcessableException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message to throw
     */
    public UnProcessableException(final String message) {
        super(message);
    }

    /**
     * @param cause to throw
     */
    public UnProcessableException(final Throwable cause) {
        super(DEFAULT_MSG, cause);
    }
}
