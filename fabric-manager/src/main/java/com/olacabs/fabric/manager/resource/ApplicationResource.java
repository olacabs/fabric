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
import com.google.common.base.Preconditions;
import com.olacabs.fabric.manager.bean.ExecutorConfig;
import com.olacabs.fabric.manager.bean.RuntimeOptions;
import com.olacabs.fabric.manager.domain.ApplicationDomain;
import com.olacabs.fabric.manager.domain.ComputationDomain;
import com.olacabs.fabric.manager.exception.ResourceNotFoundException;
import com.olacabs.fabric.manager.filter.UserRequired;
import com.olacabs.fabric.manager.service.IApplicationService;
import com.olacabs.fabric.manager.service.IComputationService;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import io.dropwizard.hibernate.UnitOfWork;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.Set;

/**
 * Todo .
 */
@Produces(MediaType.APPLICATION_JSON)
@Path("/v1/applications")
@Slf4j
@AllArgsConstructor
@Api("/v1/applications")
public class ApplicationResource {

    private final IApplicationService applicationService;
    private final IComputationService computationService;
    private final ExecutorConfig executorConfig;

    @POST
    @UserRequired
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed
    @UnitOfWork
    @Path("{tenant}/{name}")
    @ApiOperation(value = "Creates and deploys an application for a computation given tenant and name",
            response = ApplicationDomain.class)
    public Response create(@PathParam("tenant") final String tenant, @PathParam("name") final String computationName,
                           @QueryParam("version") final Integer computationVersion,
                           @NotNull final RuntimeOptions runtimeOptions) {

        final ComputationDomain computation = search(tenant, computationName, computationVersion);
        if (!runtimeOptions.getExecutorDockerImage().isEmpty()) {
            executorConfig.setExecutorDockerImage(runtimeOptions.getExecutorDockerImage());
        }

        computation.setTenant(tenant);
        final ApplicationDomain createdApp = create(runtimeOptions, executorConfig, computation);
        return Response.status(Response.Status.CREATED).entity(createdApp).build();
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed
    @UnitOfWork(transactional = false)
    @Path("{tenant}/{name}")
    @ApiOperation(value = "Get application(only metadata from db)", response = ApplicationDomain.class)
    public Response get(@PathParam("tenant") final String tenant, @PathParam("name") final String computationName) {
        final String appName = applicationService.name(tenant, computationName);
        final ApplicationDomain applicationDomain = applicationService.get(appName, tenant);
        return Response.ok().entity(applicationDomain).build();
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed
    @UnitOfWork(transactional = false)
    @Path("{tenant}/{name}/marathon")
    @ApiOperation(value = "Get application details from marathon", response = Map.class)
    public Response getFromMarathon(@PathParam("tenant") final String tenant,
                                    @PathParam("name") final String computationName) {
        final String appName = applicationService.name(tenant, computationName);
        final Map result = applicationService.getAppFromDeploymentEnv(appName);
        return Response.ok().entity(result).build();
    }

    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed
    @UserRequired
    @UnitOfWork
    @Path("{tenant}/{name}")
    @ApiOperation(value = "Delete application details from marathon and soft deletes the meta")
    public Response delete(@PathParam("tenant") final String tenant, @PathParam("name") final String computationName) {
        final String appName = applicationService.name(tenant, computationName);
        applicationService.delete(appName, tenant);
        return Response.ok().build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed
    @UserRequired
    @UnitOfWork
    @Path("{tenant}/{name}/scale")
    @ApiOperation(value = "Scales application on marathon")
    public Response scale(@PathParam("tenant") final String tenant, @PathParam("name") final String computationName,
                          @NotNull final Map<String, Integer> request) {
        final Integer instances = request.get("instances");
        Preconditions.checkNotNull(instances, "No of instances can't be null");
        Preconditions.checkArgument(instances > 0, "Num of instances should be greater than zero", computationName);
        final String appName = applicationService.name(tenant, computationName);
        applicationService.scale(appName, tenant, instances);
        return Response.noContent().build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed
    @UserRequired
    @UnitOfWork
    @Path("{tenant}/{name}/suspend")
    @ApiOperation(value = "Suspends application on marathon")
    public Response suspend(@PathParam("tenant") final String tenant, @PathParam("name") final String computationName) {
        final String appName = applicationService.name(tenant, computationName);
        applicationService.scale(appName, tenant, 0);
        return Response.noContent().build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed
    @UnitOfWork
    @UserRequired
    @Path("{tenant}/{name}/restart")
    @ApiOperation(value = "Restart application on marathon")
    public Response restart(@PathParam("tenant") final String tenant, @PathParam("name") final String computationName,
                            @QueryParam("forcefully") @DefaultValue("false") final boolean forcefully) {
        final String appName = applicationService.name(tenant, computationName);
        applicationService.restart(appName, tenant, forcefully);
        return Response.noContent().build();
    }

    /*
     * Search computation
     */
    private ComputationDomain search(final String tenant, final String name, final Integer version) {
        final Set<ComputationDomain> computations = computationService.search(tenant, name, version);
        if (computations.isEmpty()) {
            throw new ResourceNotFoundException(
                    String.format("No computation found with name %s, for tenant %s", name, tenant));
        }
        return computations.iterator().next();
    }

    /*
     * create application
     */
    private ApplicationDomain create(final RuntimeOptions runtimeOptions, final ExecutorConfig execConf,
                                     final ComputationDomain computation) {
        final ApplicationDomain application = new ApplicationDomain();
        application.setComputation(computation);
        application.setExecutorConfig(execConf);
        application.setInstances(runtimeOptions.getInstances());
        application.setRuntimeOptions(runtimeOptions);
        application.setName(applicationService.name(computation.getTenant(), computation.getName()));
        application.setTenant(computation.getTenant());
        return applicationService.create(application, computation.getTenant(), runtimeOptions.getInstances());
    }
}
