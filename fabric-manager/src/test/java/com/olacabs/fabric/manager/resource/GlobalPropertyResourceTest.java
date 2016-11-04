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
import com.olacabs.fabric.manager.domain.GlobalPropertyDomain;
import com.olacabs.fabric.manager.service.IGlobalPropertyService;
import com.olacabs.fabric.manager.service.impl.GlobalPropertyService;
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
public class GlobalPropertyResourceTest {

    private static IGlobalPropertyService service = mock(GlobalPropertyService.class);

    private static GlobalPropertyDomain globalProperty;
    private static GlobalPropertyResource resource;

    @BeforeClass
    public static void setUp() {
        globalProperty = new GlobalPropertyDomain();
        globalProperty.setName("test.global-property");
        globalProperty.setDescription("test-description");

        resource = new GlobalPropertyResource(service);
    }

    @Test
    public void testValidSave() {
        doReturn(globalProperty).when(service).save(any(GlobalPropertyDomain.class));

        final Response response = resource.save(globalProperty);
        Assert.assertEquals(response.getStatus(), 201);
        Assert.assertEquals(response.getEntity(), globalProperty);
    }

    @Test
    public void testGet() {
        doReturn(globalProperty).when(service).get(anyInt());

        final Response response = resource.get(globalProperty.getId());
        Assert.assertEquals(response.getStatus(), 200);
        Assert.assertEquals(response.getEntity(), globalProperty);
    }

    @Test
    public void testSearch() {
        doReturn(ImmutableSet.of(globalProperty)).when(service).search(anyString(), anyString());

        final Response response = resource.search(globalProperty.getName(), globalProperty.getType());
        Assert.assertEquals(response.getStatus(), 200);
        Assert.assertEquals(response.getEntity(), ImmutableSet.of(globalProperty));
    }

    @Test
    public void testDeleteById() {
        doNothing().when(service).delete(anyInt());

        final Response response = resource.delete(globalProperty.getId());
        Assert.assertEquals(response.getStatus(), 200);
    }
}
