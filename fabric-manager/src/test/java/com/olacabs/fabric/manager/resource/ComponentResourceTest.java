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
import com.olacabs.fabric.compute.builder.impl.JarScanner;
import com.olacabs.fabric.manager.domain.ComponentDomain;
import com.olacabs.fabric.manager.service.IComponentService;
import com.olacabs.fabric.manager.service.impl.ComponentService;
import com.olacabs.fabric.model.common.ComponentType;
import com.olacabs.fabric.model.processor.ProcessorType;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Todo .
 */
public class ComponentResourceTest {

    private static IComponentService service = mock(ComponentService.class);
    private static JarScanner scanner = mock(JarScanner.class);

    private static ComponentDomain component;
    private static ComponentResource resource;

    @BeforeClass
    public static void setUp() {
        final String defaultArtifactoryPath = "temp";

        component = new ComponentDomain();
        component.setVersion("0.1");
        component.setMemory(1L);
        component.setProcessorType(ProcessorType.EVENT_DRIVEN);
        component.setType(ComponentType.PROCESSOR);
        component.setName("test-component");
        component.setCpu(1L);
        component.setNamespace("global");
        component.setDescription("test-description");

        resource = new ComponentResource(service, defaultArtifactoryPath, scanner);
    }

    @Test
    public void testValidSave() {
        doReturn(component).when(service).save(any(ComponentDomain.class));

        final Response response = resource.save(component);
        Assert.assertEquals(response.getStatus(), 201);
        Assert.assertEquals(response.getEntity(), component);
    }

    @Test
    public void testGetByVersion() {
        doReturn(component).when(service).get(2);

        final Response response = resource.get(2);
        Assert.assertEquals(response.getStatus(), 200);
        Assert.assertEquals(response.getEntity(), component);
    }

    @Test
    public void testSearch() {
        doReturn(ImmutableSet.of(component)).when(service).search(anyString(), anyString(), anyString());

        final Response response =
                resource.search(component.getNamespace(), component.getName(), component.getVersion());
        Assert.assertEquals(response.getStatus(), 200);
        Assert.assertEquals(response.getEntity(), ImmutableSet.of(component));
    }

    @Test
    public void testGet() {
        doReturn(ImmutableSet.of(component)).when(service).search(anyString(), anyString(), anyString());

        final Response response = resource.get(component.getNamespace(), component.getName(), component.getVersion());
        Assert.assertEquals(response.getStatus(), 200);
        Assert.assertEquals(response.getEntity(), component);
    }

    @Test
    public void testAllGroupedByVersion() {
        doReturn(ImmutableSet.of(component)).when(service).search(anyString(), anyString(), anyString());

        final Response response = resource.getAllGroupedByVersion(component.getNamespace(), component.getName());
        Assert.assertEquals(response.getStatus(), 200);
    }
}
