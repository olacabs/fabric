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

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.olacabs.fabric.manager.dao.IComponentDAO;
import com.olacabs.fabric.manager.dao.IComputationDAO;
import com.olacabs.fabric.manager.dao.impl.ComponentDAO;
import com.olacabs.fabric.manager.dao.impl.ComputationDAO;
import com.olacabs.fabric.manager.domain.ComputationDomain;
import com.olacabs.fabric.manager.service.impl.ComputationService;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Set;

import static org.mockito.Mockito.*;

/**
 * Computation service test.
 */
public class ComputationServiceTest {

    private static IComputationDAO computationDAO;
    private static IComponentDAO componentDAO;
    private static ComputationDomain computation;
    private static ComputationDomain updatedComputation;
    private static ComputationService computationService;
    private static String tenant = "tenant";

    @BeforeClass
    public static void setUp() {
        computationDAO = mock(ComputationDAO.class);
        componentDAO= mock(ComponentDAO.class);

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

        updatedComputation = computation;
        updatedComputation.setDescription("test-updated-description");
        updatedComputation.setOwnerEmail("testupdatedemail@email.com");

        computationService = new ComputationService(computationDAO, componentDAO);
    }

    @Test
    public void testValidSave() {
        doReturn(Sets.newHashSet()).when(computationDAO).search(anyString(), anyString(), anyInt());
        doReturn(computation).when(computationDAO).save(any(ComputationDomain.class));

        final ComputationDomain result = computationService.saveOrUpdate(tenant, computation);
        Assert.assertEquals(result, computation);
    }

    @Test
    public void testValidUpdate() {
        final Set<ComputationDomain> computationDomainSet = Sets.newHashSet();
        computationDomainSet.add(computation);
        doReturn(computationDomainSet).when(computationDAO).search(anyString(), anyString(), anyInt());
        doReturn(updatedComputation).when(computationDAO).save(any(ComputationDomain.class));

        final ComputationDomain result = computationService.saveOrUpdate(tenant, updatedComputation);
        Assert.assertEquals(result, updatedComputation);
    }

    @Test
    public void testGetById() {
        doReturn(computation).when(computationDAO).get(anyInt());

        final ComputationDomain result = computationService.getById(tenant, computation.getInternalId());
        Assert.assertEquals(result, computation);
    }

    @Test
    public void testSearch() {
        final Set<ComputationDomain> computationDomainSet = Sets.newHashSet();
        computationDomainSet.add(computation);
        doReturn(computationDomainSet).when(computationDAO).search(anyString(), anyString(), anyInt());

        final ComputationDomain result = computationService.getById(tenant, computation.getInternalId());
        Assert.assertEquals(result, computation);
    }

    @Test
    public void testDelete() {
        doReturn(computation).when(computationDAO).get(anyInt());
        doNothing().when(computationDAO).delete(any(ComputationDomain.class));
        computationService.delete(tenant, computation.getInternalId());
    }
}
