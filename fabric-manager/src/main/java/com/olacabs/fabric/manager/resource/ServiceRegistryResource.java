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

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.ImmutableMap;
import com.olacabs.fabric.manager.managed.ServiceFinder;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

import lombok.AllArgsConstructor;

/**
 * Todo .
 */
@AllArgsConstructor
@Produces(MediaType.APPLICATION_JSON)
@Path("/instances")
@Api("/instances")
public class ServiceRegistryResource {

    private ServiceFinder finder;

    @GET
    @Timed
    @ApiOperation(value = "Returns all service nodes registered for service discovery", response = List.class)
    public Response getNodeDetails() {
        return Response.ok().entity(ImmutableMap.of("nodes", finder.getAllNodes())).build();
    }
}
