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

package com.olacabs.fabric.manager.resource;

import com.codahale.metrics.annotation.Timed;
import com.olacabs.fabric.manager.domain.GlobalPropertyDomain;
import com.olacabs.fabric.manager.service.IGlobalPropertyService;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import io.dropwizard.hibernate.UnitOfWork;
import lombok.AllArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Set;

/**
 * Todo .
 */
@AllArgsConstructor
@Produces(MediaType.APPLICATION_JSON)
@Path("/v1/global/properties")
@Api("/v1/global/properties")
public class GlobalPropertyResource {

    private IGlobalPropertyService service;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed
    @UnitOfWork
    @ApiOperation(value = "Save global property", response = GlobalPropertyDomain.class)
    public Response save(@NotNull @Valid final GlobalPropertyDomain globalProperty) {
        final GlobalPropertyDomain propertyDomain = service.save(globalProperty);
        return Response.status(Response.Status.CREATED).entity(propertyDomain).build();
    }

    @GET
    @Timed
    @UnitOfWork(transactional = false)
    @Path("{id}")
    @ApiOperation(value = "Get global property by id", response = GlobalPropertyDomain.class)
    public Response get(@PathParam("id") final int id) {
        return Response.ok().entity(service.get(id)).build();
    }

    @GET
    @Timed
    @UnitOfWork(transactional = false)
    @ApiOperation(value = "Search global property based on name, param", response = Set.class)
    public Response search(@QueryParam("name") final String name, @QueryParam("type") final String propertyType) {
        return Response.ok().entity(service.search(name, propertyType)).build();
    }

    @DELETE
    @Timed
    @UnitOfWork
    @Path("{id}")
    @ApiOperation(value = "Soft delete")
    public Response delete(@PathParam("id") final int id) {
        service.delete(id);
        return Response.ok().build();
    }
}
