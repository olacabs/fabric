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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.olacabs.fabric.manager.domain.ComputationDomain;
import com.olacabs.fabric.manager.service.IComputationService;
import com.olacabs.fabric.manager.service.impl.ComputationService;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Todo .
 */
public class ComputationResourceTest {

    private static IComputationService service = mock(ComputationService.class);

    private static ComputationDomain computation;
    private static ComputationDomain updatedComputation;
    private static ComputationResource resource;
    private static String tenant = "tenant";

    @BeforeClass
    public static void setUp() {
        computation = new ComputationDomain();
        computation.setName("test-computation");
        computation.setDescription("test-description");
        computation.setOwnerEmail("testemail@email.com");
        computation.setTenant(tenant);
        computation.setAttributes(Maps.newHashMap());
        computation.setSources(Sets.newHashSet());
        computation.setProcessors(Sets.newHashSet());
        computation.setConnections(Sets.newHashSet());
        computation.setProperties(Maps.newHashMap());
        computation.setVersion(1);

        updatedComputation = computation;
        updatedComputation.setDescription("test-updated-description");
        updatedComputation.setOwnerEmail("testupdatedemail@email.com");
        updatedComputation.setVersion(2);

        resource = new ComputationResource(service);
    }

    @Test
    public void testValidSave() {
        doReturn(computation).when(service).saveOrUpdate(anyString(), any(ComputationDomain.class));

        final Response response = resource.saveOrUpdate(tenant, computation);
        Assert.assertEquals(response.getStatus(), 201);
        Assert.assertEquals(response.getEntity(), computation);
    }

    @Test
    public void testGetByName() {
        doReturn(ImmutableSet.of(computation, updatedComputation)).when(service).search(anyString(), anyString(),
                anyInt());

        final Response response = resource.getLatestByName(tenant, computation.getName(), 2);
        Assert.assertEquals(response.getStatus(), 200);
        Assert.assertEquals(response.getEntity(), updatedComputation);
    }

    @Test
    public void testDeleteById() {
        doNothing().when(service).delete(anyString(), anyInt());

        final Response response = resource.delete(tenant, 1);
        Assert.assertEquals(response.getStatus(), 200);
    }
}
