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

package com.olacabs.fabric.manager.exception.mapper;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpResponseException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * Todo .
 */
@Slf4j
public class HttpExceptionMapper implements ExceptionMapper<HttpResponseException> {

    @Override
    public Response toResponse(final HttpResponseException e) {

        final Response defaultResponse =
                Response.serverError().entity(ImmutableMap.of("errors", ImmutableList.of(e.getMessage()))).build();

        log.error("Error - {}", e.getMessage());

        if (e instanceof groovyx.net.http.HttpResponseException) {
            return Response.status(e.getStatusCode())
                    .entity(ImmutableMap.of("errors", ImmutableList.of(e.getMessage()))).build();
        }
        log.error("Stack trace - ", e);
        return defaultResponse;
    }
}
