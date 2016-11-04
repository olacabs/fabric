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

package com.olacabs.fabric.manager.filter;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;

import com.google.common.base.Strings;
import com.olacabs.fabric.manager.exception.UnProcessableException;

import lombok.extern.slf4j.Slf4j;

/**
 * Todo .
 */
@UserRequired
@Slf4j
public class UserFilter implements ContainerRequestFilter, ContainerResponseFilter {

    static final String X_USER = "X-User";

    @Override
    public void filter(final ContainerRequestContext requestContext) throws IOException {
        final Context context = UserContext.instance().getContextThreadLocal();
        final String userKey = requestContext.getHeaderString(X_USER);
        log.debug(X_USER + " -  {}", userKey);

        if (Strings.isNullOrEmpty(userKey)) {
            log.error("X-User: {}, X-User can't be null/empty", userKey);
            throw new UnProcessableException("X-User can't be null/empty, X-USER: " + userKey);
        }
        context.setItem(userKey);
    }

    @Override
    public void filter(final ContainerRequestContext requestContext, final ContainerResponseContext responseContext)
            throws IOException {
        log.trace("Clearing the user context information for the request {}", responseContext);
        UserContext.instance().clear();
    }
}
