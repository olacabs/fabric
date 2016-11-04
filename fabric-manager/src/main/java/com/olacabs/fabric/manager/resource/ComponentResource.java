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
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.olacabs.fabric.compute.builder.impl.ArtifactoryJarPathResolver;
import com.olacabs.fabric.compute.builder.impl.JarScanner;
import com.olacabs.fabric.manager.bean.ArtifactoryRegistrationRequest;
import com.olacabs.fabric.manager.bean.ComponentVersions;
import com.olacabs.fabric.manager.domain.ComponentDomain;
import com.olacabs.fabric.manager.filter.UserRequired;
import com.olacabs.fabric.manager.service.IComponentService;
import com.olacabs.fabric.model.common.ComponentMetadata;
import com.olacabs.fabric.model.common.ComponentType;
import com.olacabs.fabric.model.common.sources.ArtifactoryComponentSource;
import com.olacabs.fabric.model.common.sources.JarComponentSource;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import io.dropwizard.hibernate.UnitOfWork;
import lombok.AllArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Components resource.
 */
@AllArgsConstructor
@Produces(MediaType.APPLICATION_JSON)
@Path("/v1/components")
@Api("/v1/components")
public class ComponentResource {

    private final IComponentService service;
    private final String artifactoryPath;
    private final JarScanner scanner;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Timed
    @UnitOfWork
    @UserRequired
    @ApiOperation(value = "Save computation", response = ComponentDomain.class)
    public Response save(@NotNull @Valid final ComponentDomain component) {
        final ComponentDomain savedComponent = service.save(component);
        return Response.status(Response.Status.CREATED).entity(savedComponent).build();
    }

    @GET
    @Path("/versions")
    @Timed
    @UnitOfWork(transactional = false)
    @ApiOperation(value = "Get all computation grouped by version", response = ComponentDomain.class)
    public Response getAllGroupedByVersion(@QueryParam("namespace") final String namespace,
            @QueryParam("name") final String name) {
        return Response.ok().entity(ComponentVersions.resolve(service.search(namespace, name, null))).build();
    }

    @GET
    @Path("/{namespace}/{name}/{version}")
    @Timed
    @UnitOfWork(transactional = false)
    @ApiOperation(value = "Get component based on namespace, name and version", response = ComponentDomain.class)
    public Response get(@PathParam("namespace") final String namespace, @PathParam("name") final String name,
            @PathParam("version") final String version) {
        final Set<ComponentDomain> components = service.search(namespace, name, version);
        if (components.isEmpty()) {
            throw new NotFoundException(String.format("Component with namespace: %s, name: %s, version: %s not found",
                    namespace, name, version));
        }
        return Response.ok().entity(components.iterator().next()).build();
    }

    @GET
    @Timed
    @UnitOfWork(transactional = false)
    @ApiOperation(value = "Searches component based on param", response = Set.class)
    public Response search(@QueryParam("namespace") final String namespace, @QueryParam("name") final String name,
            @QueryParam("version") final String version) {
        return Response.ok().entity(service.search(namespace, name, version)).build();
    }

    @GET
    @Path("/{id}")
    @UnitOfWork(transactional = false)
    @ApiOperation(value = "Get component by id", response = ComponentDomain.class)
    public Response get(@PathParam("id") final int internalId) {
        return Response.ok().entity(service.get(internalId)).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/register")
    @Timed
    @UnitOfWork
    @UserRequired
    @ApiOperation(value = "Registers a processor/source jar from url", response = List.class)
    public Response register(@Context final UriInfo uriinfo, @QueryParam("url") final String url) throws Exception {
        final List<ComponentDomain> registeredComponents = Lists.newArrayList();
        final List<JarScanner.ScanResult> scanResults =
                scanner.loadJars(Collections.singletonList(url), this.getClass().getClassLoader());
        scanResults.stream().map(JarScanner.ScanResult::getMetadata).forEach((componentMetadata) -> {
            final ComponentDomain component = transform(componentMetadata);
            component.setSource(JarComponentSource.builder().url(url).build());
            registeredComponents.add(service.save(component));
        });

        final URI uri = (!registeredComponents.isEmpty() && registeredComponents.size() < 2)
                ? uriinfo.getBaseUriBuilder().path("v1/components")
                        .path(String.valueOf(registeredComponents.get(0).getInternalId())).build()
                : uriinfo.getBaseUriBuilder().path("v1/components").build();

        return Response.created(uri).status(Response.Status.CREATED).entity(registeredComponents).build();
    }

    @UserRequired
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/artifact")
    @Timed
    @UnitOfWork
    @ApiOperation(value = "Registers a processor/source jar from artifactory", response = List.class)
    public Response registerFromArtifactory(@Context UriInfo uriinfo,
            @NotNull final ArtifactoryRegistrationRequest request) throws Exception {
        final List<ComponentDomain> registeredComponents = Lists.newArrayList();
        if (Strings.isNullOrEmpty(request.getArtifactory())) {
            request.setArtifactory(artifactoryPath);
        }

        final String url = ArtifactoryJarPathResolver.resolve(request.getArtifactory(), request.getGroupId(),
                request.getArtifactId(), request.getVersion());
        final List<JarScanner.ScanResult> scanResults =
                scanner.loadJars(Collections.singletonList(url), this.getClass().getClassLoader());
        scanResults.stream().map(JarScanner.ScanResult::getMetadata).forEach((componentMetadata) -> {
            final ComponentDomain component = transform(componentMetadata);
            component.setSource(ArtifactoryComponentSource.builder().artifactoryUrl(request.getArtifactory())
                    .groupId(request.getGroupId()).artifactId(request.getArtifactId()).version(request.getVersion())
                    .build());
            registeredComponents.add(service.save(component));
        });

        final URI uri = (!registeredComponents.isEmpty() && registeredComponents.size() < 2)
                ? uriinfo.getBaseUriBuilder().path("v1/components")
                        .path(String.valueOf(registeredComponents.get(0).getInternalId())).build()
                : uriinfo.getBaseUriBuilder().path("v1/components").build();

        return Response.created(uri).status(Response.Status.CREATED).entity(registeredComponents).build();
    }

    /*
     * Transform from model to component domain
     */
    private ComponentDomain transform(final ComponentMetadata metadata) {
        final ComponentDomain component = new ComponentDomain();
        component.setCpu(metadata.getCpu());
        component.setDescription(metadata.getDescription());
        component.setMemory(metadata.getMemory());
        component.setName(metadata.getName());
        component.setVersion(metadata.getVersion());
        component.setNamespace(metadata.getNamespace());
        component.setOptionalProperties(metadata.getOptionalProperties());
        component.setType(metadata.getType());
        component.setProcessorType(
                Objects.equals(component.getType(), ComponentType.PROCESSOR) ? metadata.getProcessorType() : null);
        component.setRequiredProperties(metadata.getRequiredProperties());
        component.setSource(metadata.getSource());
        return component;
    }
}
