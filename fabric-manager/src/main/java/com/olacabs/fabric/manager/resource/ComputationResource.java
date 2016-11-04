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
import com.google.common.collect.Sets;
import com.olacabs.fabric.manager.domain.ComputationDomain;
import com.olacabs.fabric.manager.exception.ResourceNotFoundException;
import com.olacabs.fabric.manager.filter.UserRequired;
import com.olacabs.fabric.manager.service.IComputationService;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import io.dropwizard.hibernate.UnitOfWork;
import lombok.AllArgsConstructor;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Computation resource.
 */
@AllArgsConstructor
@Produces(MediaType.APPLICATION_JSON)
@Path("/v1/computations")
@Api("/v1/computations")
public class ComputationResource {

    private final IComputationService service;

    @UserRequired
    @POST
    @Timed
    @Consumes(MediaType.APPLICATION_JSON)
    @UnitOfWork
    @Path("{tenant}")
    @ApiOperation(value = "Save/update computation, basically creates a new version",
            response = ComputationDomain.class)
    public Response saveOrUpdate(@PathParam("tenant") final String tenant, @Valid final ComputationDomain computation) {
        final ComputationDomain result = service.saveOrUpdate(tenant, computation);
        return Response.status(Response.Status.CREATED).entity(result).build();
    }

    @GET
    @Timed
    @UnitOfWork(transactional = false)
    @Path("{tenant}/{name}")
    @ApiOperation(value = "Get latest computation based on name and version(optional)",
            response = ComputationDomain.class)
    public Response getLatestByName(@PathParam("tenant") final String tenant, @PathParam("name") final String name,
            @QueryParam("version") final Integer version) {

        final Set<ComputationDomain> computations = service.search(tenant, name, version);
        if (computations.isEmpty()) {
            throw new ResourceNotFoundException("Computation Not found");
        }
        return Response
                .ok()
                .entity(version == null ? fetchLatest(computations) : computations.iterator().next())
                .build();
    }

    @GET
    @Timed
    @UnitOfWork(transactional = false)
    @Path("{tenant}")
    @ApiOperation(value = "Search computation based on params", response = List.class)
    public Response search(@PathParam("tenant") final String tenant,
                           @QueryParam("name") final String name,
                           @QueryParam("version") final Integer version,
                           @QueryParam("latest") @DefaultValue("true") final Boolean latest) {

        Set<ComputationDomain> computations = service.search(tenant, name, version);
        if (computations.isEmpty()) {
            throw new ResourceNotFoundException("Computation Not found");
        }

        if (latest) {
            final Set<ComputationDomain> latestComputations = Sets.newHashSet();
            computations.stream()
                    .collect(Collectors.groupingBy(ComputationDomain::getName))
                    .entrySet()
                    .stream()
                    .forEach(entry -> {
                        latestComputations.add(fetchLatest(Sets.newHashSet(entry.getValue())));
                    });
            computations = latestComputations;
        }

        return Response.ok().entity(computations).build();
    }

    @DELETE
    @Timed
    @Path("{tenant}/{internalId}")
    @UnitOfWork
    @UserRequired
    @ApiOperation(value = "Soft delete computation")
    public Response delete(@PathParam("tenant") final String tenant, @PathParam("internalId") final int id) {
        service.delete(tenant, id);
        return Response.ok().build();
    }

    @GET
    @Timed
    @Path(("{tenant}/id/{internalId}"))
    @UnitOfWork(transactional = false)
    public Response get(@PathParam("tenant") final String tenant, @PathParam("internalId") final int id) {
        return Response.ok().entity(service.getById(tenant, id)).build();
    }

    /*
     * TODO - fix with cleaner solution Fetches the latest
     */
    private ComputationDomain fetchLatest(final Set<ComputationDomain> computations) {
        ComputationDomain result = new ComputationDomain();
        result.setVersion(0);
        for (final ComputationDomain computation : computations) {
            if (computation.getVersion() > result.getVersion()) {
                result = computation;
            }
        }
        return result;
    }
}
