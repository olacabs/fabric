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

import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.olacabs.fabric.manager.exception.ResourceNotFoundException;
import com.olacabs.fabric.manager.exception.UnProcessableException;

import lombok.extern.slf4j.Slf4j;

/**
 * Todo .
 */
@Slf4j
public class RuntimeExceptionMapper implements ExceptionMapper<RuntimeException> {

    private static final int UN_PROCESSABLE_STATUS = 422;

    @Override
    public Response toResponse(final RuntimeException e) {

        final Response defaultResponse =
                Response.serverError().entity(ImmutableMap.of("errors", ImmutableList.of(e.getMessage()))).build();

        log.error("Error - {}", e.getMessage());

        if (e instanceof ResourceNotFoundException) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ImmutableMap.of("errors", ImmutableList.of(e.getMessage()))).build();
        } else if (e instanceof ConstraintViolationException) {
            return Response.status(UN_PROCESSABLE_STATUS)
                    .entity(ImmutableMap.of("errors", ImmutableList.of(e.getMessage()))).build();
        } else if (e instanceof IllegalArgumentException) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ImmutableMap.of("errors", ImmutableList.of(e.getMessage()))).build();
        } else if (e instanceof UnProcessableException) {
            return Response.status(UN_PROCESSABLE_STATUS)
                    .entity(ImmutableMap.of("errors", ImmutableList.of(e.getMessage()))).build();
        } else if (e instanceof org.hibernate.exception.ConstraintViolationException) {
            return Response.status(UN_PROCESSABLE_STATUS)
                    .entity(ImmutableMap.of("errors", ImmutableList.of(e.getMessage()))).build();
        }

        log.error("Stack trace - ", e);
        return defaultResponse;
    }
}
