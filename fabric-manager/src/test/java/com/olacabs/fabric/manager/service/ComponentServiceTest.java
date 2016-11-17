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

package com.olacabs.fabric.manager.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.olacabs.fabric.manager.dao.IComponentDAO;
import com.olacabs.fabric.manager.domain.ComponentDomain;
import com.olacabs.fabric.manager.exception.UnProcessableException;
import com.olacabs.fabric.manager.service.impl.ComponentService;
import com.olacabs.fabric.model.common.ComponentType;
import com.olacabs.fabric.model.common.sources.ArtifactoryComponentSource;
import com.olacabs.fabric.model.processor.ProcessorType;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Todo .
 */
public class ComponentServiceTest {

    private static IComponentService componentService;
    private static IComponentDAO componentDAO;
    private static ComponentDomain component;

    @BeforeClass
    public static void setUp() {
        componentDAO = mock(IComponentDAO.class);
        componentService = new ComponentService(componentDAO);

        component = new ComponentDomain();
        component.setSource(ArtifactoryComponentSource.builder().version("0.1").artifactoryUrl("http://test")
                .artifactId("com.test").groupId("test").build());
        component.setType(ComponentType.PROCESSOR);
        component.setName("test");
        component.setCpu(0.5);
        component.setDescription("test-description");
        component.setNamespace("global");
        component.setMemory(1024);
        component.setProcessorType(ProcessorType.EVENT_DRIVEN);
        component.setOptionalProperties(ImmutableList.of("op1", "op2"));
        component.setRequiredProperties(ImmutableList.of("rp1", "rp2"));
        component.setVersion("0.0.1");

        doReturn(component).when(componentDAO).save(any(ComponentDomain.class));
        doReturn(component).when(componentDAO).read(anyInt());
    }

    @Test
    public void testSave() {
        ComponentDomain saved = componentService.save(component);
        Assert.assertEquals(saved, component);
    }

    @Test
    public void testGet() {
        final ComponentDomain saved = componentService.get(component.getInternalId());
        Assert.assertEquals(saved, component);
    }

    @Test
    public void testSaveIfComponentAlreadyRegistered() {
        doReturn(ImmutableSet.of(component)).when(componentDAO).search(anyString(), anyString(), anyString());
        try {
            componentService.save(component);
        } catch (final RuntimeException e) {
            assertTrue(e instanceof UnProcessableException);
        }

        // Cleanup
        doReturn(ImmutableSet.of()).when(componentDAO).search(anyString(), anyString(), anyString());
    }

    @Test
    public void testSearch() {
        doReturn(ImmutableSet.of(component)).when(componentDAO).search(anyString(), anyString(), anyString());
        final Set<ComponentDomain> result = componentService.search("test", "test", "test");
        assertTrue(result.size() == 1);

        // Cleanup
        doReturn(ImmutableSet.of()).when(componentDAO).search(anyString(), anyString(), anyString());
    }

    @Test
    public void testDelete() {
        doReturn(ImmutableSet.of(component)).when(componentDAO).search(anyString(), anyString(), anyString());
        doNothing().when(componentDAO).delete(anyInt());
        componentService.delete("Test", "test", "test");
    }
}
