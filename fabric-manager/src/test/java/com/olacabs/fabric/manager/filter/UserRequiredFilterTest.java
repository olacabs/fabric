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

package com.olacabs.fabric.manager.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.UriInfo;

import org.junit.Before;
import org.junit.Test;

/**
 * Todo .
 */
public class UserRequiredFilterTest {

    private UserFilter userFilter;
    private ContainerRequestContext request;
    private ContainerResponseContext response;

    @Before
    public void setup() {
        userFilter = new UserFilter();
        request = mock(ContainerRequestContext.class);
        response = mock(ContainerResponseContext.class);
        when(request.getHeaderString(UserFilter.X_USER)).thenReturn("awesome_user@olacabs.com");

        UriInfo uri = mock(UriInfo.class);
        doReturn(uri).when(request).getUriInfo();
        doReturn(null).when(uri).getPath();
        when(request.getUriInfo().getPath()).thenReturn("");
    }

    @Test
    public void shouldSetUserContext() throws IOException, ServletException {
        userFilter.filter(request);
        assertEquals(UserContext.instance().getUser(), "awesome_user@olacabs.com");
    }

    @Test
    public void shouldResetUserContext() throws IOException, ServletException {
        userFilter.filter(request, response);
        assertNull(UserContext.instance().getUser());
    }
}
