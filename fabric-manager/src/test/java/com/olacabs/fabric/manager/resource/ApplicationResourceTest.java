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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.olacabs.fabric.manager.bean.ExecutorConfig;
import com.olacabs.fabric.manager.bean.RuntimeOptions;
import com.olacabs.fabric.manager.domain.ApplicationDomain;
import com.olacabs.fabric.manager.domain.ComputationDomain;
import com.olacabs.fabric.manager.service.IApplicationService;
import com.olacabs.fabric.manager.service.IComputationService;
import mesosphere.marathon.client.model.v2.GetAppResponse;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Todo .
 */
public class ApplicationResourceTest {

    private static IApplicationService applicationService = mock(IApplicationService.class);
    private static IComputationService computationService = mock(IComputationService.class);
    private static ExecutorConfig executorConfig = mock(ExecutorConfig.class);
    private static String tenant = "tenant";
    private static String computationName = "test-computation";
    private static ApplicationDomain application;
    private static ComputationDomain computation;
    private static RuntimeOptions runtimeOptions;
    private static ApplicationResource resource;

    @BeforeClass
    public static void setUp() {
        computation = new ComputationDomain();
        runtimeOptions = new RuntimeOptions();

        application = new ApplicationDomain();
        application.setName("test-application");
        application.setTenant(tenant);
        application.setComputation(computation);
        application.setExecutorConfig(new ExecutorConfig());
        application.setRuntimeOptions(runtimeOptions);

        resource = new ApplicationResource(applicationService, computationService, executorConfig);
    }

    @Test
    public void testAppCreate() {
        doReturn(ImmutableSet.of(computation)).when(computationService).search(anyString(), anyString(), anyInt());
        doReturn(application).when(applicationService).create(any(ApplicationDomain.class), anyString(), anyInt());

        final Response response = resource.create(tenant, computationName, 2, runtimeOptions);

        Assert.assertEquals(response.getStatus(), 201);
        Assert.assertEquals(response.getEntity(), application);
    }

    @Test
    public void testGet() {
        doReturn(application).when(applicationService).get(anyString(), anyString());

        final Response response = resource.get(application.getTenant(), application.getName());
        Assert.assertEquals(response.getStatus(), 200);
        Assert.assertEquals(response.getEntity(), application);
    }

    @Test
    public void testGetFromMarathon() {
        Map map = Collections.singletonMap("response", new GetAppResponse());
        doReturn(map).when(applicationService).getAppFromDeploymentEnv(anyString());

        final Response response = resource.getFromMarathon(application.getTenant(), application.getName());
        Assert.assertEquals(response.getStatus(), 200);
        Assert.assertEquals(response.getEntity(), map);
    }

    @Test
    public void testDeleteApp() {
        doNothing().when(applicationService).delete(anyString(), anyString());

        final Response response = resource.delete(application.getTenant(), application.getName());
        Assert.assertEquals(response.getStatus(), 200);
    }

    @Test
    public void testScaleApp() {
        doNothing().when(applicationService).scale(anyString(), anyString(), anyInt());

        final Response response = resource.scale(application.getTenant(), computationName,
                ImmutableMap.of("instances", 10));
        Assert.assertEquals(response.getStatus(), 204);
    }

    @Test
    public void testSuspendApp() {
        doNothing().when(applicationService).scale(anyString(), anyString(), anyInt());

        final Response response = resource.suspend(application.getTenant(), computationName);
        Assert.assertEquals(response.getStatus(), 204);
    }

    @Test
    public void testRestartApp() {
        doNothing().when(applicationService).restart(anyString(), anyString(), anyBoolean());

        final Response response = resource.restart(application.getTenant(), computationName, true);
        Assert.assertEquals(response.getStatus(), 204);
    }
}
