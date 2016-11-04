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

import com.google.common.collect.ImmutableSet;
import com.olacabs.fabric.manager.dao.IGlobalPropertyDAO;
import com.olacabs.fabric.manager.dao.impl.GlobalPropertyDAO;
import com.olacabs.fabric.manager.domain.GlobalPropertyDomain;
import com.olacabs.fabric.manager.service.impl.GlobalPropertyService;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Todo.
 */
public class GlobalPropertyServiceTest {
    private static IGlobalPropertyDAO globalPropertyDAO;
    private static IGlobalPropertyService globalPropertyService;
    private static GlobalPropertyDomain globalProperty;

    @BeforeClass
    public static void setUp() throws Exception {
        globalPropertyDAO = mock(GlobalPropertyDAO.class);

        globalProperty = new GlobalPropertyDomain();
        globalProperty.setName("test.global-property");
        globalProperty.setDescription("test-description");

        doReturn(globalProperty).when(globalPropertyDAO).save(any(GlobalPropertyDomain.class));
        doReturn(globalProperty).when(globalPropertyDAO).get(anyInt());
        doReturn(ImmutableSet.of(globalProperty)).when(globalPropertyDAO).search(anyString(), anyString());
        doNothing().when(globalPropertyDAO).delete(any(GlobalPropertyDomain.class));

        globalPropertyService = new GlobalPropertyService(globalPropertyDAO);
    }

    @Test
    public void testSave() {
        final GlobalPropertyDomain saved = globalPropertyService.save(globalProperty);
        Assert.assertEquals(saved, globalProperty);
    }

    @Test
    public void testGet() {
        final GlobalPropertyDomain saved = globalPropertyService.get(globalProperty.getId());
        Assert.assertEquals(saved, globalProperty);
    }

    @Test
    public void testSearch() {
        final Set<GlobalPropertyDomain> search = globalPropertyService.search(globalProperty.getName(),
                globalProperty.getType());
        Assert.assertEquals(search, ImmutableSet.of(globalProperty));
    }

    @Test
    public void testDelete() {
        globalPropertyService.delete(globalProperty.getId());
    }
}
