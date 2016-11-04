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

package com.olacabs.fabric.manager.service.impl;

import java.util.Set;

import com.olacabs.fabric.manager.dao.IComponentDAO;
import com.olacabs.fabric.manager.domain.ComponentDomain;
import com.olacabs.fabric.manager.exception.ResourceNotFoundException;
import com.olacabs.fabric.manager.exception.UnProcessableException;
import com.olacabs.fabric.manager.service.IComponentService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Todo .
 */
@AllArgsConstructor
@Slf4j
public class ComponentService implements IComponentService {

    private final IComponentDAO componentDAO;

    @Override
    public ComponentDomain save(final ComponentDomain component) {
        final Set<ComponentDomain> components =
                componentDAO.search(component.getNamespace(), component.getName(), component.getVersion());
        if (!components.isEmpty()) {
            throw new UnProcessableException(
                    String.format("Component already found with namespace, id, version - [%s,%s,%s]",
                            component.getNamespace(), component.getName(), component.getVersion()));
        }
        return componentDAO.save(component);
    }

    @Override
    public ComponentDomain get(final int id) {
        return componentDAO.read(id);
    }

    @Override
    public Set<ComponentDomain> search(final String namespace, final String name, final String version) {
        return componentDAO.search(namespace, name, version);
    }

    @Override
    public void update(final int id, final ComponentDomain component) {
        componentDAO.update(id, component);
    }

    @Override
    public void delete(final String namespace, final String name, final String version) {
        final Set<ComponentDomain> components = search(namespace, name, version);
        if (components.isEmpty()) {
            throw new ResourceNotFoundException(String
                    .format("Component not found with namespace, id, version - [%s,%s,%s]", namespace, name, version));
        }
        final ComponentDomain component = components.iterator().next();
        componentDAO.delete(component.getInternalId());
    }
}
